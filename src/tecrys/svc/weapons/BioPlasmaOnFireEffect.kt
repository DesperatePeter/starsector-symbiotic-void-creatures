package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import tecrys.svc.weapons.scripts.LinkedProjectilesScript

class BioPlasmaOnFireEffect: OnFireEffectPlugin {

    private val looseProjectilesByWeapon = mutableMapOf<WeaponAPI, DamagingProjectileAPI>()

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        weapon ?: return
        projectile ?: return
        engine ?: return
        val oldProjectile = looseProjectilesByWeapon[weapon]
        oldProjectile?.let { oldProj ->
            engine.addPlugin(LinkedProjectilesScript(oldProj, projectile))
            looseProjectilesByWeapon.remove(weapon)
        } ?: run {
            looseProjectilesByWeapon[weapon] = projectile
        }
    }
}