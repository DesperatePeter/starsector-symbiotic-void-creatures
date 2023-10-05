package tecrys.svc.world.fleets

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.makeHostile
import tecrys.svc.*
import tecrys.svc.listeners.SvcFleetListener
import tecrys.svc.listeners.VoidlingFIDConf
import tecrys.svc.listeners.WhaleFleetListener
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.attackFleet
import tecrys.svc.utils.makeAlwaysHostile
import tecrys.svc.utils.orbitClosestPlanet
import tecrys.svc.world.fleets.FleetSpawner.Companion.countFactionFleets
import tecrys.svc.world.notifications.DefeatedMagicBountyDialog
import tecrys.svc.world.notifications.NotificationShower

class FleetManager : EveryFrameScript {

    companion object {
        const val WHALE_RAND_DIST = 700f
        const val WHALE_PLAYER_FLEET_DIRECTION_DIST = 1500f
        const val WHALE_SPAWN_BASE_INTERVAL = 40f
        const val WHALE_OIL_PER_DP_IN_CARGO = 0.1f
        const val HUNTER_FLEET_DISTANCE = 3000f
        val MIN_DIST_FROM_CENTER_TO_SPAWN_HYPERSPACE_FLEETS = Global.getSettings().getInt("sectorWidth") * 0.15f
        val DIST_FROM_CENTER_SPAWN_CHANCE_SCALING = Global.getSettings().getInt("sectorWidth") * 0.25f
        val spawner = FleetSpawner()
        var whaleSpawnIntervalMultiplier: Float by CampaignSettingDelegate("$" + SVC_MOD_ID + "whaleSpawnMult", 1.0f)
        fun spawnSvcFleetNowAtPlayer(): Boolean{
            return FleetManager().spawnSvcFleet(Global.getSector().playerFleet, true)
        }
        fun tryToSpawnHunterFleet(): Boolean{
            return FleetManager().spawnHunterFleet()
        }
        fun currentSpawnChance(): Float {
            val playerFleet = Global.getSector().playerFleet ?: return 0f
            if (!playerFleet.isInHyperspace) return 0f
            val distFromCenter = playerFleet.locationInHyperspace.length()
            return (distFromCenter - MIN_DIST_FROM_CENTER_TO_SPAWN_HYPERSPACE_FLEETS) / DIST_FROM_CENTER_SPAWN_CHANCE_SCALING
        }
    }

    private val svcSpawnInterval = IntervalUtil(10f, 30f)
    private val hunterSpawnInterval = IntervalUtil(50f, 100f)
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
            while (spawnSvcFleet() && Global.getSettings().isDevMode) {
                Global.getLogger(this.javaClass).info("Spawned SVC fleet")
            }
        }
        if(whaleSpawnInterval.intervalElapsed()){
            spawnWhaleFleet()
            whaleSpawnInterval.setInterval(WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier,
                2f * WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier)
        }
        if(hunterSpawnInterval.intervalElapsed()){
            if(spawnHunterFleet()){
                NotificationShower.showNotificationRepeatable(NotificationShower.HUNTER_FLEET_APPROACHING_ID)
            }
        }
    }

    /**
     * @return true if fleet was successfully spawned
     */
    private fun spawnSvcFleet(location: SectorEntityToken? = null, forceSpawn: Boolean = false): Boolean {
        val params = FleetSpawnParameterCalculator(svcSettings)
        val loc = location ?: spawner.getRandomSpawnableLocation(SVC_FACTION_ID)
        val fleet = spawner.spawnFactionFleetIfPossible(SVC_FACTION_ID, params, loc, forceSpawn)
        params.logParameters()
        fleet?.let {
            it.addEventListener(SvcFleetListener)
            it.orbitClosestPlanet()
            it.makeHostile()
            it.makeAlwaysHostile()
            it.memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = VoidlingFIDConf()
            return true
        }
        return false
    }

    private fun spawnHunterFleet(): Boolean {
        val possibleHunters = hunterFleetsToSpawn.toList()
        if(possibleHunters.isEmpty()) return false
        val playerFleet = Global.getSector().playerFleet ?: return false
        if(!shouldSpawnBasedOnLocation()) return false

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
                    var loc = playerFleet.locationInHyperspace
                    loc += Vector2f(2f * (Math.random().toFloat() - 0.5f) * HUNTER_FLEET_DISTANCE, 2f * (Math.random().toFloat() - 0.5f) * HUNTER_FLEET_DISTANCE)
                    setLocation(loc.x, loc.y)

                    makeHostile()
                    makeAlwaysHostile()
                    attackFleet(playerFleet)
                    memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = VoidlingFIDConf()
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
    private fun spawnWhaleFleet(): Boolean {
        val whaleParams = FleetSpawnParameterCalculator(whaleSettings)
        if(countFactionFleets(VWL_FACTION_ID) >= whaleParams.maxFleetCount) return false
        val svcParams = FleetSpawnParameterCalculator(svcSettings)
        val playerFleet = Global.getSector().playerFleet ?: return false
        if(!shouldSpawnBasedOnLocation()) return false
        val whales = spawner.createFactionFleet(VWL_FACTION_ID, whaleParams) ?: return false
        val voidlings = spawner.createFactionFleet(SVC_FACTION_ID, svcParams) ?: return false
        playerFleet.containingLocation?.addEntity(whales)
        playerFleet.containingLocation?.addEntity(voidlings)
        var loc = playerFleet.locationInHyperspace
        loc += Vector2f(2f * (Math.random().toFloat() - 0.5f) * WHALE_RAND_DIST, 2f * (Math.random().toFloat() - 0.5f) * WHALE_RAND_DIST)
        val offset = Vector2f(playerFleet.velocity.x, playerFleet.velocity.y)
        offset.normalise()
        offset.scale(WHALE_PLAYER_FLEET_DIRECTION_DIST)
        loc += offset
        listOf(whales, voidlings).forEach {
            it.setLocation(loc.x, loc.y)
            it.forceSync()
        }
        voidlings.makeHostile()
        voidlings.makeAlwaysHostile()
        voidlings.attackFleet(whales)
        voidlings.addEventListener(SvcFleetListener)
        voidlings.memoryWithoutUpdate[MemFlags.FLEET_INTERACTION_DIALOG_CONFIG_OVERRIDE_GEN] = VoidlingFIDConf()
        whales.attackFleet(voidlings)
        whales.addEventListener(WhaleFleetListener)
        val oilInCargo = whales.fleetPoints * WHALE_OIL_PER_DP_IN_CARGO
        whales.cargo.addItems(CargoAPI.CargoItemType.SPECIAL, SpecialItemData(WHALE_OIL_ITEM_ID, WHALE_OIL_ITEM_ID), oilInCargo)

        return true
    }

    private fun shouldSpawnBasedOnLocation(): Boolean{
        return Math.random() + currentSpawnChance() > 1f
    }

}