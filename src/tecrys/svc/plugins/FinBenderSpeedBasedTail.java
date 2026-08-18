package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import tecrys.svc.utils.DecoUtils;

import java.awt.*;
import java.util.EnumSet;

public class FinBenderSpeedBasedTail implements EveryFrameWeaponEffectPlugin {
    private static final Color VISIBLE_COLOR = new Color(255, 255, 255, 255);
    private static final Color HIDDEN_COLOR = new Color(255, 255, 255, 0);
    private boolean isInitialized = false;

    // --- SWAY CONTROLS ---
    // Controls how much the fin bends when the ship strafes sideways
    private static final float STRAFE_SWAY_MULTIPLIER = 0.5f;
    // Controls how much the fin lags behind when the ship turns (Positive = drags behind turn)
    private static final float TURN_SWAY_MULTIPLIER = 0.3f;
    // Higher values make the fin snap to the target angle faster, lower makes it more sluggish/organic.
    private static final float SWAY_SMOOTHNESS = 4.0f;

    private float currentRotOffset = 0f;

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

            BendingFinRenderer renderer = new BendingFinRenderer(weapon, isMirrored);
            engine.addLayeredRenderingPlugin(renderer);
            isInitialized = true;
        }

// In advance()
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

            float strafeOffset = 0f;
            if (speed > 1f && maxSpeed > 1f) {
                float velDir = VectorUtils.getFacing(ship.getVelocity());
                float angleDiff = MathUtils.getShortestRotation(ship.getFacing(), velDir);
                float lateralFraction = (float) Math.sin(Math.toRadians(angleDiff)) * (speed / maxSpeed);
                strafeOffset = lateralFraction * halfArc * STRAFE_SWAY_MULTIPLIER;
            }

            // Turning left = positive angular velocity. Negating it makes the fin rotate clockwise (drag behind)
            float turnOffset = -ship.getAngularVelocity() * TURN_SWAY_MULTIPLIER;

            // Combine forces and clamp to max arc limits
            float targetOffset = strafeOffset + turnOffset;
            targetOffset = Math.max(-halfArc, Math.min(halfArc, targetOffset));

            // Smoothly interpolate current offset toward the target offset
            currentRotOffset += (targetOffset - currentRotOffset) * (amount * SWAY_SMOOTHNESS);
        } else {
            currentRotOffset = 0f; // Return to center in refit screen
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
        private static final float BASE_SWIM_RATE = 1.5f;
        private static final float MAX_SPEED_BONUS_RATE = 6f;
        private static final int SEGMENTS = 24;

        // Increase this for a wider amplitude (wider wiggle)
        private static final float MAX_BEND_PIXELS = 12f;

        // Increase this for a shorter wavelength (more ripples along the fin)
        // 1.0f is default. 2.0f doubles the ripples.
        private static final float WAVE_FREQUENCY = 2.0f;

        // --- ANCHOR & PIVOT CONTROLS ---
        private static final float PIVOT_FRACTION = 0.6f;

        private static final float ANCHOR_X_OFFSET = 0f;
        private static final float ANCHOR_Y_OFFSET = 0f;

        // --- WAVE & TEXTURE CONTROLS ---
        private final boolean reverseWaveDirection;

        private static final boolean INVERT_U_MAPPING = false;
        private static final boolean INVERT_V_MAPPING = true;

        public BendingFinRenderer(WeaponAPI weapon, boolean isMirrored) {
            this.weapon = weapon;
            this.sprite = weapon.getSprite();
            this.isMirrored = isMirrored;

            // Evaluates once per weapon
            this.reverseWaveDirection = (weapon.getDamageType() == DamageType.ENERGY);

            if (isMirrored) {
                phaseOffset = (float) Math.PI;
            } else {
                phaseOffset = 0f;
            }
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

// In render()
            sprite.setColor(HIDDEN_COLOR);
            sprite.setAlphaMult(0f);

            float length = sprite.getHeight();
            float width = sprite.getWidth();
            float centerX = sprite.getCenterX();
            float centerY = sprite.getCenterY();

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

                float distFromPivot = fraction - PIVOT_FRACTION;

// CRITICAL FIX: Cast Math.PI calculation safely to float to satisfy the compiler
                float waveDir = reverseWaveDirection ? 1f : -1f;

                // MULTIPLY by WAVE_FREQUENCY to compress or stretch the wave
                float wavePropagation = (float) (distFromPivot * Math.PI * waveDir * WAVE_FREQUENCY);

                float bendOffset = (float) Math.sin(phase + phaseOffset + wavePropagation) * MAX_BEND_PIXELS * distFromPivot;

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
            if (weapon.getDamageType() == DamageType.ENERGY) {
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