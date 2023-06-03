package tecrys.svc.plugins

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class BloodEffect : BaseEveryFrameCombatPlugin() {
    companion object{
        val magnitudeBySize = mapOf(
            WeaponSize.SMALL to 1f,
            WeaponSize.MEDIUM to 1.5f,
            WeaponSize.LARGE to 2.5f
        )

        private const val AVERAGE_SMOKE_SIZE = 10f
        private const val SMOKE_SIZE_VARIANCE = 5f
        private const val VELOCITY_MAGNITUDE = 10f
        private const val NUMBER_OF_PARTICLES = 3
        private const val PARTICLE_DURATION = 3f
        private const val PARTICLE_OPACITY = 0.3f
        private val PARTICLE_COLOR = Color(200, 0, 0, 200)

        fun getRandomizedSmokeSize(weapon: WeaponAPI) : Float {
            return (magnitudeBySize[weapon.size] ?: 1f) * (AVERAGE_SMOKE_SIZE + SMOKE_SIZE_VARIANCE * (Math.random() - 0.5f)).toFloat()
        }
        fun getRandomizedVelocity() : Vector2f {
            val toReturn = Vector2f(Math.random().toFloat() - 0.5f, Math.random().toFloat() - 0.5f)
            toReturn.normalise()
            toReturn.scale(VELOCITY_MAGNITUDE)
            return toReturn
        }
    }

    var engine: CombatEngineAPI? = null
    val interval = IntervalUtil(0.1f, 0.3f)

    override fun init(engine: CombatEngineAPI?) {
        this.engine = engine
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val eng = this.engine ?: return
        if(eng.isPaused) return
        interval.advance(amount)
        if(!interval.intervalElapsed()) return
        eng.ships?.filter { it.variant.hasHullMod("BGECarapace") }?.filterNotNull()?.forEach { ship ->
            ship.allWeapons?.filterNotNull()?.filter {
                it.isDisabled || ship.isHulk
            }?.forEach { w ->
                for (i in 0 until NUMBER_OF_PARTICLES){
                    eng.addSmokeParticle(w.location, getRandomizedVelocity(), getRandomizedSmokeSize(w),
                        PARTICLE_OPACITY, PARTICLE_DURATION, PARTICLE_COLOR)
                }
            }
        }
    }


}