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
    companion object {
        private val magnitudeBySize = mapOf(
            WeaponSize.SMALL to 1f,
            WeaponSize.MEDIUM to 1.5f,
            WeaponSize.LARGE to 2.5f
        )

        private const val AVERAGE_SMOKE_SIZE = 7f
        private const val SMOKE_SIZE_VARIANCE = 5f
        private const val VELOCITY_MAGNITUDE = 6f
        private const val NUMBER_OF_PARTICLES = 1
        private const val PARTICLE_DURATION = 8f
        private const val PARTICLE_OPACITY = 0.3f
        private val PARTICLE_COLOR = Color(200, 0, 0, 200)
        private val PARTICLE_COLOR_WHALES = Color(60, 0, 180, 200)

        // Cache a scratch vector to prevent constant Vector2f allocation
        private val scratchVelocity = Vector2f()

        fun getRandomizedSmokeSize(weapon: WeaponAPI, multiplier: Float = 1f): Float {
            val sizeMult = magnitudeBySize[weapon.size] ?: 1f
            return multiplier * sizeMult * (AVERAGE_SMOKE_SIZE + SMOKE_SIZE_VARIANCE * (Math.random() - 0.5f)).toFloat()
        }

        // Mutate the scratch vector instead of returning a new one
        fun randomizeVelocity(vec: Vector2f) {
            vec.set(Math.random().toFloat() - 0.5f, Math.random().toFloat() - 0.5f)
            if (vec.lengthSquared() > 0) {
                vec.normalise()
                vec.scale(VELOCITY_MAGNITUDE)
            }
        }
    }

    var engine: CombatEngineAPI? = null
    private val interval = IntervalUtil(0.2f, 0.5f)

    override fun init(engine: CombatEngineAPI?) {
        this.engine = engine
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        val eng = this.engine ?: return
        if (eng.isPaused) return

        interval.advance(amount)
        if (!interval.intervalElapsed()) return

        val ships = eng.ships
        val numShips = ships.size

        // Single pass over the engine's ship array
        for (i in 0 until numShips) {
            val ship = ships[i]
            val variant = ship.variant ?: continue

            // Assuming a ship only has one of these hullmods, an else-if saves a redundant check
            if (variant.hasHullMod(SVC_BASE_HULLMOD_ID)) {
                spawnBlood(ship, PARTICLE_COLOR, eng)
            } else if (variant.hasHullMod(WHALE_HULLMOD_ID)) {
                spawnBlood(ship, PARTICLE_COLOR_WHALES, eng)
            }
        }
    }

    private fun spawnBlood(ship: ShipAPI, color: Color, eng: CombatEngineAPI) {
        val weapons = ship.allWeapons ?: return
        val numWeapons = weapons.size

        // Cache ship-level stats outside the weapon loop
        val hullLevel = ship.hullLevel
        val isHulk = ship.isHulk
        val hullMult = 1f - hullLevel

        for (i in 0 until numWeapons) {
            val w = weapons[i] ?: continue

            if (w.isDisabled || isHulk || hullLevel < 0.5f) {
                for (j in 0 until NUMBER_OF_PARTICLES) {
                    randomizeVelocity(scratchVelocity)
                    eng.addSmokeParticle(
                        w.location,
                        scratchVelocity,
                        getRandomizedSmokeSize(w, hullMult),
                        PARTICLE_OPACITY,
                        PARTICLE_DURATION,
                        color
                    )
                }
            }
        }
    }
}