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

import java.awt.Color;
import java.util.EnumSet;

public class FinSweepBenderSpeedBased implements EveryFrameWeaponEffectPlugin {

    private boolean isInitialized = false;
    private static final Color VISIBLE_COLOR = new Color(255, 255, 255, 255);
    private static final Color HIDDEN_COLOR = new Color(255, 255, 255, 0);

    // --- SWAY CONTROLS ---
    private static final float STRAFE_SWAY_MULTIPLIER = 1.5f;
    private static final float TURN_SWAY_MULTIPLIER = 0.6f;
    private static final float SWAY_SMOOTHNESS = 7.0f;

    // --- SWIVEL SYNC CONTROLS ---
    // Controls how much of the weapon's arc is used for the continuous swivel.
    // 1.0f uses the entire arc. 0.5f uses half of the arc.
    private static final float SWIVEL_ARC_FRACTION = 0.5f;

    // Offsets the rotation phase from the bend phase.
    // PI / 2 (90 degrees) ensures that when the rotation is swinging the fastest,
    // the fin is bent at its maximum, perfectly simulating drag against water.
    private static final float SWIVEL_SYNC_OFFSET = (float) (Math.PI / 2f);

    private float currentRotOffset = 0f;
    private float swivelDir = 1f;

    // We now store the renderer so we can read its exact phase and lock them together
    private BendingFinRenderer renderer;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (ship == null || ship.isHulk()) return;

        boolean isRefit = (ship.getOriginalOwner() == -1);

        if (!isInitialized) {
            boolean isMirrored = DecoUtils.isOnLeft(weapon) != DecoUtils.isFacingForward(weapon);

            if (isRefit && isMirrored) {
                DecoUtils.mirror(weapon, false);
            }

            // Determine direction to rotate towards the front of the ship initially.
            float toFrontAngle = MathUtils.getShortestRotation(weapon.getArcFacing(), 0f);
            swivelDir = (toFrontAngle >= 0) ? 1f : -1f;

            if (Math.abs(toFrontAngle) < 1f || Math.abs(toFrontAngle) > 179f) {
                swivelDir = 1f;
            }

            renderer = new BendingFinRenderer(weapon, isMirrored);
            engine.addLayeredRenderingPlugin(renderer);
            isInitialized = true;
        }

        if (isRefit) {
            weapon.getSprite().setColor(VISIBLE_COLOR);
            weapon.getSprite().setAlphaMult(1f);
        } else {
            weapon.getSprite().setColor(HIDDEN_COLOR);
            weapon.getSprite().setAlphaMult(0f);
        }

        if (engine.isPaused()) return;

        float arcCenterAbsolute = ship.getFacing() + weapon.getArcFacing();

        // Organic Swivel Logic
        if (!isRefit) {
            float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
            float speed = ship.getVelocity().length();
            float halfArc = weapon.getArc() / 2f;

            float speedFraction = 0f;
            if (maxSpeed > 1f) {
                speedFraction = Math.max(0f, Math.min(1f, speed / maxSpeed));
            }

            // 1. Synchronized Back-and-Forth Swivel
            // We fetch the exact phase from the renderer to keep the rotation and the fin bend perfectly locked.
            float currentSwivelPhase = renderer.getPhase() + SWIVEL_SYNC_OFFSET;
            float swivelAmplitude = halfArc * SWIVEL_ARC_FRACTION;
            float swivelOffset = (float) (swivelAmplitude * FastTrig.sin(currentSwivelPhase) * swivelDir);

            // 2. Strafe and Turn Sway
            float strafeOffset = 0f;
            if (speed > 1f && maxSpeed > 1f) {
                float velDir = VectorUtils.getFacing(ship.getVelocity());
                float angleDiff = MathUtils.getShortestRotation(ship.getFacing(), velDir);
                float lateralFraction = (float) (FastTrig.sin(Math.toRadians(angleDiff)) * speedFraction);
                strafeOffset = lateralFraction * halfArc * STRAFE_SWAY_MULTIPLIER;
            }

            float turnOffset = -ship.getAngularVelocity() * TURN_SWAY_MULTIPLIER;

            // Combine all offsets, but rigorously clamp the final result to the weapon's hard max limits
            float targetOffset = swivelOffset + strafeOffset + turnOffset;
            targetOffset = Math.max(-halfArc, Math.min(halfArc, targetOffset));

            currentRotOffset += (targetOffset - currentRotOffset) * (amount * SWAY_SMOOTHNESS);
        } else {
            currentRotOffset = 0f;
        }

        weapon.setCurrAngle(arcCenterAbsolute + currentRotOffset);
    }

    public static class BendingFinRenderer extends BaseCombatLayeredRenderingPlugin {
        private final WeaponAPI weapon;
        private final SpriteAPI sprite;
        private float phase = 0f;
        private final float phaseOffset;
        private final boolean isMirrored;

        // --- RENDERER SETTINGS ---
        // These now dictate the speed of BOTH the visual bend and the physical base rotation
        private static final float BASE_SWIM_RATE = 2.0f;
        private static final float MAX_SPEED_BONUS_RATE = 6.0f;
        private static final int SEGMENTS = 16;
        private static final float MAX_BEND_PIXELS = 22f;
        private static final float WAVE_FREQUENCY = 1.5f;

        // --- ANCHOR CONTROLS ---
        private static final float ANCHOR_X_OFFSET = 0f;
        private static final float ANCHOR_Y_OFFSET = 0f;

        // --- WAVE & TEXTURE CONTROLS ---
        private static final boolean REVERSE_WAVE_DIRECTION = false;
        private static final boolean INVERT_U_MAPPING = false;
        private static final boolean INVERT_V_MAPPING = true;

        public BendingFinRenderer(WeaponAPI weapon, boolean isMirrored) {
            this.weapon = weapon;
            this.sprite = weapon.getSprite();
            this.isMirrored = isMirrored;

            if (isMirrored) {
                phaseOffset = (float) Math.PI;
            } else {
                phaseOffset = 0f;
            }
        }

        // Getter allows the outer class to synchronize physical rotation with this visual phase
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

            boolean isRefit = (ship.getOriginalOwner() == -1);
            if (isRefit) return;

            sprite.setColor(HIDDEN_COLOR);
            sprite.setAlphaMult(0f);

            float length = sprite.getHeight();
            float width = sprite.getWidth();
            float centerX = sprite.getCenterX();
            float centerY = sprite.getCenterY();

            // DYNAMIC PIVOT: This forces the wave anchor to be exactly on the weapon slot
            float pivotFraction = centerY / length;

            float texX = sprite.getTexX();
            float texY = sprite.getTexY();
            float texW = sprite.getTexWidth();
            float texH = sprite.getTexHeight();

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

                float fraction = (float) i / SEGMENTS;
                float localY = (length * fraction) - centerY + ANCHOR_Y_OFFSET;

                float distFromPivot = fraction - pivotFraction;

                float waveDir = REVERSE_WAVE_DIRECTION ? 1f : -1f;
                float wavePropagation = (float) (distFromPivot * Math.PI * waveDir * WAVE_FREQUENCY);

                float bendOffset = (float) (FastTrig.sin(phase + phaseOffset + wavePropagation) * MAX_BEND_PIXELS * distFromPivot);

                float leftX = -centerX + bendOffset + ANCHOR_X_OFFSET;
                float rightX = width - centerX + bendOffset + ANCHOR_X_OFFSET;

                float v;
                if (INVERT_V_MAPPING) {
                    v = texY + (texH * fraction);
                } else {
                    v = texY + texH - (texH * fraction);
                }

                float uLeft, uRight;
                boolean shouldInvertU = isMirrored;
                if (INVERT_U_MAPPING) {
                    shouldInvertU = !shouldInvertU;
                }

                if (shouldInvertU) {
                    uLeft = texX + texW;
                    uRight = texX;
                } else {
                    uLeft = texX;
                    uRight = texX + texW;
                }

                GL11.glTexCoord2f(uLeft, v);
                GL11.glVertex2f(leftX, localY);

                GL11.glTexCoord2f(uRight, v);
                GL11.glVertex2f(rightX, localY);
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
            if (weapon.getSlot().isHardpoint()){
                return EnumSet.of(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER);
            } else {
                return EnumSet.of(CombatEngineLayers.BELOW_SHIPS_LAYER);
            }
        }

        @Override
        public boolean isExpired() {
            return weapon.getShip() == null || !Global.getCombatEngine().isEntityInPlay(weapon.getShip()) || weapon.getShip().isHulk();
        }
    }
}