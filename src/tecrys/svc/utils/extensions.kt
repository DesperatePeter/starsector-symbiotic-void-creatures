package tecrys.svc.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.findNearestJumpPointThatCouldBeExitedFrom
import org.magiclib.kotlin.findNearestPlanetTo
import org.magiclib.kotlin.getAngleDiff
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.MASTERMIND_FLEET_MEMKEY
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs

const val DAMAGE_SCAN_RANGE = 250f
const val MAX_ORBIT_ASSIGNMENT_DURATION = 1e9f
const val MAX_GOTO_ASSIGNMENT_DURATION = 1000f
const val MAX_ATTACK_DURATION = 100f

fun PlanetAPI.hasVolatiles(): Boolean{
    return hasCondition(Conditions.VOLATILES_TRACE) ||
           hasCondition(Conditions.VOLATILES_DIFFUSE) ||
           hasCondition(Conditions.VOLATILES_PLENTIFUL) ||
           hasCondition(Conditions.VOLATILES_ABUNDANT)
}

fun PlanetAPI.hasOrganics(): Boolean{
    return hasCondition(Conditions.ORGANICS_TRACE) ||
            hasCondition(Conditions.ORGANICS_COMMON) ||
            hasCondition(Conditions.ORGANICS_ABUNDANT) ||
            hasCondition(Conditions.ORGANICS_PLENTIFUL)
}

fun OptionPanelAPI.addLeaveOption(){
    addOption("Leave", "Leave")
    setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, false)
}
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

fun ShipAPI.estimateProjectileDamageToBeTaken(t: Float = 1f): Float{
    return CombatUtils.getProjectilesWithinRange(location, DAMAGE_SCAN_RANGE).filter {
        CollisionUtils.getCollides(it.location, it.location + t * it.velocity, location, collisionRadius)
    }.mapNotNull { it.damageAmount }.sum()
}

fun ShipAPI.estimateBeamDamageToBeTaken(t: Float = 1f): Float{
    return t * (Global.getCombatEngine()?.beams?.filter { it.damageTarget == this }?.map { it.damage.damage }?.sum() ?: 0f)
}

fun ShipAPI.estimateMissileDamageToBeTaken(t: Float = 1f): Float{
    return CombatUtils.getMissilesWithinRange(location, DAMAGE_SCAN_RANGE).filter {
        it.isArmed
    }.filter {
        it.isGuided || CollisionUtils.getCollides(it.location, it.location + t * it.velocity, location, collisionRadius)
    }.mapNotNull { it.damageAmount }.sum()
}

fun ShipAPI.estimateDamageToBeTaken(t: Float = 1f): Float{
    return estimateBeamDamageToBeTaken(t) + estimateProjectileDamageToBeTaken(t) + estimateMissileDamageToBeTaken(t)
}

fun ShipAPI.getRandomPointOnShipOutline(): Vector2f{
    val bounds = exactBounds ?: return Vector2f()
    bounds.update(location, facing)
    bounds.segments.random().let { s ->
        return s.p1 + Math.random().toFloat() * (s.p2 - s.p1)
    }
}

fun CampaignFleetAPI.isMastermindFleet(): Boolean = memoryWithoutUpdate.contains(MASTERMIND_FLEET_MEMKEY)

fun CampaignFleetAPI.markAsHunter(id: String){
    memoryWithoutUpdate[FleetManager.HUNTER_FLEET_ID_MEM_KEY] = id
}

fun CampaignFleetAPI.getHunterIdIfHunter(): String? = memoryWithoutUpdate[FleetManager.HUNTER_FLEET_ID_MEM_KEY] as? String

fun CampaignFleetAPI.isHunter(): Boolean = getHunterIdIfHunter() != null

fun CampaignFleetAPI.attackFleet(opponent: CampaignFleetAPI, delay: Float = 0f) {
    this.addAssignment(
        FleetAssignment.HOLD, null, delay
    )
    this.addAssignment(
        FleetAssignment.INTERCEPT,
        opponent,
        MAX_ATTACK_DURATION
    )
}

fun CampaignFleetAPI.follow(fleet: CampaignFleetAPI, delay: Float = 0f){
    this.addAssignment(
        FleetAssignment.HOLD, null, delay
    )
    this.addAssignment(FleetAssignment.FOLLOW, fleet, MAX_GOTO_ASSIGNMENT_DURATION)
}

fun CargoAPI.getSpecialQuantity(id: String, data: String? = null): Float{
    return getQuantity(CargoAPI.CargoItemType.SPECIAL, SpecialItemData(id, data))
}

fun CampaignFleetAPI.makeAlwaysHostile(){
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON, true);
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT, true);
    this.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER, true);
}

fun CampaignFleetAPI.getIntoHyperspaceAnd(onFinishScript: () -> Unit){
    if(isInHyperspace) {
        Global.getLogger(this.javaClass).error("Tried to go from hyperspace to hyperspace")
        return
    }
    val jp = findNearestJumpPointThatCouldBeExitedFrom() ?: return
    val jd = jp.destinations.firstOrNull { it.destination.isInHyperspace } ?: return
    clearAssignments()
    addAssignment(FleetAssignment.GO_TO_LOCATION, jp, 999f){
        Global.getSector().doHyperspaceTransition(this, jp, jd)
        onFinishScript()
    }
}

fun CampaignFleetAPI.getToDestSystemAnd (destSystem: StarSystemAPI, onFinishScript: () -> Unit) {
    if(destSystem == starSystem){
        onFinishScript()
        return
    }
    if(!isInHyperspace){
        getIntoHyperspaceAnd { getToDestSystemAnd(destSystem, onFinishScript) }
    }
    val (jp, jd)  = Global.getSector().hyperspace.jumpPoints.filterIsInstance<JumpPointAPI>().asSequence().associateWith {
        it.destinations.firstOrNull { d ->
            d.destination.starSystem == destSystem
        }
    }.filter { (_, v) ->
        v != null
    }.map { it.toPair() }.firstOrNull() ?: return
    clearAssignments()
    addAssignment(FleetAssignment.GO_TO_LOCATION, jp, 999f){
        Global.getSector().doHyperspaceTransition(this, jp, jd)
        onFinishScript()
    }
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