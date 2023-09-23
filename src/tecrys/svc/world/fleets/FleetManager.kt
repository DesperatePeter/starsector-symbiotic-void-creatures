package tecrys.svc.world.fleets

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.makeHostile
import tecrys.svc.*
import tecrys.svc.listeners.SvcFleetListener
import tecrys.svc.listeners.WhaleFleetListener
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.attackFleet
import tecrys.svc.utils.makeAlwaysHostile
import tecrys.svc.utils.orbitClosestPlanet
import tecrys.svc.world.fleets.FleetSpawner.Companion.countFactionFleets
import tecrys.svc.world.notifications.DefeatedMagicBountyDialog

class FleetManager : EveryFrameScript {

    companion object {
        const val WHALE_RAND_DIST = 700f
        const val WHALE_PLAYER_FLEET_DIRECTION_DIST = 1500f
        const val WHALE_SPAWN_BASE_INTERVAL = 40f
        const val WHALE_OIL_PER_DP_IN_CARGO = 0.1f
        val MIN_DIST_FROM_CENTER_TO_SPAWN_WHALES = Global.getSettings().getInt("sectorWidth") * 0.15f
        val DIST_FROM_CENTER_SPAWN_CHANCE_SCALING = Global.getSettings().getInt("sectorWidth") * 0.25f
        val spawner = FleetSpawner()
        var whaleSpawnIntervalMultiplier: Float by CampaignSettingDelegate("$" + SVC_MOD_ID + "whaleSpawnMult", 1.0f)
        fun spawnSvcFleetNowAtPlayer(): Boolean{
            return FleetManager().spawnSvcFleet(Global.getSector().playerFleet, true)
        }
    }

    private val svcSpawnInterval = IntervalUtil(10f, 30f)
    private val whaleSpawnInterval = IntervalUtil(WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier,
        2f * WHALE_SPAWN_BASE_INTERVAL * whaleSpawnIntervalMultiplier)
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if(Global.getSector().isPaused) return
        if (!DefeatedMagicBountyDialog.shouldSpawnVoidlings) return
        svcSpawnInterval.advance(amount)
        whaleSpawnInterval.advance(amount)
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
            return true
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
        if (!playerFleet.isInHyperspace) return false
        val distFromCenter = playerFleet.locationInHyperspace.length()
        val rng = Math.random() + (distFromCenter - MIN_DIST_FROM_CENTER_TO_SPAWN_WHALES) / DIST_FROM_CENTER_SPAWN_CHANCE_SCALING
        if(rng < 1f) return false
        val whales = spawner.createFactionFleet(VWL_FACTION_ID, whaleParams) ?: return false
        val voidlings = spawner.createFactionFleet(SVC_FACTION_ID, svcParams) ?: return false
        playerFleet.containingLocation?.addEntity(whales)
        playerFleet.containingLocation?.addEntity(voidlings)
        var loc = playerFleet.locationInHyperspace
        loc += Vector2f(2f * (Math.random().toFloat() - 0.5f) * WHALE_RAND_DIST, 2f * (Math.random().toFloat() - 0.5f) * WHALE_RAND_DIST)
        var offset = Vector2f(playerFleet.velocity.x, playerFleet.velocity.y)
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
        whales.attackFleet(voidlings)
        whales.addEventListener(WhaleFleetListener)
        whales.customData[WHALES_ORIGINAL_STRENGTH_KEY] = whales.fleetPoints
        val oilInCargo = whales.fleetPoints * WHALE_OIL_PER_DP_IN_CARGO
        whales.cargo.addItems(CargoAPI.CargoItemType.SPECIAL, SpecialItemData(WHALE_OIL_ITEM_ID, WHALE_OIL_ITEM_ID), oilInCargo)

        return true
    }



}