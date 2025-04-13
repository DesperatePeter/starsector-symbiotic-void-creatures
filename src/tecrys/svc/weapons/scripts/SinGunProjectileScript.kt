package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.times
import kotlin.math.sin

class SinGunProjectileScript(projs: List<DamagingProjectileAPI>, weaponAngle: Float) : BaseEveryFrameCombatPlugin() {
    companion object {
        const val PHASE_TIME_MULT = 1.5f
        const val INNER_PM = 3f
        const val OUTER_PM = 2f
        const val OUTER_AMPLITUDE = 500f
        const val INNER_AMPLITUDE = 1500f
        const val DIST_SCALE_CONST = 100f
        const val MULT_NAME = "SvcSinGunDist"
        fun computeDamageMult(distance: Float): Float{
            return (2f - distance/ DIST_SCALE_CONST).coerceIn(0f, 2f)
        }
    }

    init {
        if (projs.size != 4) {
            Global.getLogger(this.javaClass)
                .error("Spawned SinGunScript with a number of projectiles != 4. Game might crash!")
        }
    }

    private val orthVector = Misc.getUnitVectorAtDegreeAngle(weaponAngle + 90f)
    private val outermostProjectile = projs.maxBy { p1 ->
        projs.maxOfOrNull { (it.location - p1.location) * orthVector } ?: 0f
    }
    private val projectiles = projs.sortedBy { p ->
        (p.location - outermostProjectile.location) * orthVector
    }
    // FIXME: Are outer and inner projectiles swapped???
    private val outerProjectiles = Pair(projectiles.first(), projectiles.last())
    private val innerProjectiles = Pair(projectiles[1], projectiles[2])
    private var phase = 3f
    private var hasFirstFrameBeenSkipped = false


    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val engine = Global.getCombatEngine() ?: return
        if(engine.isPaused) return
        if(!hasFirstFrameBeenSkipped){
            hasFirstFrameBeenSkipped = true
            return
        }
        phase += amount * PHASE_TIME_MULT
        adjustProjectileVelocity(amount)
        listOf(innerProjectiles, outerProjectiles).forEach {
            modifyProjectileDamage(it)
        }
        if(isDone()) engine.removePlugin(this)
    }

    private fun adjustProjectileVelocity(amount: Float) {
        mapOf(
            outerProjectiles.first to Pair(OUTER_PM, OUTER_AMPLITUDE),
            outerProjectiles.second to Pair(OUTER_PM, -OUTER_AMPLITUDE),
            innerProjectiles.first to Pair(INNER_PM, INNER_AMPLITUDE),
            innerProjectiles.second to Pair(INNER_PM, -INNER_AMPLITUDE)
        ).forEach { (projectile, pmAndAmp) -> // why can't you decompose inner pairs? :(
            val magnitude = sin(phase * pmAndAmp.first) * pmAndAmp.second * amount
            val deltaV = Vector2f(orthVector.x * magnitude, orthVector.y * magnitude)
            Vector2f.add(projectile.velocity, deltaV, projectile.velocity)
        }
        projectiles.forEach {
            it.facing = Misc.getAngleInDegrees(it.velocity)
        }
    }

    private fun modifyProjectileDamage(projectilePair: Pair<DamagingProjectileAPI, DamagingProjectileAPI>){
        val dist = (projectilePair.first.location - projectilePair.second.location).length()
        val mult = computeDamageMult(dist)
        listOf(projectilePair.first, projectilePair.second).forEach {
            it.damage.modifier.modifyMult(MULT_NAME, mult)
            // TODO visible indicator for damage mult
        }
    }

    private fun isDone(): Boolean{
        return projectiles.all { it.isFading || it.isExpired }
    }
}