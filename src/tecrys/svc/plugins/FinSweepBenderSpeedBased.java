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
import tecrys.svc.utils.DecoUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.awt.Color;
import java.util.EnumSet;

public class FinSweepBenderSpeedBased implements EveryFrameWeaponEffectPlugin {

    private boolean isInitialized = false;
    private static final Color VISIBLE_COLOR = new Color(255, 255, 255, 255);
    private static final Color HIDDEN_COLOR = new Color(255, 255, 255, 0);
    // --- SWAY CONTROLS (Reactive Movement) ---
    private static final float STRAFE_SWAY_MULTIPLIER = 1.5f;
    private static final float TURN_SWAY_MULTIPLIER = 0.6f;
    private static final float SWAY_SMOOTHNESS = 7.0f;

    // --- SWEEP CONTROLS (Continuous Swimming) ---
    private static final float BASE_SWEEP_RATE = 1.5f;
    private static final float MAX_SWEEP_BONUS_RATE = 4.0f;
    private static final float SWEEP_ARC_MULTIPLIER = 0.6f;

    private float currentSwayOffset = 0f;
    private float sweepPhase = 0f;
    private float sweepPhaseOffset = 0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        ShipAPI ship = weapon.getShip();
        if (ship == null || ship.isHulk()) return;

        boolean isRefit = (ship.getOriginalOwner() == -1);

        if (!isInitialized) {
            boolean isMirrored = DecoUtils.isOnLeft(weapon) != DecoUtils.isFacingForward(weapon);

            if (isMirrored) {
                if (isRefit) DecoUtils.mirror(weapon, false);
                sweepPhaseOffset = (float) Math.PI;
            } else {
                sweepPhaseOffset = 0f;
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
        float finalRotOffset = 0f;

        if (!isRefit) {
            float maxSpeed = ship.getMutableStats().getMaxSpeed().getModifiedValue();
            float speed = ship.getVelocity().length();
            float halfArc = weapon.getArc() / 2f;

            float speedPct = 0f;
            if (maxSpeed > 1f) {
                speedPct = Math.min(1f, Math.max(0f, speed / maxSpeed));
            }

            float currentSweepRate = BASE_SWEEP_RATE + (speedPct * MAX_SWEEP_BONUS_RATE);
            sweepPhase += currentSweepRate * amount;
            if (sweepPhase > Math.PI * 2) {
                sweepPhase -= (float) (Math.PI * 2);
            }
            float sweepOffset = (float) Math.sin(sweepPhase + sweepPhaseOffset) * (halfArc * SWEEP_ARC_MULTIPLIER);

            float strafeOffset = 0f;
            if (speed > 1f && maxSpeed > 1f) {
                float velDir = VectorUtils.getFacing(ship.getVelocity());
                float angleDiff = MathUtils.getShortestRotation(ship.getFacing(), velDir);
                float lateralFraction = (float) Math.sin(Math.toRadians(angleDiff)) * (speedPct);
                strafeOffset = lateralFraction * halfArc * STRAFE_SWAY_MULTIPLIER;
            }

            float turnOffset = -ship.getAngularVelocity() * TURN_SWAY_MULTIPLIER;

            float targetSway = strafeOffset + turnOffset;
            targetSway = Math.max(-halfArc, Math.min(halfArc, targetSway));

            currentSwayOffset += (targetSway - currentSwayOffset) * (amount * SWAY_SMOOTHNESS);

            finalRotOffset = sweepOffset + currentSwayOffset;
            finalRotOffset = Math.max(-halfArc, Math.min(halfArc, finalRotOffset));

        } else {
            currentSwayOffset = 0f;
            sweepPhase = 0f;
        }

        weapon.setCurrAngle(arcCenterAbsolute + finalRotOffset);
    }

    public static class BendingFinRenderer extends BaseCombatLayeredRenderingPlugin {
        private final WeaponAPI weapon;
        private final SpriteAPI sprite;
        private float phase = 0f;
        private final float phaseOffset;
        private final boolean isMirrored;

        // --- RENDERER SETTINGS ---
        private static final float BASE_SWIM_RATE = 2.0f;
        private static final float MAX_SPEED_BONUS_RATE = 7.0f;
        private static final int SEGMENTS = 16;

        private static final float MAX_BEND_PIXELS = 16f;
        private static final float WAVE_FREQUENCY = 1.5f;

        // --- ANCHOR & PIVOT CONTROLS ---
        private static final float PIVOT_FRACTION = 0.26f;
        private static final float PIVOT_STIFFNESS = 0.2f;

        // Tunes how much the Y-axis retracts to compensate for X-axis bending.
        // Start around 0.25f. Higher = pulls in more. 0f = no compensation (bobbing).
        private static final float LENGTH_COMPENSATION_FACTOR = 0.25f;

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

                float distFromPivot = fraction - PIVOT_FRACTION;
                float absDist = Math.abs(distFromPivot);

                float amplitudeMultiplier;
                if (absDist <= PIVOT_STIFFNESS) {
                    float normalizedDist = absDist / PIVOT_STIFFNESS;
                    amplitudeMultiplier = normalizedDist * normalizedDist;
                } else {
                    amplitudeMultiplier = 1f;
                }

                float waveDir = REVERSE_WAVE_DIRECTION ? 1f : -1f;
                float wavePropagation = (float) (distFromPivot * Math.PI * waveDir * WAVE_FREQUENCY);

                float bendOffset = (float) Math.sin(phase + phaseOffset + wavePropagation)
                        * MAX_BEND_PIXELS
                        * distFromPivot
                        * amplitudeMultiplier;

                // --- NEW: Y-Axis Arc Length Compensation ---
                // Pulls the Y coordinate inward toward the pivot based on how aggressively it is bent on the X axis.
                // Math.signum ensures points above the pivot pull down, and points below the pivot pull up.
                float yCompensation = Math.abs(bendOffset) * LENGTH_COMPENSATION_FACTOR * Math.signum(-distFromPivot);
                float localY = (length * fraction) - centerY + ANCHOR_Y_OFFSET + yCompensation;
                // -------------------------------------------

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