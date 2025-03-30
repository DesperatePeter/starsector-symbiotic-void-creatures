package tecrys.svc.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class Brainhelix implements OnFireEffectPlugin {

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        float angle = engine.getTotalElapsedTime(false) * 2f; // Simplified speed

        float x = 10f * (float) Math.cos(angle);
        float y = 10f * (float) Math.sin(angle);

        Vector2f offset = new Vector2f(x, y);
        Vector2f.add(projectile.getVelocity(), offset, projectile.getVelocity());
    }
}