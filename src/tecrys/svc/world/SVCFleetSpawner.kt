package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.apache.log4j.Level
import org.apache.log4j.Priority
import tecrys.svc.SVC_FACTION_ID

class SVCFleetSpawner : EveryFrameScript {

    companion object{
        const val MAX_NUMBER_OF_ACTIVE_SVC_FLEETS = 100
    }

    private val interval = IntervalUtil(50f, 51f)
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        interval.advance(amount)
        if(!interval.intervalElapsed()) return
        var numFleets = countSVCFleets()
        Global.getSector().allLocations?.filter {
            loc -> loc.planets?.all { it.faction.id == "neutral" } ?: false
        }?.filter {
            it.fleets.none { loc -> loc.faction.id == SVC_FACTION_ID }
        }?.filterNotNull()?.forEach { loc ->
            loc.jumpPoints.firstOrNull()?.let {
                if(numFleets >= MAX_NUMBER_OF_ACTIVE_SVC_FLEETS) return
                val fleet = createSVCFleet()
                if(fleet == null){
                    Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet null")
                    return
                }
                loc.spawnFleet(it, 10f, 10f, fleet)
                numFleets++
            }
        }
    }

    private fun createSVCFleet(minDP: Int = (Math.random() * 250f).toInt(), name: String? = null) : CampaignFleetAPI? {
        val svc = Global.getSector().getFaction(SVC_FACTION_ID) ?: return null
        val n = name ?: Global.getSector().getFaction(SVC_FACTION_ID)?.pickRandomShipName() ?: "unknown"
        val fleet = Global.getFactory().createEmptyFleet(SVC_FACTION_ID, n, true)


        while (fleet.fleetPoints < minDP){
            val role = listOf("combatSmall", "combatMedium", "combatLarge").random()
            if(svc.pickShipAndAddToFleet(role, FactionAPI.ShipPickParams(), fleet) <= 0.001f){
                Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet pick null")
                return null
            }
            fleet.inflateIfNeeded()
        }

        return fleet
    }

    private fun countSVCFleets(): Int{
        return Global.getSector().allLocations.sumOf { loc ->
            loc.fleets.filterNotNull().count { it.faction.id == SVC_FACTION_ID }
        }
    }

}