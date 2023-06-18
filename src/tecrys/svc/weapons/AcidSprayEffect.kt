package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.plus
import tecrys.svc.utils.vectorFromAngleDeg
import tecrys.svc.weapons.scripts.AcidSprayScript
import java.awt.Color

class AcidSprayEffect: OnFireEffectPlugin {
    companion object{
        private const val EFFECT_DURATION = 2f
        private const val INITIAL_EFFECT_RADIUS = 2f
        private const val EFFECT_RADIUS_GROWTH = 50f
        private const val EFFECT_SPEED = 200f
        private val EFFECT_COLOR = Color(50, 200, 0, 50)
    }
    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        val w = weapon ?: return
        val e = engine ?: return
        val effectOffset = vectorFromAngleDeg(w.currAngle)
        effectOffset.scale(INITIAL_EFFECT_RADIUS)
        val effectLocation = w.location + effectOffset
        effectOffset.scale(EFFECT_SPEED / INITIAL_EFFECT_RADIUS)
        val velocity = w.ship.velocity + effectOffset
        e.addPlugin(AcidSprayScript(w.ship, e, effectLocation, velocity, EFFECT_COLOR, INITIAL_EFFECT_RADIUS, EFFECT_DURATION, EFFECT_RADIUS_GROWTH))
    }
}