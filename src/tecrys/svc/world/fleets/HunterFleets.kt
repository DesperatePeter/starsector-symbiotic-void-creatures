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
    "combatEliteSmall" to 1,
    "combatEliteSmallEvolved" to 1,
    "combatEliteMedium" to 3,
    "combatEliteMediumEvolved" to 2,
    "ScoliacMiniBoss" to 1,
)

val largeHunterFleetRolesQuantity = mapOf(
    "combaElitetLarge" to 2,
    "combaElitetLargeEvolved" to 2,
    "combatEliteSmall" to 1,
    "combatEliteSmallEvolved" to 1,
    "combatEliteMedium" to 3,
    "combatEliteMediumEvolved" to 3,
    "ScoliacMiniBoss" to 1,
)

val smallHunterFleet = HunterFleetConfig(smallHunterFleetRolesQuantity,
    "small", "Void Razors", 80f, 75f, HuntersDefeatedListener)

val mediumHunterFleet = HunterFleetConfig(mediumHunterFleetRolesQuantity,
    "medium", "Void Razors", 130f, 100f, HuntersDefeatedListener)

val largeHunterFleet = HunterFleetConfig(largeHunterFleetRolesQuantity,
    "large", "Void Razors", 250f, 140f, HuntersDefeatedListener)

var hunterFleetsToSpawn: MutableMap<String, HunterFleetConfig>
by CampaignSettingDelegate("$" + SVC_MOD_ID + "hunterFleetsToSpawn",
    mutableMapOf(
        smallHunterFleet.id to smallHunterFleet,
        mediumHunterFleet.id to mediumHunterFleet,
        largeHunterFleet.id to largeHunterFleet
    )
)