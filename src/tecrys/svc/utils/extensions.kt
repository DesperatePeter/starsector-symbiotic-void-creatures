package tecrys.svc.utils

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.findNearestPlanetTo
import org.magiclib.kotlin.getAngleDiff
import kotlin.math.abs

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

fun CampaignFleetAPI.makeAlwaysHostile(){
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true);
}

fun ShipSystemAPI.isUsable(): Boolean{
    return state == ShipSystemAPI.SystemState.IDLE && !isOutOfAmmo
}

fun ShipVariantAPI.removeDMods(){
    val variant = this
    val mods = hullMods.toList()
    mods.forEach {
        if(DModManager.getMod(it).hasTag(Tags.HULLMOD_DMOD)){
            DModManager.removeDMod(variant, it)
        }
    }
    // basically copy-paste from hull restoration script
     if(variant.isDHull || variant.hullSpec.isDHull || variant.displayName.contains("(D)")){
        var base = variant.hullSpec.dParentHull
        if(!variant.hullSpec.isDefaultDHull && !variant.hullSpec.isRestoreToBase){
            base = variant.hullSpec
        }
        if(base == null && variant.hullSpec.isRestoreToBase){
            base = variant.hullSpec.baseHull
        }
        base?.let {
            variant.setHullSpecAPI(it)
        }
    }
}

fun ShipAPI.orientTowards(targetFacing: Float, maxDelta: Float){
    val normalizedTargetFacing = Misc.normalizeAngle(targetFacing)
    if(normalizedTargetFacing.getAngleDiff(facing) < maxDelta){
        facing = targetFacing
        return
    }
    val otherWayShorter = abs(normalizedTargetFacing - facing) > 180f
    val positiveDirection = targetFacing > facing
    facing += maxDelta * otherWayShorter.toFloat() * positiveDirection.toFloat()
}

fun Boolean.toFloat(): Float{
    return if (this) 1f else -1f
}