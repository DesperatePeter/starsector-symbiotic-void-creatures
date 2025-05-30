package tecrys.svc.world.fleets

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.apache.log4j.Level
import tecrys.svc.FLEET_ORIGINAL_STRENGTH_KEY

class FleetSpawner {

    companion object {
        const val SYSTEM_HIDDEN_TAG = "hidden"
        val SYSTEM_NAME_BLOCKLIST = listOf("Limbo", "Prism") // don't spawn in systems where the name contains any of these words
        fun countFactionFleets(faction: String): Int {
            return getFactionFleets(faction).count()
        }

        fun getFactionFleets(faction: String): List<CampaignFleetAPI> {
            return Global.getSector().allLocations.map { getFactionFleetsInSystem(faction, it) }
                .flatten()

        }

        fun getFactionFleetsInSystem(faction: String, system: LocationAPI): List<CampaignFleetAPI> {
            return system.fleets.filter { fleet -> fleet.faction.id == faction }
        }

        fun isValidSpawnableEntity(token: SectorEntityToken): Boolean {
            return token !is CampaignFleetAPI && token !is CampaignProgressIndicatorAPI && token !is OrbitalStationAPI
                    && (token as? PlanetAPI)?.isStar != true
        }
    }

    fun spawnFactionFleetIfPossible(
        faction: String,
        params: FleetSpawnParameterCalculator,
        location: SectorEntityToken?,
        forceSpawn: Boolean = false,
        name: String? = null,
        guaranteedRolesWithQuantity: Map<String, Int>? = null,
        overrideDp: Float? = null
    ): CampaignFleetAPI? {
        val numFleets = countFactionFleets(faction)

        if (numFleets >= params.maxFleetCount && !forceSpawn) return null
        val loc = location ?: return null

        val fleet = createFactionFleet(faction, params, name, guaranteedRolesWithQuantity, overrideDp)
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
        val possibleLocations = Global.getSector().allLocations?.filter { loc -> // no colonized planets
            loc.planets?.all { it.faction.id == "neutral" } ?: false
        }?.filter { loc -> // not hidden of cut off from hyperspace
            !loc.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) && !loc.hasTag(SYSTEM_HIDDEN_TAG)
                    && loc.planets.none { it.hasTag(SYSTEM_HIDDEN_TAG) || it.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER) }
        }?.filter { loc -> // name doesn't contain a word from the blocklist ("Prism" or "Limbo")
            SYSTEM_NAME_BLOCKLIST.none {
                blockedSystem -> loc.name.contains(blockedSystem)
            }
        }?.filter { // no fleet of the chosen faction present
            it.fleets.none { loc -> loc.faction.id == faction }
        }?.filterNotNull()?.shuffled()?.mapNotNull { loc -> // is a reasonable place for the fleet to spawn (a planet/asteroid etc.)
            loc.allEntities?.filter {
                isValidSpawnableEntity(it)
            }
        }?.flatten()
        return possibleLocations?.randomOrNull()
    }

    fun createFactionFleet(
        factionId: String,
        params: FleetSpawnParameterCalculator,
        name: String? = null,
        guaranteedRolesWithQuantity: Map<String, Int>? = null,
        overrideDp: Float? = null
    ): CampaignFleetAPI? {
        val faction = Global.getSector().getFaction(factionId)
        if (faction == null) {
            Global.getLogger(this.javaClass).log(Level.ERROR, "Tried to create a fleet for unknown faction $factionId")
            return null
        }
        val n = name ?: Global.getSector().getFaction(factionId)?.getFleetTypeName("patrolLarge") ?: "unknown"
        val fleet = Global.getFactory().createEmptyFleet(factionId, n, true)

        guaranteedRolesWithQuantity?.forEach { (role, quantity) ->
            for (i in 0 until quantity) {
                if (faction.pickShipAndAddToFleet(role, FactionAPI.ShipPickParams(), fleet) <= 0.001f) {
                    Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet pick null")
                }
            }
        }
        val targetDp = overrideDp?.toInt() ?: params.fleetSize.toInt()
        while (fleet.fleetPoints < targetDp) {
            val role = params.combatRole
            if (faction.pickShipAndAddToFleet(role, FactionAPI.ShipPickParams(), fleet) <= 0.001f) {
                Global.getLogger(this.javaClass).log(Level.ERROR, "Fleet pick null")
                return null
            }
            fleet.inflateIfNeeded()
        }

        fleet.customData[FLEET_ORIGINAL_STRENGTH_KEY] = fleet.fleetPoints
        return fleet
    }

}