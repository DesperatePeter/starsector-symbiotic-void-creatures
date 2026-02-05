package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.ext.minus
import tecrys.svc.shipsystems.SpookyActionAtADistance
import tecrys.svc.shipsystems.spooky.gui.SpookyGuiShower
import tecrys.svc.shipsystems.spooky.gui.SpookyPlayerGui
import tecrys.svc.utils.getEffectiveShipTarget

class SpookyPlayerImpl(private val ship: ShipAPI): SpookyActionAtADistance.SpookyImpl {
    private val guiShower = SpookyGuiShower(ship)
    companion object{
        const val SHOULD_PAUSE_ON_ACTIVATION = true
        const val SHOULD_UNPAUSE_ON_FINISH = true
    }
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        if(state != ShipSystemStatsScript.State.ACTIVE) return
        if(guiShower.isRunning) return
        guiShower.gui = ship.getEffectiveShipTarget()?.let { SpookyPlayerGui(guiShower, it) } ?: return
        guiShower.start(SHOULD_PAUSE_ON_ACTIVATION)
    }

    override fun isUsable(
        system: ShipSystemAPI?,
        ship: ShipAPI?
    ): Boolean {
        val tgt = ship?.shipTarget ?: return false
        if (ship.isPhased) return false
        if (tgt.isHulk) return false
        return (tgt.location - ship.location).length() <= SpookyActionAtADistance.SYSTEM_RANGE
    }
}