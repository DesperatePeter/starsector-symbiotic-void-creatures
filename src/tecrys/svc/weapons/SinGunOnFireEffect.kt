package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import tecrys.svc.weapons.scripts.SinGunProjectileScript
import tecrys.svc.weapons.scripts.SinGunEffectPlayer

class SinGunOnFireEffect: OnFireEffectPlugin {
    private var looseProjectilesByWeapon = mutableMapOf<WeaponAPI, MutableList<DamagingProjectileAPI>>()
    override fun onFire(
        projectile: DamagingProjectileAPI?,
        weapon: WeaponAPI?,
        engine: CombatEngineAPI?
    ) {
        weapon ?: return
        projectile ?: return
        engine ?: return
        looseProjectilesByWeapon[weapon] ?: kotlin.run { looseProjectilesByWeapon[weapon] = mutableListOf() }
        looseProjectilesByWeapon[weapon]?.add(projectile)
        if((looseProjectilesByWeapon[weapon]?.size ?: 0) >= 4 ){
            engine.addPlugin(looseProjectilesByWeapon[weapon]?.let { SinGunProjectileScript(it, weapon.currAngle) })
            looseProjectilesByWeapon[weapon] = mutableListOf()
        }
        if(!SinGunEffectPlayer.isSoundPlayerAlreadyPresent(weapon)){
            engine.addPlugin(SinGunEffectPlayer(weapon))
        }
    }
}