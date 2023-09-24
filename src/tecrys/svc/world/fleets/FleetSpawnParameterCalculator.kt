package tecrys.svc.world.fleets

import com.fs.starfarer.api.Global
import org.lazywizard.lazylib.MathUtils
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate
import kotlin.math.min

class FleetSpawnParameterCalculator(private val s: FleetSpawnParameterSettings) {
    companion object {
        private const val CYCLE_ZERO = 206
        var extraSpawnPower: Float by CampaignSettingDelegate("$" + SVC_MOD_ID + "extraSpawnPower", 0f)
    }

    private val combatRoleScaling = s.combatRoleFinalWeights.mapValues {
        (it.value - (s.combatRoleBaseWeights[it.key] ?: 0f)) / s.maxSpawnPower
    }

    /**
     * a number roughly in the magnitude of 10~1000
     */
    private val playerFleetStrength: Float
        get() = Global.getSector()?.playerFleet?.effectiveStrength ?: 0f
    private val campaignCyclesElapsed: Float
        get() = Global.getSector()?.clock?.cycle?.minus(CYCLE_ZERO)?.toFloat() ?: 0f
    val spawnPower: Float
        get() {
            return min(
                s.flatSpawnPower + playerFleetStrength * s.spawnPowerScalingByPlayerStrength
                        + campaignCyclesElapsed * s.spawnPowerScalingByCycles
                        + extraSpawnPower,
                s.maxSpawnPower
            )
        }

    private val randomizedSpawnPower: Float
        get() = (0.75f + 0.5f * Math.random().toFloat()) * spawnPower

    val fleetSize: Float
        get() = MathUtils.clamp(randomizedSpawnPower * s.fleetSizeScaling, s.minFleetSize, s.maxFleetSize)


    val maxFleetCount: Int
        get() {
            if (Global.getSettings().isDevMode) return 1000
            return s.baseMaxFleetCount + ((spawnPower / s.maxSpawnPower) * (s.finalMaxFleetCount - s.baseMaxFleetCount)).toInt()
        }
    val combatRole: String
        get() {
            val combatRoleWeights = s.combatRoleBaseWeights.mapValues {
                it.value + (combatRoleScaling[it.key] ?: 0f) * randomizedSpawnPower
            }.filter { it.value > 0f }
            return getWeightedRandom(combatRoleWeights)
        }


    private fun <T> getWeightedRandom(itemsWithWeights: Map<T, Float>): T {
        val total = itemsWithWeights.values.sum()
        var diceRoll = Math.random() * total
        itemsWithWeights.forEach {
            diceRoll -= it.value
            if (diceRoll <= 0f) return it.key
        }
        return itemsWithWeights.keys.last()
    }

    fun logParameters() {
        var logString = ""

        logString += "\n\n#####################################" +
                "\n---------SVC FLEET SPAWN INFO--------" +
                "\nplayer fleet strength: $playerFleetStrength" +
                "\ncycles elapsed: $campaignCyclesElapsed" +
                "\nspawn power: $spawnPower (max ${s.maxSpawnPower})" +
                "\nsample fleet size: $fleetSize" +
                "\nmax fleet count: $maxFleetCount" +
                "\nnumber of spawned fleets: ${FleetSpawner.countFactionFleets("svc")}" +
                "\n\nFLEET LOCATIONS" +
                "\nCoordinates: Star/Center has coordinates 0,0. positive x => right, positive y => above"

        FleetSpawner.getFactionFleets("svc").forEach {
            logString += "\n${it.containingLocation.name}(x = ${it.location.x}, y = ${it.location.y})"
        }
        logString += "\n-------END OF FLEET SPAWN INFO-------" +
                "\n#####################################\n"

        Global.getLogger(this::class.java).info(logString)
    }

}