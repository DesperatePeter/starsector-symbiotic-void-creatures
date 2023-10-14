package tecrys.svc.industries

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.util.IntervalUtil
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.listeners.FleetDespawnedListener
import tecrys.svc.world.fleets.FleetSpawnParameterCalculator
import tecrys.svc.world.fleets.FleetSpawner
import tecrys.svc.world.fleets.voidlingHatcherySettings

class VoidlingHatchery: BaseIndustry() {
    companion object{
        const val MIN_ADVANCE_INTERVAL = 30f // in days
        const val MAX_ADVANCE_INTERVAL = 60f // in days
        const val MAX_PATROL_DURATION = 5000f
    }

    private var fleet: CampaignFleetAPI? = null
    private var fleetDespawnedListener: FleetDespawnedListener? = null
    private val interval = IntervalUtil(MIN_ADVANCE_INTERVAL, MAX_ADVANCE_INTERVAL)

    override fun apply() {
        super.apply(true)
    }

    override fun isAvailableToBuild(): Boolean {
        if (Global.getSector().playerPerson?.faction?.knowsIndustry(getId()) != true) {
            return false;
        }
        return market.planetEntity?.hasCondition(Conditions.LOW_GRAVITY) == true
    }

    override fun showWhenUnavailable(): Boolean = Global.getSector().playerPerson?.faction?.knowsIndustry(getId()) == true

    override fun getUnavailableReason(): String = "Planet must have low gravity. As space-borne creatures, voidlings are very sensitive to high gravity. "

    override fun canImprove(): Boolean = false

    override fun canInstallAICores(): Boolean = false

    override fun advance(amount: Float) {
        super.advance(amount)
        if (Global.getSector()?.isPaused == true) return
        if(fleetDespawnedListener?.isFleetDespawned != false){
            fleet?.despawn()
            val advanceDays = Global.getSector().clock?.convertToDays(amount) ?: 0f
            interval.advance(advanceDays)
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

        fleetDespawnedListener = FleetDespawnedListener()
        fleet?.addEventListener(fleetDespawnedListener)

        fleet?.setFaction(market.factionId, true)
        market.containingLocation?.addEntity(fleet)
        fleet?.setLocation(market.location.x, market.location.y)
        fleet?.addAssignment(FleetAssignment.PATROL_SYSTEM, market.planetEntity, MAX_PATROL_DURATION)
        fleet?.memoryWithoutUpdate?.set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, false)

    }
}