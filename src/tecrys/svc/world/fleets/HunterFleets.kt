package tecrys.svc.world.fleets

import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.listeners.HuntersDefeatedListener
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.getHunterIdIfHunter

val smallHunterFleetRolesQuantity = mapOf(
    "combatEliteSmall" to 3,
    "combatEliteSmallEvolved" to 2,
    "combatEliteMedium" to 1,
    "combatEliteMediumEvolved" to 1,
)

val mediumHunterFleetRolesQuantity = mapOf(
    "combatEliteSmall" to 2,
    "combatEliteSmallEvolved" to 2,
    "combatEliteMedium" to 4,
    "combatEliteMediumEvolved" to 3,
    "ScoliacMiniBoss" to 1,
)

val largeHunterFleetRolesQuantity = mapOf(
    "combatEliteLarge" to 2,
    "combatEliteLargeEvolved" to 3,
    "combatEliteSmall" to 2,
    "combatEliteSmallEvolved" to 2,
    "combatEliteMedium" to 4,
    "combatEliteMediumEvolved" to 4,
    "ScoliacMiniBoss" to 1,
)

val mastermindFleetQuantity = mapOf(
    "combatEliteLarge" to 2,
    "combatEliteLargeEvolved" to 3,
    "combatEliteSmall" to 2,
    "combatEliteSmallEvolved" to 2,
    "combatEliteMedium" to 4,
    "combatEliteMediumEvolved" to 4,
    "mastermind" to 1,
)

val smallHunterFleet = HunterFleetConfig(smallHunterFleetRolesQuantity,
    "small", "Void Razors", 80f, 75f, HuntersDefeatedListener("small"))

val mediumHunterFleet = HunterFleetConfig(mediumHunterFleetRolesQuantity,
    "medium", "Void Hunters", 130f, 100f, HuntersDefeatedListener("medium"))

val largeHunterFleet = HunterFleetConfig(largeHunterFleetRolesQuantity,
    "large", "Void Stalkers", 250f, 160f, HuntersDefeatedListener("large"))

const val MASTERMIND_FLEET_MEMKEY = "\$SVC_MASTERMIND_FLEET"
val mastermindFleet = HunterFleetConfig(mastermindFleetQuantity,
    "mastermind", "Overlord", 250f, 0f, )
    // "mastermind", "TODO", 10f, 0f, )

val hunterFleets = listOf(smallHunterFleet, mediumHunterFleet, largeHunterFleet)

var hunterFleetsThatHaveBeenDefeated by CampaignSettingDelegate("$${SVC_MOD_ID}defeatedHunters", mutableSetOf<String>())

val hunterFleetsThatCanSpawn: List<HunterFleetConfig>
    get() = hunterFleets.filterNot {
        it.id in hunterFleetsThatHaveBeenDefeated
    }.filter {
        val svcParams = FleetSpawnParameterCalculator(svcSettings)
        svcParams.spawnPower >= it.minSpawnPower
    }.filterNot {
        FleetSpawner.getFactionFleets(SVC_FACTION_ID).any { svcFleet ->
            svcFleet.getHunterIdIfHunter() == it.id
        }
    }