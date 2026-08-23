package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import java.awt.Color;

public class QuickStrafeWeaponEffect implements EveryFrameWeaponEffectPlugin {

    private CombatEngineAPI engine;
    private ShipAPI ship;

    // No HashMap needed! A weapon effect only belongs to one ship.
    private StrafeData strafeData = new StrafeData();

    private IntervalUtil shotInterval = new IntervalUtil(0.05f, 0.05f);
    boolean start = false;

    // Cache the color so we don't allocate it continuously
    private static final Color AFTERIMAGE_COLOR = new Color(255, 255, 255, 40);

    private static class StrafeData {
        float lastStrafeTime = 0f;
        boolean lastStrafeLeft = false;
        float activationThreshold = 0.25f; // 250ms converted to in-game seconds
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || engine.isPaused()) return;
        this.engine = engine;

        if (this.ship == null) {
            this.ship = weapon.getShip();
            if (this.ship == null) return;
        }

        ShipSystemAPI phaseCloak = this.ship.getPhaseCloak();
        if (!this.ship.isAlive() || phaseCloak == null) return;

        String cloakId = phaseCloak.getId();

        if (cloakId.equals("svc_biomemetism")) {
            if ((this.ship.getEngineController().isAccelerating() && this.ship.getEngineController().isAcceleratingBackwards())) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            if ((!this.ship.getEngineController().isStrafingLeft() && !this.ship.getEngineController().isStrafingRight())) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }

        if (cloakId.equals("svc_dash")) {
            this.shotInterval.advance(amount);

            if ((phaseCloak.getState() != ShipSystemAPI.SystemState.IDLE && shotInterval.intervalElapsed())) {
                tecrys.svc.utils.UtilsKt.renderCustomAfterimage(ship, AFTERIMAGE_COLOR, 0.6f);
            }

            if (phaseCloak.getState() == ShipSystemAPI.SystemState.COOLDOWN){
                this.ship.giveCommand(ShipCommand.DECELERATE, null, 0);
            }

            updateShip(ship, strafeData);

            if ((this.ship.getEngineController().isAccelerating() && this.ship.getEngineController().isAcceleratingBackwards())) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            if ((!this.ship.getEngineController().isStrafingLeft() && !this.ship.getEngineController().isStrafingRight())) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }
    }

    private void updateShip(ShipAPI ship, StrafeData data) {
        if (ship.getEngineController().isStrafingLeft()) {
            checkStrafe(ship, data, true);
        } else if (ship.getEngineController().isStrafingRight()) {
            checkStrafe(ship, data, false);
        }
    }

    private void checkStrafe(ShipAPI ship, StrafeData data, boolean strafeLeft) {
        // Use engine time, NOT real-world system time!
        float currentTime = engine.getTotalElapsedTime(false);
        float timeSinceLastStrafe = currentTime - data.lastStrafeTime;

        if (timeSinceLastStrafe <= data.activationThreshold && data.lastStrafeLeft == strafeLeft) {
            ShipSystemAPI phaseCloak = ship.getPhaseCloak();

            if (phaseCloak.isActive()) {
                phaseCloak.deactivate();
            } else if ((phaseCloak.getCooldownRemaining() <= 0 && timeSinceLastStrafe > 0.06f)) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                ship.giveCommand(strafeLeft ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT, null, 0);
                start = true;
            }

            if (phaseCloak.isChargedown()) {
                ship.giveCommand(strafeLeft ? ShipCommand.STRAFE_RIGHT : ShipCommand.STRAFE_LEFT, null, 0);
            }
        }

        data.lastStrafeTime = currentTime;
        data.lastStrafeLeft = strafeLeft;
    }
}