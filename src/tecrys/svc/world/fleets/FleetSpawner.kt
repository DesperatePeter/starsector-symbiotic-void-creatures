package tecrys.svc.world.fleets

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import org.apache.log4j.Level
import org.magiclib.kotlin.findNearestPlanetTo
import tecrys.svc.SVC_FACTION_ID

class FleetSpawner {

    companion object {
        fun countFactionFleets(faction: String): Int {
            return getFactionFleets(faction).count()
        }
        fun getFactionFleets(faction: String): List<CampaignFleetAPI> {
            return Global.getSector().allLocations.map { it.fleets.filter { fleet -> fleet.faction.id == faction } }
                .flatten()
        }
        fun isValidSpawnableEntity(token: SectorEntityToken): Boolean {
            return token !is CampaignFleetAPI && token !is CampaignProgressIndicatorAPI && token !is OrbitalStationAPI
                    && (token as? PlanetAPI)?.isStar != true
        }
    }

    fun spawnFactionFleetIfPossible(faction: String, params: FleetSpawnParameterCalculator, location: SectorEntityToken?): CampaignFleetAPI? {
        val numFleets = countFactionFleets(faction)

        if (numFleets >= params.maxFleetCount) return null
        val loc = location ?: return null

        val fleet = createFactionFleet(faction, params)
        if (fleet == null) {
            Global.getLogger(this.javaClass).log(Level.ERROR, "Tried to spawn fleet but newly created fleet was null")
            return null
        }
        loc.containingLocation.addEntity(fleet)
        fleet.setLocation(loc.location.x, loc.location.y)
        fleet.forceSync()
        return fleet
    }

    fun getRandomSpawnableLocation(faction: String): SectorEntityToken? {
        return Global.getSector().allLocations?.filter { loc ->
            loc.planets?.all { it.faction.id == "neutral" } ?: false
        }?.filter {
            it.fleets.none { loc -> loc.faction.id == faction }
        }?.filterNotNull()?.shuffled()?.map { loc ->
            loc.allEntities?.filter {
                isValidSpawnableEntity(it)
            }
        }?.filterNotNull()?.flatMap { it }?.randomOrNull()
    }

    fun createFactionFleet(
        factionId: String,
        params: FleetSpawnParameterCalculator,
        name: String? = null
    ): CampaignFleetAPI? {
        val faction = Global.getSector().getFaction(factionId)
        if (faction == null) {
            Global.getLogger(this.javaClass).log(Level.ERROR, "Tried to create a fleet for unknown faction $factionId")
            return null
        }
        val n = name ?: Global.getSector().getFaction(factionId)?.pickRandomShipName() ?: "unknown"
        val fleet = Global.getFactory().createEmptyFleet(factionId, n, true)


        while (fleet.fleetPoints < params.fleetSize.toInt()) {
            val role = params.combatRole
            if (faction.pickShipAndAddToFleet(role, FactionAPI.ShipPickParams(), fleet) <= 0.001f) {
                Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet pick null")
                return null
            }
            fleet.inflateIfNeeded()
        }
        return fleet
    }

}