package tecrys.svc.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class QuickStrafeSystemActivatorPlugin extends BaseEveryFrameCombatPlugin {
    public IntervalUtil shotInterval = new IntervalUtil(0.2f,0.2f);
    public float time;
    boolean start = false;
    private CombatEngineAPI engine;
    private Map<ShipAPI, StrafeData> shipDataMap = new HashMap<>();

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    @Override
    public void advance(float v, java.util.List events) {
        if (engine == null) return; {
        if (engine.isPaused()) return;
            ShipAPI player = Global.getCombatEngine().getPlayerShip();
        Iterator<ShipAPI> shipIterator = engine.getShips().iterator();
        while (shipIterator.hasNext()) {
            ShipAPI ship = shipIterator.next();
            if (ship.isAlive() && ship.getPhaseCloak() != null && ship.getPhaseCloak().getId().equals("svc_dash")) {
                StrafeData data = shipDataMap.get(ship);
                if (data == null) {
                    data = new StrafeData();
                    shipDataMap.put(ship, data);
                }
                updateShip(ship, data);

                    if ((!ship.getEngineController().isStrafingLeft() && !ship.getEngineController().isStrafingRight())
                            || ship.getEngineController().isAccelerating() || ship.getEngineController().isDecelerating()) {
                        ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                }


            }
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
            }            else if ((ship.getPhaseCloak().getCooldownRemaining() <= 0 && timeSinceLastStrafe > 60)
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

    private static class StrafeData {
        long lastStrafeTime = 0;
        boolean lastStrafeLeft = false;
        long activationThreshold = 250; // Milliseconds between strafes for activation
    }

}