package tecrys.svc.world.fleets

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.*
import tecrys.svc.world.fleets.FleetSpawner.Companion.countFactionFleets
import tecrys.svc.world.notifications.DefeatedMagicBountyDialog

class FleetManager : EveryFrameScript {

    companion object {
        val spawner = FleetSpawner()
        const val WHALE_DIST = 500f
    }

    private val interval = IntervalUtil(10f, 30f)
    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if(Global.getSector().isPaused) return
        if (!DefeatedMagicBountyDialog.shouldSpawnVoidlings) return
        interval.advance(amount)
        if (!interval.intervalElapsed()) return
        while (spawnSvcFleet() && Global.getSettings().isDevMode) {
            Global.getLogger(this.javaClass).info("Spawned SVC fleet")
        }
        spawnWhaleFleet()
    }

    /**
     * @return true if fleet was successfully spawned
     */
    private fun spawnSvcFleet(): Boolean {
        val params = FleetSpawnParameterCalculator(svcSettings)
        val loc = spawner.getRandomSpawnableLocation(SVC_FACTION_ID)
        val fleet = spawner.spawnFactionFleetIfPossible(SVC_FACTION_ID, params, loc)
        params.logParameters()
        fleet?.let {
            it.addEventListener(SvcFleetListener)
            it.orbitClosestPlanet()
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
        val whales = spawner.createFactionFleet(VWL_FACTION_ID, whaleParams) ?: return false
        val voidlings = spawner.createFactionFleet(SVC_FACTION_ID, svcParams) ?: return false
        playerFleet.containingLocation?.addEntity(whales)
        playerFleet.containingLocation?.addEntity(voidlings)
        val loc = playerFleet.locationInHyperspace + Vector2f(2f * (Math.random().toFloat() - 0.5f) * WHALE_DIST, 2f * (Math.random().toFloat() - 0.5f) * WHALE_DIST)
        listOf(whales, voidlings).forEach {
            it.setLocation(loc.x, loc.y)
            it.forceSync()
        }
        voidlings.attackFleet(whales)
        voidlings.addEventListener(SvcFleetListener)
        whales.attackFleet(voidlings)
        whales.addEventListener(WhaleFleetListener)
        whales.customData[WHALES_ORIGINAL_STRENGTH_KEY] = whales.fleetPoints

        return true
    }



}