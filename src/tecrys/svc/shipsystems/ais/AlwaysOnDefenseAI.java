package tecrys.svc.shipsystems.ais;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class AlwaysOnDefenseAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;

    private boolean shouldUseSystem = false;
    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.15f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
    {
        if(engine == null) return;
        if(engine.isPaused()) return;
        tracker.advance(amount);
        if (tracker.intervalElapsed())
        {
            shouldUseSystem =  !ship.getPhaseCloak().isOn() && !ship.getPhaseCloak().isCoolingDown();

            // If system is inactive and should be active, enable it
            if(shouldUseSystem == true)
            {
                //ship.getMouseTarget().set(MathUtils.getRandomPointInCircle(ship.getLocation(),2000f));
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, null, 0);
            }
        }
    }
}
