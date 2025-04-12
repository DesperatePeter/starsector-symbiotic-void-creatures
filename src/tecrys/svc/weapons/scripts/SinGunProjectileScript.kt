package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import kotlin.math.sin

class SinGunProjectileScript(projs: List<DamagingProjectileAPI>, weaponAngle: Float) : BaseEveryFrameCombatPlugin() {
    companion object {
        const val PHASE_TIME_MULT = 1f
        const val INNER_PM = 3f
        const val OUTER_PM = 2f
        const val OUTER_AMPLITUDE = 200f
        const val INNER_AMPLITUDE = 100f
        const val MULT_NAME = "SvcSinGunDist"
        fun computeDamageMult(amplitude: Float, distance: Float): Float{
            return (2f - distance/amplitude).coerceIn(0f, 2f)
        }
    }

    init {
        if (projs.size != 4) {
            Global.getLogger(this.javaClass)
                .error("Spawned SinGunScript with a number of projectiles != 4. Game might crash!")
        }
    }

    private val outermostProjectile = projs.maxBy { p1 ->
        projs.maxOfOrNull { (it.location - p1.location).length() } ?: 0f
    }
    private val projectiles = projs.sortedBy { p ->
        (p.location - outermostProjectile.location).length()
    }
    private val outerProjectiles = Pair(projectiles.first(), projectiles.last())
    private val innerProjectiles = Pair(projectiles[1], projectiles[2])
    private val orthVector = Misc.getUnitVectorAtDegreeAngle(weaponAngle + 90f)
    private var phase = 0f


    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        phase += amount * PHASE_TIME_MULT
        mapOf(
            outerProjectiles.first to Pair(OUTER_PM, OUTER_AMPLITUDE),
            outerProjectiles.second to Pair(OUTER_PM, -OUTER_AMPLITUDE),
            innerProjectiles.first to Pair(INNER_PM, INNER_AMPLITUDE),
            innerProjectiles.second to Pair(INNER_PM, -INNER_AMPLITUDE)
        ).forEach { (projectile, pmAndAmp) -> // why can't you decompose inner pairs? :(
            val magnitude = sin(phase * pmAndAmp.first) * pmAndAmp.second
            projectile.velocity.set(
                projectile.velocity.x + orthVector.x * magnitude,
                projectile.velocity.y + orthVector.y * magnitude
            )
        }
        val innerDist = (innerProjectiles.first.location - innerProjectiles.second.location).length()
        listOf(innerProjectiles.first, innerProjectiles.second).forEach {
            it.damage.modifier.modifyMult(MULT_NAME, computeDamageMult(INNER_AMPLITUDE, innerDist))
        }
        val outerDist = (outerProjectiles.first.location - outerProjectiles.second.location).length()
        listOf(outerProjectiles.first, outerProjectiles.second).forEach {
            it.damage.modifier.modifyMult(MULT_NAME, computeDamageMult(OUTER_AMPLITUDE, outerDist))
        }
    }
}