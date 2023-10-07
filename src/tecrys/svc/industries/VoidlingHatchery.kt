package tecrys.svc.industries

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.util.IntervalUtil
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.world.fleets.FleetSpawnParameterCalculator
import tecrys.svc.world.fleets.FleetSpawner
import tecrys.svc.world.fleets.voidlingHatcherySettings

class VoidlingHatchery: BaseIndustry() {
    companion object{
        const val ADVANCE_INTERVAL = 90f
        const val MAX_PATROL_DURATION = 5000f
    }

    private var fleet: CampaignFleetAPI? = null
    private val interval = IntervalUtil(ADVANCE_INTERVAL, 2f * ADVANCE_INTERVAL)

    override fun apply() {
        super.apply(true)
    }

    override fun isAvailableToBuild(): Boolean {
        if (!Global.getSector().playerFaction.knowsIndustry(getId())) {
            return false;
        }
        return market.planetEntity?.hasCondition(Conditions.LOW_GRAVITY) == true
    }

    override fun showWhenUnavailable(): Boolean = Global.getSector().playerFaction.knowsIndustry(getId())

    override fun getUnavailableReason(): String = "Planet must have low gravity"

    override fun canImprove(): Boolean = false

    override fun canInstallAICores(): Boolean = false

    override fun advance(amount: Float) {
        super.advance(amount)
        if (Global.getSector()?.isPaused == true) return
        interval.advance(amount)
        if(fleet == null || fleet?.isEmpty == true || fleet?.isDespawning == true || fleet?.isAlive == false){
            if(interval.intervalElapsed()){
                spawnFleet()
            }
        }
    }

    private fun spawnFleet(){
        market ?: return
        fleet = FleetSpawner().createFactionFleet(
            SVC_FACTION_ID, FleetSpawnParameterCalculator(voidlingHatcherySettings(market)),
            "Hatched Defenders"
        )

        fleet?.setFaction(market.factionId, true)
        market.containingLocation?.addEntity(fleet)
        fleet?.setLocation(market.location.x, market.location.y)
        fleet?.addAssignment(FleetAssignment.PATROL_SYSTEM, market.planetEntity, MAX_PATROL_DURATION)
        fleet?.memoryWithoutUpdate?.set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, false)

    }
}