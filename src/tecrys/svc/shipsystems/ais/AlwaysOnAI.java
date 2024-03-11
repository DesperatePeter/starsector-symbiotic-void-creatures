package tecrys.svc.shipsystems.ais;


import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

public class AlwaysOnAI implements ShipSystemAIScript {

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
            shouldUseSystem = AIUtils.canUseSystemThisFrame(ship) && !ship.getSystem().isOn() && !ship.getSystem().isCoolingDown();

            // If system is inactive and should be active, enable it
            if(shouldUseSystem == true)
            {
                //ship.getMouseTarget().set(MathUtils.getRandomPointInCircle(ship.getLocation(),2000f));
                ship.useSystem();
            }
        }
    }
}
