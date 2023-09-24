package tecrys.svc.world.fleets

import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate

val smallHunterFleetRolesQuantity = mapOf(
    "combatSmall" to 3,
    "combatMedium" to 1,
    "combatLarge" to 1,
)

val smallHunterFleet = HunterFleetConfig(smallHunterFleetRolesQuantity,
    "small", "Void Razors", 80f, 75f)

var hunterFleetsToSpawn: MutableMap<String, HunterFleetConfig>
by CampaignSettingDelegate("$" + SVC_MOD_ID + "hunterFleetsToSpawn",
    mutableMapOf(
        smallHunterFleet.id to smallHunterFleet
    )
)