@file:Suppress("UNCHECKED_CAST")

package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.getRandomPointOnShipOutline
import tecrys.svc.utils.times

class SinGunEffectPlayer() : EveryFrameWeaponEffectPlugin {
    companion object {
        const val SOUND_ID = "svc_emp"
        const val PITCH = 1f
        const val VOLUME = 0.7f
    }

    private val chargeUpInterval = IntervalUtil(0.01f, 0.03f)

    fun spawnChargeUpArc(weapon: WeaponAPI){
        Global.getCombatEngine().spawnEmpArcVisual(weapon.location, weapon.ship,
            weapon.ship.getRandomPointOnShipOutline(), weapon.ship,
            SinGunProjectileScript.EMP_ARC_THICKNESS_MULT * 5f,
            SinGunProjectileScript.ARC_COLOR,
            SinGunProjectileScript.ARC_GLOW_COLOR,
            SinGunProjectileScript.createEmpParams())
    }

    override fun advance(
        amount: Float,
        engine: CombatEngineAPI,
        weapon: WeaponAPI
    ) {
        if (Global.getCombatEngine().isPaused) return
        // charging up, not down
        if(weapon.cooldownRemaining < 0.01f && weapon.chargeLevel > 0.01f && weapon.chargeLevel <= 0.99f){
            chargeUpInterval.advance(amount)
            if(chargeUpInterval.intervalElapsed()) spawnChargeUpArc(weapon)
        }

        if (weapon.chargeLevel > 0.8f) {
            Global.getSoundPlayer()
                ?.playLoop("svc_beam", weapon, PITCH, VOLUME, weapon.location, weapon.ship?.velocity, 0.1f, 0.5f)

        }
    }
}