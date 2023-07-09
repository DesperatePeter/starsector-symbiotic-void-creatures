package tecrys.svc.world

import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils
import kotlin.math.min

class FleetSpawnParameters {
    companion object{
        private const val MAX_SPAWN_POWER = 300f            // spawn power is a combination of time elapsed and player fleet strength. it's the basis for all other values
        private const val FLEET_STRENGTH_SCALING = 0.5f     // How quickly spawn power scales with player fleet strength
        private const val CYCLES_ELAPSED_SCALING = 10f      // how quickly spawn power scales with time elapsed
        private const val FLEET_SIZE_SCALING = 1f           // How quickly voidling fleet size scales with spawn power
        private const val MIN_FLEET_SIZE = 10f
        private const val MAX_FLEET_SIZE = 300f

        private const val CYCLE_ZERO = 206
        private const val BASE_MAX_FLEET_COUNT = 10
        private const val FINAL_MAX_FLEET_COUNT = 30

        // weights when spawn power is 0 (negative weights are the same as 0, but make things appear later)
        private val combatRoleBaseWeights = mapOf(
            "combatSmall" to 1f,
            "combatMedium" to 0.5f,
            "combatLarge" to 0.25f,
            "combatEliteSmall" to 0.01f,
            "combatEliteMedium" to -0.1f,
            "combatEliteLarge" to -0.15f,
            "combatEliteSmallEvolved" to -0.2f,
            "combatEliteMediumEvolved" to -0.1f,
            "combatEliteLargeEvolved" to -0.15f,
        )
        // weights when spawn power is max
        private val combatRoleFinalWeights = mapOf(
            "combatSmall" to 0.3f,
            "combatMedium" to 0.4f,
            "combatLarge" to 0.3f,
            "combatEliteSmall" to 0.5f,
            "combatEliteMedium" to 0.4f,
            "combatEliteLarge" to 0.2f,
            "combatEliteSmallEvolved" to 0.25f,
            "combatEliteMediumEvolved" to 0.1f,
            "combatEliteLargeEvolved" to 0.08f,
        )

        private val combatRoleScaling = combatRoleFinalWeights.mapValues {
            (it.value - (combatRoleBaseWeights[it.key] ?: 0f)) / MAX_SPAWN_POWER }

        /**
         * a number roughly in the magnitude of 10~1000
         */
        private val playerFleetStrength: Float
            get() = Global.getSector()?.playerFleet?.effectiveStrength ?: 0f
        private val campaignCyclesElapsed: Float
            get() = Global.getSector()?.clock?.cycle?.minus(CYCLE_ZERO)?.toFloat() ?: 0f
        private val spawnPower: Float
            get() {
                return min(
                    playerFleetStrength * FLEET_STRENGTH_SCALING + campaignCyclesElapsed * CYCLES_ELAPSED_SCALING,
                    MAX_SPAWN_POWER)
            }

        private val randomizedSpawnPower: Float
            get() = (0.75f + 0.5f * Math.random().toFloat()) * spawnPower

        val fleetSize: Float
            get() = MathUtils.clamp(randomizedSpawnPower * FLEET_SIZE_SCALING, MIN_FLEET_SIZE, MAX_FLEET_SIZE)


        val maxFleetCount: Int
            get() = BASE_MAX_FLEET_COUNT + ((spawnPower / MAX_SPAWN_POWER) * (FINAL_MAX_FLEET_COUNT - BASE_MAX_FLEET_COUNT)).toInt()
        val combatRole: String
            get() {
                val combatRoleWeights = combatRoleBaseWeights.mapValues {
                    it.value + (combatRoleScaling[it.key] ?: 0f) * randomizedSpawnPower
                }.filter { it.value > 0f }
                return getWeightedRandom(combatRoleWeights)
            }


        private fun<T> getWeightedRandom(itemsWithWeights: Map<T, Float>): T{
            val total = itemsWithWeights.values.sum()
            var diceRoll = Math.random() * total
            itemsWithWeights.forEach{
                diceRoll -= it.value
                if (diceRoll <= 0f) return it.key
            }
            return itemsWithWeights.keys.last()
        }

        fun logParameters(){
            val logString = "" +
                    "\n###################################" +
                    "\n---------FLEET SPAWN PARAMS--------" +
                    "\nplayer fleet strength: $playerFleetStrength" +
                    "\ncycles elapsed: $campaignCyclesElapsed" +
                    "\nspawn power: $spawnPower (max 300)" +
                    "\nsample fleet size: $fleetSize" +
                    "\nmax fleet count: $maxFleetCount" +
                    "\nnumber of spawned fleets: ${SVCFleetSpawner.countFactionFleets("svc")}"
            Global.getLogger(this::class.java).info(logString)
        }
    }


}