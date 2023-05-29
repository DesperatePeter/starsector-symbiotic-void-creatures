package tecrys.svc.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.MathUtils;

public class Greifer_effect_pd implements BeamEffectPlugin
{
    private final IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam)
    {
        tracker.advance(amount);
        
        CombatEntityAPI target = beam.getDamageTarget();
        //Do we have a valid Entity to effect?
        if (target != null)
        {
            //Yes!  Is it in range, and the beam's on?
            if (beam.getBrightness() >= 1f) 
            {
                if(tracker.intervalElapsed())
                {
                    float force;
                    Vector2f dir;

                    if(target instanceof ShipAPI)
                    {
					
                        ShipAPI ship = (ShipAPI) target;
                        ShipSystemAPI cloak = ship.getPhaseCloak();
                        if (cloak != null && cloak.isActive() || (ship.getHullSize() != HullSize.FIGHTER))
                        {
                                return;
                        }

                        if(ship != beam.getSource())
                        {
                            force = Math.max(1 / ship.getMass() * 4000f, 0.01f);				
							
                            dir = (Vector2f) VectorUtils.getDirectionalVector(beam.getSource().getLocation(), ship.getLocation()).scale(force);
                            Vector2f.add(ship.getVelocity(), dir, ship.getVelocity());
                        }
                    }
                    else
                    {
                        if(target != beam.getSource())
                        {
                            force = Math.max(1 / target.getMass() * 5000f, 0.5f);
                            dir = (Vector2f) VectorUtils.getDirectionalVector(beam.getSource().getLocation(), target.getLocation()).scale(force);
                            Vector2f.add(target.getVelocity(), dir, target.getVelocity());     
                        }
                    }
                }
            }
        }
    }	
}
