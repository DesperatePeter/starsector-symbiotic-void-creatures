package tecrys.svc.plugins

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.SVC_BASE_HULLMOD_ID
import tecrys.svc.WHALE_HULLMOD_ID
import java.awt.Color

class BloodEffect : BaseEveryFrameCombatPlugin() {
    companion object{
        private val magnitudeBySize = mapOf(
            WeaponSize.SMALL to 1f,
            WeaponSize.MEDIUM to 1.5f,
            WeaponSize.LARGE to 2.5f
        )

        private const val AVERAGE_SMOKE_SIZE = 7f
        private const val SMOKE_SIZE_VARIANCE = 5f
        private const val VELOCITY_MAGNITUDE = 10f
        private const val NUMBER_OF_PARTICLES = 6
        private const val PARTICLE_DURATION = 6f
        private const val PARTICLE_OPACITY = 0.3f
        private val PARTICLE_COLOR = Color(200, 0, 0, 200)
        private val PARTICLE_COLOR_WHALES = Color(60, 0, 180, 200)

        fun getRandomizedSmokeSize(weapon: WeaponAPI, multiplier: Float = 1f) : Float {
            return multiplier * (magnitudeBySize[weapon.size] ?: 1f) * (AVERAGE_SMOKE_SIZE + SMOKE_SIZE_VARIANCE * (Math.random() - 0.5f)).toFloat()
        }
        fun getRandomizedVelocity() : Vector2f {
            val toReturn = Vector2f(Math.random().toFloat() - 0.5f, Math.random().toFloat() - 0.5f)
            toReturn.normalise()
            toReturn.scale(VELOCITY_MAGNITUDE)
            return toReturn
        }
    }

    var engine: CombatEngineAPI? = null
    private val interval = IntervalUtil(0.1f, 0.3f)

    override fun init(engine: CombatEngineAPI?) {
        this.engine = engine
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val eng = this.engine ?: return
        if(eng.isPaused) return
        interval.advance(amount)
        if(!interval.intervalElapsed()) return
        eng.ships?.filter { it.variant.hasHullMod(SVC_BASE_HULLMOD_ID) }?.filterNotNull()?.forEach { ship ->
            spawnBlood(ship, PARTICLE_COLOR, eng)
        }
        eng.ships?.filter { it.variant.hasHullMod(WHALE_HULLMOD_ID) }?.filterNotNull()?.forEach { ship ->
            spawnBlood(ship, PARTICLE_COLOR_WHALES, eng)
        }
    }

    private fun spawnBlood(ship: ShipAPI, color: Color, eng: CombatEngineAPI){
        ship.allWeapons?.filterNotNull()?.filter {
            it.isDisabled || ship.isHulk || ship.hullLevel < 0.5f
        }?.forEach { w ->
            for (i in 0 until NUMBER_OF_PARTICLES){
                eng.addSmokeParticle(w.location, getRandomizedVelocity(), getRandomizedSmokeSize(w, 1f - ship.hullLevel),
                    PARTICLE_OPACITY, PARTICLE_DURATION, color)
            }
        }
    }


}