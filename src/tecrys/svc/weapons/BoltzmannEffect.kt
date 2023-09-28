package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.dem.DEMScript
import tecrys.svc.weapons.scripts.BoltzmannScript
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;


class BoltzmannEffect: OnFireEffectPlugin {
    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        val missile = projectile as? MissileAPI ?: return
        engine?.addPlugin(BoltzmannScript(missile))

        if (projectile !is MissileAPI) return

        var ship: ShipAPI? = null
        if (weapon != null) ship = weapon.ship
        if (ship == null) return

        val script = DEMScript(projectile, ship, weapon)
        Global.getCombatEngine().addPlugin(script)
    }
}