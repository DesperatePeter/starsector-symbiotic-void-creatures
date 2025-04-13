@file:Suppress("UNCHECKED_CAST")

package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI

class SinGunSoundPlayer(private val weapon: WeaponAPI): BaseEveryFrameCombatPlugin() {
    companion object{
        const val SOUND_ID = "svc_brain_loop"
        const val PRESENCE_KEY = "\$svc_singun_soundplayer_present"
        const val PITCH = 1f
        const val VOLUME = 1f
        fun isSoundPlayerAlreadyPresent(weapon: WeaponAPI): Boolean{
            return (weapon.ship.customData?.get(PRESENCE_KEY) as? MutableSet<*>)?.contains(weapon) == true
        }
        fun markAsPresent(weapon: WeaponAPI){
            if(weapon.ship.customData.containsKey(PRESENCE_KEY)){
                val weaponSet = weapon.ship.customData[PRESENCE_KEY] as MutableSet<WeaponAPI>
                weaponSet.add(weapon)
                weapon.ship.setCustomData(PRESENCE_KEY, weaponSet)
            }else{
                weapon.ship.setCustomData(PRESENCE_KEY, mutableSetOf(weapon))
            }
        }
    }

    init {
        markAsPresent(weapon)
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(weapon.isFiring){
            Global.getSoundPlayer()?.playLoop(SOUND_ID, weapon, PITCH, VOLUME, weapon.location, weapon.ship?.velocity)
        }
    }
}