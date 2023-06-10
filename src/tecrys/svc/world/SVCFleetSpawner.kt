package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CampaignProgressIndicatorAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.OrbitalStationAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.apache.log4j.Level
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.UVC_FACTION_ID

class SVCFleetSpawner : EveryFrameScript {

    companion object{
        const val MAX_NUMBER_OF_ACTIVE_SPAWNED_FLEETS = 100
        val FACTIONS_TO_SPAWN = listOf(SVC_FACTION_ID, UVC_FACTION_ID)
    }

    private val interval = IntervalUtil(50f, 250f)
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        interval.advance(amount)
        if(!interval.intervalElapsed()) return
        FACTIONS_TO_SPAWN.forEach { spawnFactionFleetsUntilLimit(it) }
    }

    private fun spawnFactionFleetsUntilLimit(faction: String) {
        val numFleets = countFactionFleets(faction)
        Global.getSector().allLocations?.filter {
                loc -> loc.planets?.all { it.faction.id == "neutral" } ?: false
        }?.filter {
            it.fleets.none { loc -> loc.faction.id == faction }
        }?.filterNotNull()?.forEach { loc ->
            loc.allEntities?.filter { it !is CampaignFleetAPI && it !is CampaignProgressIndicatorAPI && it !is OrbitalStationAPI}
                ?.randomOrNull()?.let{
                if(numFleets >= MAX_NUMBER_OF_ACTIVE_SPAWNED_FLEETS) return
                val fleet = createFactionFleet(faction)
                if(fleet == null){
                    Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet null")
                    return
                }
                loc.spawnFleet(it, 10f, 10f, fleet)
                return
            }
        }
    }

    private fun createFactionFleet(factionId: String, minDP: Int = (Math.random() * 300f).toInt(), name: String? = null) : CampaignFleetAPI? {
        val faction = Global.getSector().getFaction(factionId)
        if(faction == null){
            Global.getLogger(this.javaClass).log(Level.ERROR, "Tried to create a fleet for unknown faction $factionId")
            return null
        }
        val n = name ?: Global.getSector().getFaction(factionId)?.pickRandomShipName() ?: "unknown"
        val fleet = Global.getFactory().createEmptyFleet(factionId, n, true)


        while (fleet.fleetPoints < minDP){
            val role = listOf("combatSmall", "combatSmall", "combatMedium", "combatLarge").random()
            if(faction.pickShipAndAddToFleet(role, FactionAPI.ShipPickParams(), fleet) <= 0.001f){
                Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet pick null")
                return null
            }
            fleet.inflateIfNeeded()
        }
        return fleet
    }

    private fun countFactionFleets(faction: String): Int{
        return Global.getSector().allLocations.sumOf { loc ->
            loc.fleets.filterNotNull().count { it.faction.id == faction }
        }
    }

}