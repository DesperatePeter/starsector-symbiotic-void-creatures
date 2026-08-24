package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.opengl.GL11;
import org.lazywizard.lazylib.FastTrig;
import tecrys.svc.utils.DecoUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.json.JSONObject;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class MultiTentacleSweeperSpeedBased implements EveryFrameWeaponEffectPlugin {

    private boolean isInitialized = false;
    private static final Color VISIBLE_COLOR = new Color(255, 255, 255, 255);
    private static final Color HIDDEN_COLOR = new Color(255, 255, 255, 0);

    // --- SWAY CONTROLS ---
    private static final float STRAFE_SWAY_MULTIPLIER = 1.5f;
    private static final float TURN_SWAY_MULTIPLIER = 0.6f;
    private static final float SWAY_SMOOTHNESS = 7.0f;

    // --- SWIVEL SYNC CONTROLS ---
    private static final float SWIVEL_ARC_FRACTION = 0.5f;
    private static final float SWIVEL_SYNC_OFFSET = (float) (Math.PI / 2f);

    // --- PERFORMANCE CACHES ---
    private static final Map<String, Float> sizeMultCache = new HashMap<>();
    private static final Map<String, Float> tierPhaseCache = new HashMap<>();

    private float currentRotOffset = 0f;
    private float swivelDir = 1f;
    private float tierPhase = 0f;

    private BendingTentacleRenderer renderer;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;

        ShipAPI ship = weapon.getShip();
        if (ship == null || ship.isHulk()) return;

        boolean isRefit = (ship.getOriginalOwner() == -1);

        // --- ANIMATION FRAME CONTROL ---
        if (weapon.getAnimation() != null) {
            weapon.getAnimation().pause();
            if (isRefit) {
                weapon.getAnimation().setFrame(0);
            } else {
                if (weapon.getAnimation().getNumFrames() > 1) {
                    weapon.getAnimation().setFrame(1);
                } else {
                    weapon.getAnimation().setFrame(0);
                }
            }
        }

        if (!isInitialized) {
            weapon.ensureClonedSpec();
            String targetId = weapon.getSpec().getWeaponId();

            if (!sizeMultCache.containsKey(targetId)) {
                float sizeMult = 1.0f;
                float localTierPhase = 0f;
                try {
                    org.json.JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/weapons/weapon_data.csv", "starsector");
                    for (int i = 0; i < spreadsheet.length(); i++) {
                        JSONObject row = spreadsheet.getJSONObject(i);
                        if (targetId.equals(row.optString("id"))) {
                            sizeMult = (float) row.optDouble("rarity", 1.0);

                            // Map values like 0.5 directly from the tier column
                            localTierPhase = (float) row.optDouble("tier", 0.0);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Global.getLogger(MultiTentacleSweeperSpeedBased.class).error("Could not read CSV columns for tentacle: " + targetId, e);
                }
                sizeMultCache.put(targetId, sizeMult);
                tierPhaseCache.put(targetId, localTierPhase);
            }

            float sizeMult = sizeMultCache.get(targetId);
            this.tierPhase = tierPhaseCache.get(targetId);

            boolean isMirrored = DecoUtils.isOnLeft(weapon) != DecoUtils.isFacingForward(weapon);

            if (isRefit && isMirrored) {
                DecoUtils.mirror(weapon, false);
            }

            float toFrontAngle = MathUtils.getShortestRotation(weapon.getArcFacing(), 0f);
            swivelDir = (toFrontAngle >= 0) ? 1f : -1f;

            if (Math.abs(toFrontAngle) < 1f || Math.abs(toFrontAngle) > 179f) {
                swivelDir = 1f;
            }

            renderer = new BendingTentacleRenderer(weapon, isMirrored, sizeMult, this.tierPhase);
            engine.addLayeredRenderingPlugin(renderer);
            isInitialized = true;
        }

        if (isRefit) {
            weapon.getSprite().setColor(VISIBLE_COLOR);
            weapon.getSprite().setAlphaMult(1f);
            return;
        } else {
            weapon.getSprite().setColor(HIDDEN_COLOR);
            weapon.getSprite().setAlphaMult(0f);
        }

        float arcCenterAbsolute = ship.getFacing() + weapon.getArcFacing();
        float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
        float speed = ship.getVelocity().length();
        float halfArc = weapon.getArc() / 2f;

        float speedFraction = 0f;
        if (maxSpeed > 1f) {
            speedFraction = Math.max(0f, Math.min(1f, speed / maxSpeed));
        }

        float currentSwivelPhase = renderer.getPhase() + SWIVEL_SYNC_OFFSET;

        // Apply fractional phase offsets dynamically (1.0 = PI)
        currentSwivelPhase += this.tierPhase * (float) Math.PI;

        float swivelAmplitude = halfArc * SWIVEL_ARC_FRACTION;
        float swivelOffset = (float) (swivelAmplitude * FastTrig.sin(currentSwivelPhase) * swivelDir);

        float strafeOffset = 0f;
        if (speed > 1f && maxSpeed > 1f) {
            float velDir = VectorUtils.getFacing(ship.getVelocity());
            float angleDiff = MathUtils.getShortestRotation(ship.getFacing(), velDir);
            float lateralFraction = (float) (FastTrig.sin(Math.toRadians(angleDiff)) * speedFraction);
            strafeOffset = lateralFraction * halfArc * STRAFE_SWAY_MULTIPLIER;
        }

        float turnOffset = -ship.getAngularVelocity() * TURN_SWAY_MULTIPLIER;

        float targetOffset = swivelOffset + strafeOffset + turnOffset;
        targetOffset = Math.max(-halfArc, Math.min(halfArc, targetOffset));

        currentRotOffset += (targetOffset - currentRotOffset) * (amount * SWAY_SMOOTHNESS);
        weapon.setCurrAngle(arcCenterAbsolute + currentRotOffset);
    }

    public static class BendingTentacleRenderer extends BaseCombatLayeredRenderingPlugin {
        private final WeaponAPI weapon;
        private final SpriteAPI sprite;
        private float phase = 0f;
        private final float phaseOffset;
        private final float sizeMult;

        // --- RENDERER SETTINGS ---
        private static final float BASE_SWIM_RATE = 2.0f;
        private static final float MAX_SPEED_BONUS_RATE = 6.0f;
        private static final int SEGMENTS = 16;
        private static final float MAX_BEND_RATIO = 0.35f;
        private static final float WAVE_FREQUENCY = 1.5f;

        // --- ANCHOR CONTROLS ---
        private static final float ANCHOR_X_OFFSET = 0f;
        private static final float ANCHOR_Y_OFFSET = 0f;

        // --- WAVE & TEXTURE CONTROLS ---
        private static final boolean REVERSE_WAVE_DIRECTION = false;
        private static final boolean INVERT_U_MAPPING = false;
        private static final boolean INVERT_V_MAPPING = true;

        // Optimized Kinematics Arrays
        private final float[] targetX = new float[SEGMENTS + 1];
        private final float[] targetY = new float[SEGMENTS + 1];
        private final float[] spineX = new float[SEGMENTS + 1];
        private final float[] spineY = new float[SEGMENTS + 1];

        // Trigonometry caches
        private final float[] segSin = new float[SEGMENTS + 1];
        private final float[] segCos = new float[SEGMENTS + 1];

        // Immutable Pre-Allocated Values
        private final float[] distFromPivots = new float[SEGMENTS + 1];
        private final float[] wavePropagations = new float[SEGMENTS + 1];
        private final float[] vCoords = new float[SEGMENTS + 1];
        private final float uLeft;
        private final float uRight;
        private final EnumSet<CombatEngineLayers> cachedLayer;

        public BendingTentacleRenderer(WeaponAPI weapon, boolean isMirrored, float sizeMult, float tierPhase) {
            this.weapon = weapon;
            this.sprite = weapon.getSprite();
            this.sizeMult = sizeMult;

            float pOffset = 0f;
            if (isMirrored) pOffset += (float) Math.PI;

            // Map the float value directly onto the Math.PI offset modifier
            pOffset += tierPhase * (float) Math.PI;
            this.phaseOffset = pOffset;

            if (weapon.getSlot().isTurret()) {
                this.cachedLayer = EnumSet.of(CombatEngineLayers.CONTRAILS_LAYER);
            } else {
                this.cachedLayer = EnumSet.of(CombatEngineLayers.BELOW_SHIPS_LAYER);
            }

            float length = sprite.getHeight();
            float centerY = sprite.getCenterY();
            float pivotFraction = centerY / length;

            float texX = sprite.getTexX();
            float texY = sprite.getTexY();
            float texW = sprite.getTexWidth();
            float texH = sprite.getTexHeight();

            boolean shouldInvertU = INVERT_U_MAPPING ? !isMirrored : isMirrored;
            this.uLeft = shouldInvertU ? texX + texW : texX;
            this.uRight = shouldInvertU ? texX : texX + texW;

            for (int i = 0; i <= SEGMENTS; i++) {
                float fraction = (float) i / SEGMENTS;

                this.distFromPivots[i] = fraction - pivotFraction;
                this.wavePropagations[i] = (float) (this.distFromPivots[i] * Math.PI * (REVERSE_WAVE_DIRECTION ? 1f : -1f) * WAVE_FREQUENCY);
                this.vCoords[i] = INVERT_V_MAPPING ? (texY + texH * fraction) : (texY + texH - texH * fraction);
            }
        }

        public float getPhase() {
            return phase;
        }

        @Override
        public void advance(float amount) {
            if (Global.getCombatEngine().isPaused()) return;
            ShipAPI ship = weapon.getShip();
            if (ship == null || ship.isHulk()) return;

            float shipVelocity = ship.getVelocity().length();
            float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();

            float speedPct = 0f;
            if (maxSpeed > 1f) {
                speedPct = Math.min(1f, Math.max(0f, shipVelocity / maxSpeed));
            }

            float currentRate = BASE_SWIM_RATE + (speedPct * MAX_SPEED_BONUS_RATE);
            phase += currentRate * amount;

            if (phase > Math.PI * 2) {
                phase -= (float) (Math.PI * 2);
            }
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            ShipAPI ship = weapon.getShip();
            if (ship == null || !ship.isAlive() || ship.isHulk()) return;

            float length = sprite.getHeight() * sizeMult;
            float width = sprite.getWidth() * sizeMult;
            float centerX = sprite.getCenterX() * sizeMult;

            float dynamicMaxBend = length * MAX_BEND_RATIO;

            for (int i = 0; i <= SEGMENTS; i++) {
                float bendOffset = (float) (FastTrig.sin(phase + phaseOffset + wavePropagations[i]) * dynamicMaxBend * distFromPivots[i]);
                targetX[i] = bendOffset;
                targetY[i] = length * ((float) i / SEGMENTS);
            }

            float segLen = length / SEGMENTS;
            float chainX = 0f;
            float chainY = 0f;

            for (int i = 0; i <= SEGMENTS; i++) {
                spineX[i] = chainX;
                spineY[i] = chainY;

                if (i < SEGMENTS) {
                    float dx = targetX[i + 1] - targetX[i];
                    float dy = targetY[i + 1] - targetY[i];

                    float len = (float) Math.sqrt(dx * dx + dy * dy);
                    if (len > 0.0001f) {
                        segSin[i] = dx / len;
                        segCos[i] = dy / len;
                    } else {
                        segSin[i] = 0f;
                        segCos[i] = 1f;
                    }

                    chainX += segSin[i] * segLen;
                    chainY += segCos[i] * segLen;
                } else {
                    segSin[i] = segSin[i - 1];
                    segCos[i] = segCos[i - 1];
                }
            }

            float exactPivotI = (sprite.getCenterY() / sprite.getHeight()) * SEGMENTS;
            int indexA = (int) Math.floor(exactPivotI);
            int indexB = (int) Math.ceil(exactPivotI);
            float remainder = exactPivotI - indexA;

            float pivotX = (indexA == indexB) ? spineX[indexA] : spineX[indexA] * (1f - remainder) + spineX[indexB] * remainder;
            float pivotY = (indexA == indexB) ? spineY[indexA] : spineY[indexA] * (1f - remainder) + spineY[indexB] * remainder;

            for (int i = 0; i <= SEGMENTS; i++) {
                spineX[i] -= pivotX;
                spineY[i] -= pivotY;
            }

            float baseAngle = weapon.getCurrAngle();

            GL11.glPushMatrix();
            GL11.glTranslatef(weapon.getLocation().x, weapon.getLocation().y, 0f);
            GL11.glRotatef(baseAngle - 90f, 0f, 0f, 1f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.getTextureId());

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            float alpha = viewport.getAlphaMult();
            GL11.glColor4f(1f, 1f, 1f, alpha);

            GL11.glBegin(GL11.GL_QUAD_STRIP);

            for (int i = 0; i <= SEGMENTS; i++) {
                float leftX = spineX[i] - centerX * segCos[i] + ANCHOR_X_OFFSET;
                float leftY = spineY[i] + centerX * segSin[i] + ANCHOR_Y_OFFSET;

                float rightX = spineX[i] + (width - centerX) * segCos[i] + ANCHOR_X_OFFSET;
                float rightY = spineY[i] - (width - centerX) * segSin[i] + ANCHOR_Y_OFFSET;

                float v = vCoords[i];

                GL11.glTexCoord2f(uLeft, v);
                GL11.glVertex2f(leftX, leftY);

                GL11.glTexCoord2f(uRight, v);
                GL11.glVertex2f(rightX, rightY);
            }

            GL11.glEnd();
            GL11.glPopMatrix();
        }

        @Override
        public float getRenderRadius() {
            return 99999f;
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return cachedLayer;
        }

        @Override
        public boolean isExpired() {
            return weapon.getShip() == null || !Global.getCombatEngine().isEntityInPlay(weapon.getShip()) || weapon.getShip().isHulk();
        }
    }
}