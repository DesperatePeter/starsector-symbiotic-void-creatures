package tecrys.svc.world.fleets

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import org.magiclib.kotlin.findNearestPlanetTo

const val MAX_ORBIT_ASSIGNMENT_DURATION = 1e9f
const val MAX_GOTO_ASSIGNMENT_DURATION = 1000f
const val MAX_ATTACK_DURATION = 100f

fun CampaignFleetAPI.orbitClosestPlanet() {
    this.addAssignment(
        FleetAssignment.GO_TO_LOCATION,
        this.findNearestPlanetTo(requireGasGiant = false, allowStars = false),
        MAX_GOTO_ASSIGNMENT_DURATION
    )
    this.addAssignment(
        FleetAssignment.ORBIT_AGGRESSIVE,
        this.findNearestPlanetTo(requireGasGiant = false, allowStars = false),
        MAX_ORBIT_ASSIGNMENT_DURATION
    )
}

fun CampaignFleetAPI.attackFleet(opponent: CampaignFleetAPI) {
    this.addAssignment(
        FleetAssignment.INTERCEPT,
        opponent,
        MAX_ATTACK_DURATION
    )
}