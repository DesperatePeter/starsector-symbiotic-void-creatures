package tecrys.svc.world.fleets

import com.fs.starfarer.api.campaign.listeners.FleetEventListener

data class HunterFleetConfig(
    val rolesQuantity: Map<String, Int>, // name of combat role and how often to spawn it
    val id: String, // internal name of the hunter fleet
    val name: String, // display name of the hunter fleet
    val minDP: Float, // fleet will be padded with ships as per SvcSpawnSettings until it has at least this much DP
    val minSpawnPower: Float, // hunter fleet can spawn once spawn power reaches at least this value
    val fleetListener: FleetEventListener? = null
)