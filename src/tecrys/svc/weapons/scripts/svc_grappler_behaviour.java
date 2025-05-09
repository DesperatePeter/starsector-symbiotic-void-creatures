package tecrys.svc.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import tecrys.svc.plugins.GrapplerRopePlugin;

import java.awt.*;

public class svc_grappler_behaviour implements OnFireEffectPlugin, OnHitEffectPlugin {

    private final Color tentacleColor = new Color(173, 113, 156, 255);
    private static final float BASE_FORCE = 20000f;
    private static final float FORCE_PER_DISTANCE = 4f;
    private static final float ESCAPE_VELOCITY_SCALING = 0.011f;
    public static int pluginCount = 0;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        GrapplerRopePlugin plugin = new GrapplerRopePlugin(pluginCount++, 15, 8f, false, weapon, projectile);
        plugin.setBaseColor(tentacleColor);
        plugin.setSegmentLength(15f);

        projectile.setCustomData("grappler", plugin);
        Global.getCombatEngine().addPlugin(plugin);

    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        Global.getLogger(this.getClass()).info("target: " + target.getClass());
        GrapplerRopePlugin plugin = (GrapplerRopePlugin) projectile.getCustomData().get("grappler");
        if (target instanceof DamagingProjectileAPI) {
            plugin.kill();
        } else {
            plugin.attach(target, point, projectile.getFacing());
        }
        if (projectile.getSource() != null && target instanceof ShipAPI targetShip) {
            ShipAPI sourceShip = projectile.getSource();

            Vector2f sourceLoc = sourceShip.getLocation();
            Vector2f targetLoc = targetShip.getLocation();

            Vector2f pullDirection = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(sourceLoc, targetLoc));
            float distance = MathUtils.getDistance(targetLoc, sourceLoc);

            float escapeVelocity = Vector2f.dot(pullDirection, targetShip.getVelocity());

            float force = BASE_FORCE + distance * FORCE_PER_DISTANCE;
            float deltaV = force / sourceShip.getMass()  * Math.max(escapeVelocity * ESCAPE_VELOCITY_SCALING, 1f);

            sourceShip.getVelocity().translate(pullDirection.x * deltaV, pullDirection.y * deltaV);
        }
    }
}
