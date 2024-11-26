package tecrys.svc.world.fleets

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhostCreator
import com.fs.starfarer.api.impl.campaign.ghosts.SensorGhostManager
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.makeHostile
import tecrys.svc.*
import tecrys.svc.listeners.*
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.attackFleet
import tecrys.svc.utils.makeAlwaysHostile
import tecrys.svc.utils.orbitClosestPlanet
import tecrys.svc.world.fleets.FleetSpawner.Companion.countFactionFleets
import tecrys.svc.world.notifications.DefeatedMagicBountyDialog
import tecrys.svc.world.notifications.NotificationShower
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class FleetManager : EveryFrameScript {

    companion object {
        const val WHALE_RAND_DIST = 600f
        const val WHALE_PLAYER_FLEET_DIRECTION_DIST = 1300f
        const val WHALE_SPAWN_BASE_INTERVAL = 250f
        const val WHALE_OIL_PER_DP_IN_CARGO = 0.1f
        const val HUNTER_FLEET_DISTANCE = 2000f
        const val WHALE_FLEET_IDENTIFICATION_KEY = "$" + "SVC_WHALE_FLEET_TAG"
        // Distance between whale fleets and voidling fleets when they spawn. Needs to be low enough for them to see each other!
        const val WHALE_VOIDLING_DIST = 850f
        const val WHALE_VOIDLING_CHANCE = 0.8 // chance [0.0 .. 1.0] that a whale fleet spawns together with voidlings attacking it
        private val MIN_DIST_FROM_CENTER_TO_SPAWN_HYPERSPACE_FLEETS = Global.getSettings().getInt("sectorWidth") * 0.15f
        private val DIST_FROM_CENTER_SPAWN_CHANCE_SCALING = Global.getSettings().getInt("sectorWidth") * 0.25f
        const val MASTERMIND_FLEET_MEM_KEY = "\$SVC_MASTERMIND_FLEET"
        val spawner = FleetSpawner()
        var whaleSpawnIntervalMultiplier: Float by CampaignSettingDelegate("$" + SVC_MOD_ID + "whaleSpawnMult", 1.0f)
        fun spawnSvcFleetNowAtPlayer(): Boolean{
            return FleetManager().spawnSvcFleet(Global.getSector().playerFleet, true) != null
        }
        fun tryToSpawnHunterFleet(): Boolean{
            return FleetManager().spawnHunterFleet()
        }
        fun tryToSpawnWhales(): Boolean{
            return FleetManager().spawnWhaleEncounter()
        }
        fun currentSpawnChance(): Float {
            val playerFleet = Global.getSector().playerFleet ?: return 0f
            if (!playerFleet.isInHyperspace) return 0f
            val distFromCenter = playerFleet.locationInHyperspace.length()
            return (distFromCenter - MIN_DIST_FROM_CENTER_TO_SPAWN_HYPERSPACE_FLEETS) / DIST_FROM_CENTER_SPAWN_CHANCE_SCALING
        }
    }

    private val svcSpawnInterval = IntervalUtil(10f, 30f)
    private val hunterSpawnInterval = IntervalUtil(250f, 400f)
    private val whaleSpawnInterval = IntervalUtil(WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier,
        2f * WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier)
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if(Global.getSector().isPaused) return
        if (!DefeatedMagicBountyDialog.shouldSpawnVoidlings) return
        svcSpawnInterval.advance(amount)
        whaleSpawnInterval.advance(amount)
        hunterSpawnInterval.advance(amount)
        if (svcSpawnInterval.intervalElapsed()){
            while (spawnSvcFleet() != null && Global.getSettings().isDevMode) {
                Global.getLogger(this.javaClass).info("Spawned SVC fleet")
            }
        }
        if(whaleSpawnInterval.intervalElapsed()){
            spawnWhaleEncounter()
            whaleSpawnInterval.setInterval(WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier,
                2f * WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier)
        }
    }



    /**
     * @return true if fleet was successfully spawned
     */
    fun spawnSvcFleet(location: SectorEntityToken? = null, forceSpawn: Boolean = false, spawnParamOverride: FleetSpawnParameterCalculator? = null): CampaignFleetAPI? {
        val params = spawnParamOverride ?: FleetSpawnParameterCalculator(svcSettings)
        val loc = location ?: spawner.getRandomSpawnableLocation(SVC_FACTION_ID)
        val fleet = spawner.spawnFactionFleetIfPossible(SVC_FACTION_ID, params, loc, forceSpawn)
        params.logParameters()
        fleet?.let {
            it.addEventListener(SvcFleetListener)
            it.orbitClosestPlanet()
            it.makeHostile()
            it.makeAlwaysHostile()
            it.memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = VoidlingFIDConf()
            return it
        }
        return null
    }

    private fun genHunterLocation(): Vector2f{
        var loc = Global.getSector().playerFleet.locationInHyperspace
        loc += Vector2f(2f * (Math.random().toFloat() - 0.5f) * HUNTER_FLEET_DISTANCE, 2f * (Math.random().toFloat() - 0.5f) * HUNTER_FLEET_DISTANCE)
        return loc
    }

    fun spawnMastermindFleet() : CampaignFleetAPI? {
        val possibleLocations = (0..10).mapNotNull { _ -> spawner.getRandomSpawnableLocation(SVC_FACTION_ID) }
        val loc = possibleLocations.maxByOrNull { it.location.length() } ?: return null
        val params = FleetSpawnParameterCalculator(svcSettings)
        val fleet = spawner.createFactionFleet(SVC_FACTION_ID, params, mastermindFleet.name, mastermindFleet.rolesQuantity, mastermindFleet.minDP) ?: return null
        fleet.run {
            memoryWithoutUpdate[MASTERMIND_FLEET_MEMKEY] = true
            makeHostile()
            makeAlwaysHostile()
            addEventListener(MastermindFleetListener())
            loc.containingLocation.addEntity(this)
            setLocation(loc.location.x, loc.location.y)
            memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = MastermindFIDConf()
            memoryWithoutUpdate[MemFlags.CAN_ONLY_BE_ENGAGED_WHEN_VISIBLE_TO_PLAYER] = true
            memoryWithoutUpdate[MemFlags.FLEET_IGNORES_OTHER_FLEETS] = true
            memoryWithoutUpdate[MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS] = true
        }
        return fleet
    }

    fun spawnHunterFleet(loc: Vector2f = genHunterLocation(), forceSpawn: Boolean = false): Boolean {
        val possibleHunters = hunterFleetsToSpawn.toList()
        if(possibleHunters.isEmpty()) return false
        val playerFleet = Global.getSector().playerFleet ?: return false
        if(!shouldSpawnBasedOnLocation() && !forceSpawn) return false

        val svcParams = FleetSpawnParameterCalculator(svcSettings)
        possibleHunters.forEach {
            val hunterConfig = it.second
            if(svcParams.spawnPower >= hunterConfig.minSpawnPower){
                val hunterFleet = spawner.createFactionFleet(
                    SVC_FACTION_ID, svcParams,
                    hunterConfig.name, hunterConfig.rolesQuantity, hunterConfig.minDP)

                hunterFleet?.run {
                    hunterFleetsToSpawn.remove(hunterConfig.id)
                    addEventListener(SvcFleetListener)

                    playerFleet.containingLocation.addEntity(this)
                    setLocation(loc.x, loc.y)

                    makeHostile()
                    makeAlwaysHostile()
                    attackFleet(playerFleet)
                    memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = VoidlingFIDConf()
                    customData
                    hunterConfig.fleetListener?.let { listener ->
                        addEventListener(listener)
                    }
                    return true
                }
            }
        }
        return false
    }

    /**
     * @return true if fleet was successfully spawned
     */
    private fun spawnWhaleEncounter(): Boolean {
        val whaleParams = FleetSpawnParameterCalculator(whaleSettings)
        if(countFactionFleets(VWL_FACTION_ID) >= whaleParams.maxFleetCount) return false

        val playerFleet = Global.getSector().playerFleet ?: return false
        if(!shouldSpawnBasedOnLocation()) return false

        val whales = spawner.createFactionFleet(VWL_FACTION_ID, whaleParams) ?: return false
        playerFleet.containingLocation?.addEntity(whales)

        val svcParams = FleetSpawnParameterCalculator(svcSettings)
        val shouldSpawnVoidlings = Math.random() <= WHALE_VOIDLING_CHANCE
        val voidlings = if(shouldSpawnVoidlings) spawner.createFactionFleet(SVC_FACTION_ID, svcParams) else null

        playerFleet.containingLocation?.addEntity(voidlings)

        var loc = playerFleet.locationInHyperspace
        loc += Vector2f(2f * (Math.random().toFloat() - 0.5f) * WHALE_RAND_DIST, 2f * (Math.random().toFloat() - 0.5f) * WHALE_RAND_DIST)
        val offset = Vector2f(playerFleet.velocity.x, playerFleet.velocity.y)
        try{
            offset.normalise()
        }catch (e: IllegalStateException){
            offset.x = 0f
            offset.y = 1f
        }
        offset.scale(WHALE_PLAYER_FLEET_DIRECTION_DIST)
        loc += offset
        voidlings?.setLocation(loc.x, loc.y)
        val whaleOffsetAngle = 2f * PI.toFloat() * Math.random().toFloat()
        whales.setLocation((loc.x + WHALE_VOIDLING_DIST * sin(whaleOffsetAngle)), loc.y + WHALE_VOIDLING_DIST * cos(whaleOffsetAngle))
        listOf(whales, voidlings).forEach {
            it?.forceSync()
        }

        voidlings?.let {
            it.makeHostile()
            it.makeAlwaysHostile()
            it.attackFleet(whales, 0.125f)
            it.addEventListener(SvcFleetListener)
            it.memoryWithoutUpdate?.set(MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN, VoidlingFIDConf())
            whales.attackFleet(it, 0.125f)
        }
        whales.addEventListener(WhaleFleetListener)
        val oilInCargo = whales.fleetPoints * WHALE_OIL_PER_DP_IN_CARGO
        whales.cargo.addItems(CargoAPI.CargoItemType.SPECIAL, SpecialItemData(WHALE_OIL_ITEM_ID, WHALE_OIL_ITEM_ID), oilInCargo)
        whales.customData[WHALE_FLEET_IDENTIFICATION_KEY] = true

        return true
    }

    private fun shouldSpawnBasedOnLocation(): Boolean{
        return Math.random() + currentSpawnChance() > 1f
    }

}