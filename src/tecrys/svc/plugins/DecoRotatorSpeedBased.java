package tecrys.svc.plugins;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.lazywizard.lazylib.MathUtils;
import tecrys.svc.utils.DecoUtils;

// Original file with just isHulk check added

public class DecoRotatorSpeedBased implements EveryFrameWeaponEffectPlugin {

    private boolean isInitialized = false;

    // Settings
    private static final float BASE_ROTATION_SPEED = 10.0f; // Degrees per second at standstill
    private static final float SPEED_SCALAR = 1f;           // Extra rotation speed per unit of ship velocity

    // State
    private float currentDirection = 1f; // 1 for CW, -1 for CCW

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused()) return;
        if (weapon.getShip() == null || weapon.getShip().isHulk()) return;

        // 1. Initialization (Run once per weapon instance)
        if (!isInitialized) {
            init(weapon);
        }

        ShipAPI ship = weapon.getShip();

        // 2. Calculate Speeds
        float shipVelocity = ship.getVelocity().length();
        float animSpeed = BASE_ROTATION_SPEED + (shipVelocity * SPEED_SCALAR);

        // 3. Determine Center of the Arc (Absolute Angle)
        // The angle the weapon *would* face if it was pointing straight forward in its slot
        float arcCenterAbsolute = ship.getFacing() + weapon.getArcFacing();

        // 4. Calculate Current Relative Angle
        // We calculate the difference between where the weapon is pointing now vs the arc center.
        // This returns a value between -180 and 180.
        // Negative is Left (CCW), Positive is Right (CW) relative to the slot.
        float currentRelAngle = MathUtils.getShortestRotation(arcCenterAbsolute, weapon.getCurrAngle());

        // 5. Apply Rotation
        // We add to the relative angle.
        currentRelAngle += (currentDirection * animSpeed * amount);

        // 6. Handle Bounce/Limits
        float halfArc = weapon.getArc() / 2f;

        // If we exceed the arc limits, clamp and flip direction
        if (Math.abs(currentRelAngle) >= halfArc) {
            // Clamp to edge to prevent overshooting
            currentRelAngle = Math.signum(currentRelAngle) * halfArc;
            // Flip direction
            currentDirection = -currentDirection;
        }

        // 7. Apply back to Weapon (Convert Relative back to Absolute)
        weapon.setCurrAngle(arcCenterAbsolute + currentRelAngle);
    }

    public void init(WeaponAPI weapon) {
        // Handle Sprite Mirroring
        if (DecoUtils.isOnLeft(weapon) != DecoUtils.isFacingForward(weapon)) {
            DecoUtils.mirror(weapon, false);

            // Optional: Start left-side weapons spinning the opposite way for symmetry
            currentDirection = -1f;
        } else {
            currentDirection = 1f;
        }

        isInitialized = true;
    }
}