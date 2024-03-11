package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnFireEffectPlugin
import com.fs.starfarer.api.combat.WeaponAPI
import org.lazywizard.lazylib.ext.plus
import tecrys.svc.utils.times
import tecrys.svc.utils.vectorFromAngleDeg
import tecrys.svc.weapons.scripts.AcidSprayScript
import java.awt.Color

abstract class AcidSprayEffectImpl : OnFireEffectPlugin {
    abstract val effectDuration: Float
    abstract val initialEffectRadius: Float
    abstract val effectRadiusGrowth: Float
    abstract val effectSpeed: Float
    abstract val effectColor: Color

    override fun onFire(projectile: DamagingProjectileAPI?, weapon: WeaponAPI?, engine: CombatEngineAPI?) {
        val w = weapon ?: return
        val e = engine ?: return
        val effectOffset = vectorFromAngleDeg(w.currAngle)
        effectOffset.scale(initialEffectRadius)
        val effectLocation = w.location + effectOffset
        val velocity = vectorFromAngleDeg(w.currAngle)
        val linearVelocity = w.ship.velocity * velocity
        velocity.scale(effectSpeed + linearVelocity)
        e.addPlugin(AcidSprayScript(w.ship, e, effectLocation, velocity, effectColor, initialEffectRadius, effectDuration, effectRadiusGrowth))
    }
}