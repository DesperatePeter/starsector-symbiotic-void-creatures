@file:Suppress("UNCHECKED_CAST")

package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.getRandomPointOnShipOutline
import tecrys.svc.utils.times

class SinGunEffectPlayer(private val weapon: WeaponAPI) : BaseEveryFrameCombatPlugin() {
    companion object {
        const val SOUND_ID = "svc_emp"
        const val PRESENCE_KEY = "\$svc_singun_soundplayer_present"
        const val PITCH = 1f
        const val VOLUME = 0.5f

        fun isSoundPlayerAlreadyPresent(weapon: WeaponAPI): Boolean {
            return (weapon.ship.customData?.get(PRESENCE_KEY) as? MutableSet<*>)?.contains(weapon) == true
        }

        fun markAsPresent(weapon: WeaponAPI) {

            if (weapon.ship.customData.containsKey(PRESENCE_KEY)) {
                val weaponSet = weapon.ship.customData[PRESENCE_KEY] as MutableSet<WeaponAPI>
                weaponSet.add(weapon)
                weapon.ship.setCustomData(PRESENCE_KEY, weaponSet)
            } else {
                weapon.ship.setCustomData(PRESENCE_KEY, mutableSetOf(weapon))
            }
        }
    }

    init {
        markAsPresent(weapon)
    }

    private val chargeUpInterval = IntervalUtil(0.01f, 0.05f)

    fun spawnChargeUpArc(){
        Global.getCombatEngine().spawnEmpArcVisual(weapon.location, weapon.ship,
            weapon.ship.getRandomPointOnShipOutline(), weapon.ship,
            SinGunProjectileScript.EMP_ARC_THICKNESS_MULT * 5f,
            SinGunProjectileScript.ARC_GLOW_COLOR,
            SinGunProjectileScript.ARC_COLOR,
            SinGunProjectileScript.createEmpParams())
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if (Global.getCombatEngine().isPaused) return

        // charging up, not down
        if(weapon.cooldownRemaining < 0.01f && weapon.chargeLevel > 0.01f && weapon.chargeLevel < 0.8f){
            chargeUpInterval.advance(amount)
            if(chargeUpInterval.intervalElapsed()) spawnChargeUpArc()
        }

        if (weapon.chargeLevel > 0.8f) {
            Global.getSoundPlayer()
                ?.playLoop("svc_beam", weapon, PITCH, VOLUME, weapon.location, weapon.ship?.velocity, 0.1f, 0.5f)

        }
    }
}