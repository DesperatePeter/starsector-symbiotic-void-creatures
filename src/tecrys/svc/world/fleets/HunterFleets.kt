package tecrys.svc.world.fleets

import tecrys.svc.SVC_MOD_ID
import tecrys.svc.listeners.HuntersDefeatedListener
import tecrys.svc.utils.CampaignSettingDelegate

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

val smallHunterFleet = HunterFleetConfig(smallHunterFleetRolesQuantity,
    "small", "Void Razors", 80f, 75f, HuntersDefeatedListener("small"))

val mediumHunterFleet = HunterFleetConfig(mediumHunterFleetRolesQuantity,
    "medium", "Void Hunters", 130f, 100f, HuntersDefeatedListener("medium"))

val largeHunterFleet = HunterFleetConfig(largeHunterFleetRolesQuantity,
    "large", "Void Stalkers", 250f, 160f, HuntersDefeatedListener("large"))

val hunterFleetsById = mapOf(
    smallHunterFleet.id to smallHunterFleet,
    mediumHunterFleet.id to mediumHunterFleet,
    largeHunterFleet.id to largeHunterFleet
)

var hunterFleetsToSpawn: MutableMap<String, HunterFleetConfig>
by CampaignSettingDelegate("$" + SVC_MOD_ID + "hunterFleetsToSpawn", hunterFleetsById.toMutableMap())

val hunterFleetsThatCanSpawn: Map<String, HunterFleetConfig>
    get() = hunterFleetsToSpawn.filter {
        val svcParams = FleetSpawnParameterCalculator(svcSettings)
        svcParams.spawnPower >= it.value.minSpawnPower
    }