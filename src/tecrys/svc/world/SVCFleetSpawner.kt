package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import com.fs.starfarer.api.util.IntervalUtil
import org.apache.log4j.Level
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.findNearestPlanetTo
import tecrys.svc.*
import tecrys.svc.world.notifications.DefeatedMagicBountyDialog
import tecrys.svc.world.notifications.NotificationShower

class SVCFleetSpawner : EveryFrameScript {

    companion object {
        val FACTIONS_TO_SPAWN = listOf(SVC_FACTION_ID)
        const val MAX_ORBIT_ASSIGNMENT_DURATION = 1e9f
        const val MAX_GOTO_ASSIGNMENT_DURATION = 1000f
        fun countFactionFleets(faction: String): Int {
            return getFactionFleets(faction).count()
        }
        fun getFactionFleets(faction: String): List<CampaignFleetAPI>{
            return Global.getSector().allLocations.map { it.fleets.filter { fleet -> fleet.faction.id == faction } }.flatten()
        }
    }

    private val interval = IntervalUtil(10f, 30f)
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if(Global.getSector().isPaused) return
        if (!DefeatedMagicBountyDialog.shouldSpawnVoidlings) return
        interval.advance(amount)
        if (!interval.intervalElapsed()) return
        FACTIONS_TO_SPAWN.forEach { spawnFactionFleetsUntilLimit(it) }
        FleetSpawnParameters.logParameters()
    }

    private fun spawnFactionFleetsUntilLimit(faction: String) {
        val numFleets = countFactionFleets(faction)
        Global.getSector().allLocations?.filter { loc ->
            loc.planets?.all { it.faction.id == "neutral" } ?: false
        }?.filter {
            it.fleets.none { loc -> loc.faction.id == faction }
        }?.filterNotNull()?.shuffled()?.forEach { loc ->
            loc.allEntities?.filter { it !is CampaignFleetAPI && it !is CampaignProgressIndicatorAPI && it !is OrbitalStationAPI }
                ?.randomOrNull()?.let {
                    if (numFleets >= FleetSpawnParameters.maxFleetCount) return
                    val fleet = createFactionFleet(faction, FleetSpawnParameters.fleetSize.toInt())
                    if (fleet == null) {
                        Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet null")
                        return
                    }
                    it.containingLocation.addEntity(fleet)
                    fleet.setLocation(it.location.x, it.location.y)

                    addAssignments(fleet)

                    fleet.forceSync()
                    if (!Global.getSettings().isDevMode) return
                }
        }
    }

    private fun addAssignments(fleet: CampaignFleetAPI){
        fleet.addAssignment(FleetAssignment.GO_TO_LOCATION,
            fleet.findNearestPlanetTo(requireGasGiant = false,allowStars = true),
            MAX_GOTO_ASSIGNMENT_DURATION
        )
        fleet.addAssignment(FleetAssignment.ORBIT_AGGRESSIVE,
            fleet.findNearestPlanetTo(requireGasGiant = false,allowStars = true),
            MAX_ORBIT_ASSIGNMENT_DURATION
        )
    }

    private fun createFactionFleet(
        factionId: String,
        minDP: Int,
        name: String? = null
    ): CampaignFleetAPI? {
        val faction = Global.getSector().getFaction(factionId)
        if (faction == null) {
            Global.getLogger(this.javaClass).log(Level.ERROR, "Tried to create a fleet for unknown faction $factionId")
            return null
        }
        val n = name ?: Global.getSector().getFaction(factionId)?.pickRandomShipName() ?: "unknown"
        val fleet = Global.getFactory().createEmptyFleet(factionId, n, true)


        while (fleet.fleetPoints < minDP) {
            val role = FleetSpawnParameters.combatRole
            if (faction.pickShipAndAddToFleet(role, FactionAPI.ShipPickParams(), fleet) <= 0.001f) {
                Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet pick null")
                return null
            }
            fleet.inflateIfNeeded()
        }
        fleet.addEventListener(object : FleetEventListener {
            override fun reportFleetDespawnedToListener(
                fleet: CampaignFleetAPI?,
                reason: CampaignEventListener.FleetDespawnReason?,
                param: Any?
            ) {
            }

            override fun reportBattleOccurred(
                fleet: CampaignFleetAPI?,
                primaryWinner: CampaignFleetAPI?,
                battle: BattleAPI?
            ) {
                if (primaryWinner?.isPlayerFleet == true) {
                    if (!Global.getSector().memory.contains(SVC_FLEET_DEFEATED_MEM_KEY)) {
                        NotificationShower.showNotificationOnce("voidlings_defeated")
                    }
                    // mem-key enables magic bounty
                    Global.getSector().memory.set(SVC_FLEET_DEFEATED_MEM_KEY, true)
                }
            }

        })
        return fleet
    }

}