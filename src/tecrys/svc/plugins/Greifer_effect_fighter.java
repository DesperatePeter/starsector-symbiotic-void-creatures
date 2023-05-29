package tecrys.svc.plugins;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class Greifer_effect_fighter implements BeamEffectPlugin
{
    private IntervalUtil tracker = new IntervalUtil(0.1f, 0.2f);
    
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
                    float force = 0f;
                    Vector2f dir;

                    if(target instanceof ShipAPI)
                    {
                        ShipAPI ship = (ShipAPI) target;
                        ShipAPI source = beam.getSource();
                        ShipSystemAPI cloak = ship.getPhaseCloak();
                        if (cloak != null && cloak.isActive())
                        {
                                return;
                        }

                        if(ship != beam.getSource())
                        {
                            force = 60 - (source.getMass() /125);				
							
                            dir = (Vector2f) VectorUtils.getDirectionalVector(beam.getSource().getLocation(), ship.getLocation()).scale(force);
                            Vector2f.add(source.getVelocity(), dir, source.getVelocity());
                        }
                    }
                    else
                    {
                         ShipAPI source = beam.getSource();
                         if(target != beam.getSource())
                        {
                            force = 60 - (source.getMass() /125);
                            dir = (Vector2f) VectorUtils.getDirectionalVector(beam.getSource().getLocation(), target.getLocation()).scale(force);
                            Vector2f.add(source.getVelocity(), dir, source.getVelocity());     
                        }
                    }
                }
            }
        }
    }	
}