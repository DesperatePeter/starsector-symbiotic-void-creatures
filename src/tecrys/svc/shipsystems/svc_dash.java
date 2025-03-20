package tecrys.svc.shipsystems;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;

public class svc_dash extends BaseShipSystemScript {

    public static Color IMAGE_COLOR = new Color(255,175,255,255);
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI) stats.getEntity();
        if (state == ShipSystemStatsScript.State.OUT || state == State.COOLDOWN) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
            stats.getMaxTurnRate().unmodify(id);
            stats.getDeceleration().modifyFlat(id, 10000000f);

        } else {
            stats.getMaxSpeed().modifyFlat(id, 100000f);
            stats.getAcceleration().modifyFlat(id, 100000f );
            stats.getDeceleration().modifyFlat(id, 10000000f);
            stats.getTurnAcceleration().modifyFlat(id, 30f );
            stats.getTurnAcceleration().modifyPercent(id, 200f );
            stats.getMaxTurnRate().modifyFlat(id, 15f);
            stats.getMaxTurnRate().modifyPercent(id, 100f);


        }

        if (stats.getEntity() instanceof ShipAPI) {

            String key = ship.getId() + "_" + id;
            Object test = Global.getCombatEngine().getCustomData().get(key);
            if (state == State.IN) {


            }
            if (state == State.ACTIVE) {

                float targetAngle = ship.getFacing();
                MagicRender.battlespace(
                        ship.getSpriteAPI(),
                        ship.getLocation(),
                        new Vector2f(),
                        new Vector2f(ship.getSpriteAPI().getWidth(), ship.getSpriteAPI().getHeight()),
                        new Vector2f(),
                        targetAngle,
                        ship.getAngularVelocity(),
                        new Color(255, 255, 255, 255),
                        false,
                        0.05f,
                        0.1f,
                        0.05f);


            }
            else {
                Global.getCombatEngine().getCustomData().remove(key);
            }
        }
    }
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData("improved maneuverability", false);
        } else if (index == 1) {
            return new StatusData("+50 top speed", false);
        }
        return null;
    }
}
