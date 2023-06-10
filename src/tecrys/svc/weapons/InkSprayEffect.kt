package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.vectorFromAngleDeg
import tecrys.svc.weapons.scripts.InkSprayScript
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class InkSprayEffect : OnFireEffectPlugin {
    companion object{
        const val EFFECT_DURATION = 6f
        const val EFFECT_RADIUS = 135f
        const val EFFECT_SPEED = 50f
    }
    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        val w = weapon ?: return
        val e = engine ?: return
        val effectOffset = vectorFromAngleDeg(w.currAngle)
        effectOffset.scale(w.range)
        val effectLocation = w.location + effectOffset
        effectOffset.scale(EFFECT_SPEED / w.range)
        val velocity = w.ship.velocity + effectOffset
        e.addPlugin(InkSprayScript(e, effectLocation, velocity, w.ship))
    }
}