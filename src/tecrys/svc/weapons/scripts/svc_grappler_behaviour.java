package tecrys.svc.weapons.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import tecrys.svc.plugins.GrapplerRopePlugin;
import org.lwjgl.util.vector.Vector2f;
import tecrys.svc.weapons.GreiferEffectBase;

import java.awt.*;

public class svc_grappler_behaviour extends GreiferEffectBase implements OnFireEffectPlugin, OnHitEffectPlugin {

    private Color tentacleColor = new Color(173, 113, 156, 255);
    private float pullStrength = 100f;
    public static int pluginCount = 0;
    @Override
    public boolean shouldAffectFighters() {
        return true;
    }

    @Override
    public boolean shouldAffectShips() {
        return true;
    }

    @Override
    public boolean shouldAffectObjects() {
        return false;
    }

    @Override
    public float computeForceAgainstShip(ShipAPI target, ShipAPI source) {
        return Math.max(106f + source.getMass() / 7f, 0.01f);
    }
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
        Global.getLogger(this.getClass()).info("target: " + target.getClass() );
        GrapplerRopePlugin plugin = (GrapplerRopePlugin) projectile.getCustomData().get("grappler");
        if (target instanceof DamagingProjectileAPI) {
            plugin.kill();
        }
        else {
            plugin.attach(target, point, projectile.getFacing());
        }
        if (projectile.getSource() instanceof ShipAPI && target instanceof ShipAPI) {
            ShipAPI sourceShip = (ShipAPI) projectile.getSource();
            ShipAPI targetShip = (ShipAPI) target;

            Vector2f sourceLoc = sourceShip.getLocation();
            Vector2f targetLoc = targetShip.getLocation();

            Vector2f pullVector = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees( sourceLoc, targetLoc));
            Vector2f pullEnemiesVector = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees( sourceLoc, targetLoc));

            pullVector.scale(Math.max(60f - sourceShip.getMass() / 20f, 40f));
            pullEnemiesVector.scale(( Math.max(56f - sourceShip.getMass() / 20f, 15f) ) * 0.6f);

            sourceShip.getVelocity().translate(pullVector.x, pullVector.y);
            targetShip.getVelocity().translate(-pullEnemiesVector.x, -pullEnemiesVector.y);
        }
    }

}
