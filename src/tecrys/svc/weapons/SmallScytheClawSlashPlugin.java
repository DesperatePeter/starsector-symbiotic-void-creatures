package tecrys.svc.weapons;

import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponEffectPluginWithInit;
import com.fs.starfarer.api.graphics.SpriteAPI;
import org.lwjgl.util.vector.Vector2f;

import tecrys.svc.utils.DecoUtils;

import java.awt.Color;
import java.util.EnumSet;

public class SmallScytheClawSlashPlugin extends BaseCombatLayeredRenderingPlugin implements EveryFrameWeaponEffectPlugin, OnFireEffectPlugin, WeaponEffectPluginWithInit {

    private enum State {
        RESTING,
        SLASHING,
        RETURNING
    }

    // ----------------------------------------------------------------------------------
    // BASE TEMPLATES (Static values to pull from during init)
    // ----------------------------------------------------------------------------------
    private static final float BASE_RETRACT_SPEED = 5f;
    private static final float BASE_STRIKE_CHASE_SPEED = 30f;
    private static final float BASE_FOLLOW_THROUGH_SPEED = 30f;

    private static final float BASE_CHARGE_ROTATION_ANGLE = -70f;
    private static final float BASE_SLASH_ROTATION_ANGLE = 20f;

    private static final float BASE_MIN_JOINT_DEFLECTION = 10f;
    private static final float BASE_MAX_JOINT_DEFLECTION = 160f;

    private static final float BASE_DYNAMIC_TIP_X_SHIFT = 10f;
    private static final float BASE_DYNAMIC_TIP_Y_SHIFT = -35f;

    // ----------------------------------------------------------------------------------
    // INSTANCE VARIABLES (Safe per-weapon properties)
    // ----------------------------------------------------------------------------------

    // Adjusted estimates for the smaller claw sprites
    private final Vector2f jointRed = new Vector2f(-3f, 3f);      // Base joint (Weapon Mount)
    private final Vector2f jointGreen = new Vector2f(8f, 22f);   // Elbow joint
    private final Vector2f jointBlue = new Vector2f(18f, 10f);   // Wrist joint
    private final Vector2f jointPink = new Vector2f(4f, 60f);   // Damage point / Scythe Tip

    private float retractSpeed;
    private float strikeChaseSpeed;
    private float followThroughSpeed;
    private float minJointDeflection;
    private float maxJointDeflection;
    private float dynamicTipXShift;
    private float dynamicTipYShift;
    private float chargeRotAngle;
    private float slashRotAngle;

    // ----------------------------------------------------------------------------------

    private static final Color BLANK = new Color(255, 255, 255, 0);
    private static final Color VISIBLE = new Color(255, 255, 255, 255);

    // OPTIMIZATION: Cache the layer set to prevent 12,000 object allocations per second
    private static final EnumSet<CombatEngineLayers> ACTIVE_LAYERS = EnumSet.of(CombatEngineLayers.STATION_WEAPONS_LAYER);

    private boolean setupComplete = false;
    private boolean initialized = false;
    private boolean isMirrored = false;

    private WeaponAPI weapon;
    private CombatEngineAPI engine;
    private DamagingProjectileAPI activeProj = null;

    private State state = State.RESTING;
    private float extensionRatio = 0f;
    private float returnProgress = 0f;

    // Kinematic Nodes (World Space)
    private final Vector2f p0 = new Vector2f();
    private final Vector2f p1 = new Vector2f();
    private final Vector2f p2 = new Vector2f();
    private final Vector2f p3 = new Vector2f();

    // Resting positions
    private final Vector2f p1_rest = new Vector2f();
    private final Vector2f p2_rest = new Vector2f();
    private final Vector2f p3_rest = new Vector2f();
    private final Vector2f currentIkTarget = new Vector2f();

    // Bone Lengths & Mathematics
    private float l1, l2, l3;
    private float jRedY, jGreenY, jBlueY;
    private float currentTipX, currentTipY;
    private float restElbowSign, restWristSign;

    private float maxReachDistance;
    private float projStartDist;

    // OPTIMIZATION: Pre-calculate static trigonometry
    private float bone1Alpha;
    private float bone2Alpha;

    @Override
    public void init(WeaponAPI weapon) {
        if (setupComplete) return; // Prevent double-inverting if init is called twice

        this.weapon = weapon;

        // Load non-directional magnitudes & bounds
        retractSpeed = BASE_RETRACT_SPEED;
        strikeChaseSpeed = BASE_STRIKE_CHASE_SPEED;
        followThroughSpeed = BASE_FOLLOW_THROUGH_SPEED;
        minJointDeflection = BASE_MIN_JOINT_DEFLECTION;
        maxJointDeflection = BASE_MAX_JOINT_DEFLECTION;

        // MIRRORING LOGIC (Runs immediately in the Refit Screen & Combat Setup)
        isMirrored = DecoUtils.isOnLeft(weapon);
        if (isMirrored) {
            // False means it mirrors sprites AND shooting/angle offsets
            DecoUtils.mirror(weapon, false);

            // Fix: Negate X coordinates! The sprite flipped left/right, not up/down.
            jointRed.x = -jointRed.x;
            jointGreen.x = -jointGreen.x;
            jointBlue.x = -jointBlue.x;
            jointPink.x = -jointPink.x;

            // Invert angles and X offsets
            chargeRotAngle = BASE_CHARGE_ROTATION_ANGLE * -1f;
            slashRotAngle = BASE_SLASH_ROTATION_ANGLE * -1f;
            dynamicTipXShift = BASE_DYNAMIC_TIP_X_SHIFT * -1f; // X shift inverts!
            dynamicTipYShift = BASE_DYNAMIC_TIP_Y_SHIFT;       // Y stays positive!
        } else {
            chargeRotAngle = BASE_CHARGE_ROTATION_ANGLE;
            slashRotAngle = BASE_SLASH_ROTATION_ANGLE;
            dynamicTipXShift = BASE_DYNAMIC_TIP_X_SHIFT;
            dynamicTipYShift = BASE_DYNAMIC_TIP_Y_SHIFT;
        }

        jRedY = jointRed.y;
        jGreenY = jointGreen.y;
        jBlueY = jointBlue.y;

        l1 = calculateDistance(jointRed.x, jRedY, jointGreen.x, jGreenY);
        l2 = calculateDistance(jointGreen.x, jGreenY, jointBlue.x, jBlueY);

        // Pre-calculate the static sprite angles to skip runtime atan2 math
        bone1Alpha = (float) Math.toDegrees(Math.atan2(jGreenY - jRedY, jointGreen.x - jointRed.x));
        bone2Alpha = (float) Math.toDegrees(Math.atan2(jBlueY - jGreenY, jointBlue.x - jointGreen.x));

        float baseTipY = jointPink.y;
        restElbowSign = Math.signum(getRelativeAngle(jointRed.x, jRedY, jointGreen.x, jGreenY, jointBlue.x, jBlueY));
        restWristSign = Math.signum(getRelativeAngle(jointGreen.x, jGreenY, jointBlue.x, jBlueY, jointPink.x, baseTipY));

        float maxTipX = jointPink.x + dynamicTipXShift;
        float maxTipY = jointPink.y + dynamicTipYShift;
        float maxL3 = calculateDistance(jointBlue.x, jBlueY, maxTipX, maxTipY);

        float a1 = 0f;
        float a2 = a1 + (minJointDeflection * restElbowSign);
        float a3 = a2 + (minJointDeflection * restWristSign);

        float rx = l1 + (float)Math.cos(Math.toRadians(a2)) * l2 + (float)Math.cos(Math.toRadians(a3)) * maxL3;
        float ry = (float)Math.sin(Math.toRadians(a2)) * l2 + (float)Math.sin(Math.toRadians(a3)) * maxL3;
        maxReachDistance = (float)Math.sqrt(rx * rx + ry * ry);

        setupComplete = true;
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon.getShip() == null || !engine.isEntityInPlay(weapon.getShip())) {
            return;
        }

        if (!initialized) {
            this.engine = engine;
            engine.addLayeredRenderingPlugin(this);
            initialized = true;
        }

        hideDefaultSprites();
        p0.set(weapon.getLocation());

        if (state == State.RESTING) {
            extensionRatio = 0f;
            returnProgress = 0f;
        }
        else if (state == State.SLASHING) {
            if (activeProj != null && !activeProj.isExpired() && engine.isEntityInPlay(activeProj)) {
                float projDist = calculateDistance(p0.x, p0.y, activeProj.getLocation().x, activeProj.getLocation().y);
                float travelRange = Math.max(10f, maxReachDistance - projStartDist);

                float targetExt = Math.max(0f, Math.min(1f, (projDist - projStartDist) / travelRange));
                float lerpedRatio = lerp(extensionRatio, targetExt, amount * strikeChaseSpeed);

                extensionRatio = Math.max(extensionRatio, lerpedRatio);
            } else {
                extensionRatio += amount * followThroughSpeed;
            }

            if (extensionRatio >= 0.99f) {
                extensionRatio = 1f;
                state = State.RETURNING;
                returnProgress = 0f;
            }
        }
        else if (state == State.RETURNING) {
            returnProgress += amount * retractSpeed;
            if (returnProgress >= 1f) {
                returnProgress = 1f;
                state = State.RESTING;
                extensionRatio = 0f;
            } else {
                float t = returnProgress * returnProgress * (3f - 2f * returnProgress);
                extensionRatio = 1f - t;
            }
        }

        float visualAngleOffset = 0f;
        if (state == State.RESTING && weapon.getChargeLevel() > 0 && weapon.getCooldownRemaining() <= 0f) {
            visualAngleOffset = chargeRotAngle * weapon.getChargeLevel();
        } else if (state != State.RESTING) {
            visualAngleOffset = slashRotAngle * extensionRatio;
        }
        float visualAimAngle = weapon.getCurrAngle() + visualAngleOffset;

        currentTipX = jointPink.x + (dynamicTipXShift * extensionRatio);
        currentTipY = jointPink.y + (dynamicTipYShift * extensionRatio);
        l3 = calculateDistance(jointBlue.x, jBlueY, currentTipX, currentTipY);

        float canvasRot = visualAimAngle - 90f;
        float rad = (float) Math.toRadians(canvasRot);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float redRotX = jointRed.x * cos - jRedY * sin;
        float redRotY = jointRed.x * sin + jRedY * cos;
        float cx = p0.x - redRotX;
        float cy = p0.y - redRotY;

        p1_rest.set(cx + (jointGreen.x * cos - jGreenY * sin), cy + (jointGreen.x * sin + jGreenY * cos));
        p2_rest.set(cx + (jointBlue.x * cos - jBlueY * sin), cy + (jointBlue.x * sin + jBlueY * cos));

        float aimRad = (float) Math.toRadians(visualAimAngle);
        float currentTargetDist = lerp(projStartDist, maxReachDistance, extensionRatio);
        currentIkTarget.set(p0.x + (float)Math.cos(aimRad) * currentTargetDist, p0.y + (float)Math.sin(aimRad) * currentTargetDist);

        if (state == State.RESTING) {
            p1.set(p1_rest);
            p2.set(p2_rest);
            p3.set(cx + (currentTipX * cos - currentTipY * sin), cy + (currentTipX * sin + currentTipY * cos));
        } else {
            runFABRIK(currentIkTarget);

            if (state == State.RETURNING) {
                float blend = returnProgress * returnProgress * (3f - 2f * returnProgress);
                p1.x = lerp(p1.x, p1_rest.x, blend);
                p1.y = lerp(p1.y, p1_rest.y, blend);
                p2.x = lerp(p2.x, p2_rest.x, blend);
                p2.y = lerp(p2.y, p2_rest.y, blend);

                enforceLength(p0, p1, l1);
                enforceLength(p1, p2, l2);
            }
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        this.activeProj = projectile;
        this.state = State.SLASHING;
        this.projStartDist = calculateDistance(weapon.getLocation().x, weapon.getLocation().y, projectile.getLocation().x, projectile.getLocation().y);
    }

    private void runFABRIK(Vector2f target) {
        for (int i = 0; i < 2; i++) {
            p3.set(target);
            moveTowards(p3, p2, l3, p2);
            moveTowards(p2, p1, l2, p1);

            moveTowards(p0, p1, l1, p1);

            moveTowards(p1, p2, l2, p2);
            enforceHingeLimit(p0, p1, p2, restElbowSign, l2);

            moveTowards(p2, p3, l3, p3);
            enforceHingeLimit(p1, p2, p3, restWristSign, l3);
        }
    }

    private float getRelativeAngle(float ax, float ay, float bx, float by, float cx, float cy) {
        float angleAB = (float) Math.toDegrees(Math.atan2(by - ay, bx - ax));
        float angleBC = (float) Math.toDegrees(Math.atan2(cy - by, cx - bx));
        return normalizeAngle(angleBC - angleAB);
    }

    private void enforceHingeLimit(Vector2f a, Vector2f b, Vector2f c, float expectedSign, float len) {
        float angleAB = (float) Math.toDegrees(Math.atan2(b.y - a.y, b.x - a.x));
        float angleBC = (float) Math.toDegrees(Math.atan2(c.y - b.y, c.x - b.x));
        float rel = normalizeAngle(angleBC - angleAB);

        float currentSign = Math.signum(rel);
        if (currentSign == 0) currentSign = expectedSign;

        float absRel = Math.abs(rel);

        if (currentSign != expectedSign) {
            absRel = minJointDeflection;
        } else {
            absRel = Math.max(minJointDeflection, Math.min(maxJointDeflection, absRel));
        }

        float targetAngle = (float) Math.toRadians(angleAB + (absRel * expectedSign));
        c.set(b.x + (float) Math.cos(targetAngle) * len, b.y + (float) Math.sin(targetAngle) * len);
    }

    private float normalizeAngle(float angle) {
        while (angle > 180f) angle -= 360f;
        while (angle <= -180f) angle += 360f;
        return angle;
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (weapon == null || weapon.getShip() == null || !engine.isEntityInPlay(weapon.getShip())) return;

        renderBoneNative(weapon.getUnderSpriteAPI(), p0, p1, jointRed.x, jRedY, bone1Alpha);

        float localDx3 = currentTipX - jointBlue.x;
        float localDy3 = currentTipY - jBlueY;
        float bone3Alpha = (float) Math.toDegrees(Math.atan2(localDy3, localDx3));
        renderBoneNative(weapon.getBarrelSpriteAPI(), p2, p3, jointBlue.x, jBlueY, bone3Alpha);

        renderBoneNative(weapon.getSprite(), p1, p2, jointGreen.x, jGreenY, bone2Alpha);
    }

    @Override
    public float getRenderRadius() { return 999999f; }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() { return ACTIVE_LAYERS; }

    @Override
    public boolean isExpired() { return weapon == null || weapon.getShip() == null || !engine.isEntityInPlay(weapon.getShip()); }

    private void hideDefaultSprites() {
        if (weapon.getUnderSpriteAPI() != null) weapon.getUnderSpriteAPI().setColor(BLANK);
        if (weapon.getSprite() != null) weapon.getSprite().setColor(BLANK);
        if (weapon.getBarrelSpriteAPI() != null) weapon.getBarrelSpriteAPI().setColor(BLANK);
    }

    private void renderBoneNative(SpriteAPI sprite, Vector2f pStart, Vector2f pEnd, float jStartX, float jStartY, float alpha) {
        if (sprite == null) return;

        float worldDx = pEnd.x - pStart.x;
        float worldDy = pEnd.y - pStart.y;
        float theta = (float) Math.toDegrees(Math.atan2(worldDy, worldDx));

        float rotation = theta - alpha;
        float rad = (float) Math.toRadians(rotation);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float rotOffsetX = jStartX * cos - jStartY * sin;
        float rotOffsetY = jStartX * sin + jStartY * cos;

        float centerWorldX = pStart.x - rotOffsetX;
        float centerWorldY = pStart.y - rotOffsetY;

        sprite.setColor(VISIBLE);
        sprite.setAngle(rotation);
        sprite.renderAtCenter(centerWorldX, centerWorldY);
        sprite.setColor(BLANK);
    }

    private void moveTowards(Vector2f anchor, Vector2f pull, float distMax, Vector2f out) {
        float dx = pull.x - anchor.x;
        float dy = pull.y - anchor.y;
        float len = Math.max((float) Math.sqrt(dx * dx + dy * dy), 0.0001f);
        out.set(anchor.x + (dx / len) * distMax, anchor.y + (dy / len) * distMax);
    }

    private void enforceLength(Vector2f anchor, Vector2f pull, float exactLen) {
        moveTowards(anchor, pull, exactLen, pull);
    }

    private float calculateDistance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }
}