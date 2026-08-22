package tecrys.svc.weapons.scripts;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;
import tecrys.svc.plugins.GrapplerRopePlugin;
import tecrys.svc.plugins.svcGuidedProjectile;
import tecrys.svc.weapons.GreiferEffectBase;

import java.awt.Color;

public class svc_grappler_pd_behaviour extends GreiferEffectBase implements OnFireEffectPlugin, OnHitEffectPlugin {

    private final Color tentacleColor = new Color(183, 73, 89, 255);
    public static int pluginCount = 0;

    @Override
    public boolean shouldAffectFighters() { return true; }

    @Override
    public boolean shouldAffectShips() { return true; }

    @Override
    public boolean shouldAffectObjects() { return false; }

    @Override
    public float computeForceAgainstShip(ShipAPI target, ShipAPI source) {
        return Math.max(56f - source.getMass() / 18f, 0.01f);
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        GrapplerRopePlugin plugin = new GrapplerRopePlugin(pluginCount++, 15, 4f, true, weapon, projectile);
        plugin.setBaseColor(tentacleColor);
        plugin.setSegmentLength(5f);

        projectile.setCustomData("grappler_pd", plugin);
        engine.addPlugin(plugin);

        svcGuidedProjectile tentacleplugin = new svcGuidedProjectile(projectile, null);
        engine.addPlugin(tentacleplugin);
    }

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        GrapplerRopePlugin plugin = (GrapplerRopePlugin) projectile.getCustomData().get("grappler_pd");
        if (target instanceof DamagingProjectileAPI) {
            plugin.kill();
        } else {
            plugin.attach(target, point, projectile.getFacing());
        }
    }
}