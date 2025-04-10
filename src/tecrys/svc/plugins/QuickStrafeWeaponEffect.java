package tecrys.svc.plugins;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class QuickStrafeWeaponEffect implements EveryFrameWeaponEffectPlugin {

    private CombatEngineAPI engine;
    private ShipAPI ship; // Store the ship the weapon is on.
    private Map<ShipAPI, StrafeData> shipDataMap = new HashMap<>(); // Store data per-ship
    private WeaponAPI weapon; //Store the weapon.

    private IntervalUtil shotInterval = new IntervalUtil(0.05f, 0.05f);
    private float time;
    boolean start = false;


    private static class StrafeData {
        long lastStrafeTime = 0;
        boolean lastStrafeLeft = false;
        long activationThreshold = 250; // Milliseconds between strafes for activation
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        this.engine = engine;
        this.weapon = weapon; // Store the weapon.

        if (engine == null || engine.isPaused()) {
            return;
        }

        // Find the ship this weapon is mounted on.  This is CRUCIAL.
        if (this.ship == null) {
            this.ship = weapon.getShip();
            if (this.ship == null) {
                return; // Can't do anything if we don't know the ship.
            }
        }

        // Only apply to ships with the specific phase cloak.
        if (this.ship.isAlive() && this.ship.getPhaseCloak() != null && this.ship.getPhaseCloak().getId().equals("svc_dash")) {
            this.shotInterval.advance(amount);

             if ((ship.getPhaseCloak().getState() != ShipSystemAPI.SystemState.IDLE && shotInterval.intervalElapsed())) {



                tecrys.svc.utils.UtilsKt.renderCustomAfterimage(
                        ship,
                        new Color(255, 255, 255, 40),
                        0.6f


                );
            }
//                MagicRender.objectspace(
//                        ship.getSpriteAPI(),
//                        ship,
//                        new Vector2f(),
//                        new Vector2f(0,0),
//                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
//                        new Vector2f(),
//                        180f,
//                        0f,
//                        true,
//                        new Color(255, 255, 255, 255),
//                        false,
//                        0.001f,
//                        0.08f,
//                        0.2f,
//                        true);
//                float targetAngle = ship.getFacing();
//                MagicRender.singleframe(
//                        ship.getSpriteAPI(),
//                        ship.getLocation(),
//                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
//                        targetAngle,
//                        new Color(255, 255, 255, 255),
//                        false);
//              if (shotInterval.intervalElapsed())  {  SpriteAPI sprite = ship.getSpriteAPI();
//                float targetAngle = ship.getFacing();
//                MagicRender.battlespace(
//                        sprite,
//                        ship.getLocation(),
//                        new Vector2f(),
//                        new Vector2f(sprite.getWidth(), sprite.getHeight()),
//                        new Vector2f(),
//                        targetAngle,
//                        ship.getAngularVelocity(),
//                        new Color(255, 255, 255, 255),
//                        false,
//                        0.001f,
//                        0.08f,
//                        0.2f);
//            }

            if (ship.getPhaseCloak().getState() == ShipSystemAPI.SystemState.COOLDOWN){
                this.ship.giveCommand(ShipCommand.DECELERATE, null, 0);
            }

            StrafeData data = shipDataMap.get(this.ship);
            if (data == null) {
                data = new StrafeData();
                shipDataMap.put(this.ship, data);
            }
            updateShip(ship, data);

            if ((this.ship.getEngineController().isAccelerating() && this.ship.getEngineController().isAcceleratingBackwards())) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
            if ((!this.ship.getEngineController().isStrafingLeft() && !this.ship.getEngineController().isStrafingRight())) {
                this.ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            }
        }
    }

    private void updateShip(ShipAPI ship, StrafeData data) {
        ShipSystemAPI system = ship.getSystem();

        if (ship.getEngineController().isStrafingLeft()) {
            checkStrafe(ship, data, true);
        } else if (ship.getEngineController().isStrafingRight()) {
            checkStrafe(ship, data, false);
        }
    }

    private void checkStrafe(ShipAPI ship, StrafeData data, boolean strafeLeft) {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastStrafe = currentTime - data.lastStrafeTime;

        if (timeSinceLastStrafe <= data.activationThreshold && data.lastStrafeLeft == strafeLeft) {
            if (ship.getPhaseCloak().isActive()) {
                ship.getPhaseCloak().deactivate();

            } else if ((ship.getPhaseCloak().getCooldownRemaining() <= 0 && timeSinceLastStrafe > 60)
                    && strafeLeft == true) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
                start = true;
            } else if ((ship.getPhaseCloak().getCooldownRemaining() <= 0 && timeSinceLastStrafe > 60)
                    && strafeLeft == false) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
                ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
                start = true;
            }

            if (ship.getPhaseCloak().isChargedown() && strafeLeft == true) {
                ship.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            }
            if (ship.getPhaseCloak().isChargedown() && strafeLeft == false) {
                ship.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            }
        }

        data.lastStrafeTime = currentTime;
        data.lastStrafeLeft = strafeLeft;
    }
}