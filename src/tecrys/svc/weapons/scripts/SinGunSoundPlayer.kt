@file:Suppress("UNCHECKED_CAST")

package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil

class SinGunSoundPlayer(private val weapon: WeaponAPI): BaseEveryFrameCombatPlugin() {
    companion object{
        const val SOUND_ID = "svc_emp"
        const val PRESENCE_KEY = "\$svc_singun_soundplayer_present"
        const val PITCH = 1f
        const val VOLUME = 0.5f
        private val shotInterval = IntervalUtil(0.2f, 0.6f)
        private var start: Boolean = false
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
        if(Global.getCombatEngine().isPaused) return
//        if (weapon.isFiring){
            shotInterval.advance(amount)
//            if (shotInterval.intervalElapsed()) {
//                start = false
//                //Global.getSoundPlayer()?.playSound(SOUND_ID, PITCH, VOLUME, weapon.location, weapon.ship?.velocity)
//            }
//        }
//        if (weapon.isFiring && !start) {
//            Global.getSoundPlayer()?.playSound(SOUND_ID, PITCH, VOLUME, weapon.location, weapon.ship?.velocity)
//            start = true
//        }

//        if (weapon.isInBurst && shotInterval.intervalElapsed()){
//            Global.getSoundPlayer()?.playSound(SOUND_ID, PITCH, VOLUME, weapon.location, weapon.ship?.velocity)
//
//        }
        if (weapon.chargeLevel > 0.8f) {
            Global.getSoundPlayer()?.playLoop("svc_beam", weapon, PITCH, VOLUME, weapon.location, weapon.ship?.velocity, 0.1f, 0.5f)

        }

//        if (weapon.chargeLevel > 0 && weapon.cooldownRemaining != 0f){
//            Global.getSoundPlayer()?.playLoop("svc_chargeup", weapon, PITCH, VOLUME, weapon.location, weapon.ship?.velocity)
//        }
    }
}