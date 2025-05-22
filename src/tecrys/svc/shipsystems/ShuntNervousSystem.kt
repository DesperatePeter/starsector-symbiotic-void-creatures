package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.utils.ShuntedNervousListener

class ShuntNervousSystem: BaseShipSystemScript() {
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        when(state){
            ShipSystemStatsScript.State.IN -> {
                val ship = stats?.entity as? ShipAPI

                if (ship == null) {
                    return
                }

                if(!ship.hasListenerOfClass(ShuntedNervousListener::class.java)){
                    ship.addListener(ShuntedNervousListener())
                }
            }
            ShipSystemStatsScript.State.ACTIVE -> {
                val ship = stats?.entity as? ShipAPI
                ship?.alphaMult = 0.5f
            }
            else -> {}
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        val ship = stats?.entity as? ShipAPI ?: return
        val listener = ship.getListeners(ShuntedNervousListener::class.java).getOrNull(0) as? ShuntedNervousListener ?: return
        listener.run {
            ship.removeListener(this)
            applyDelayedDamaged(ship)
        }
        ship.alphaMult = 1f
    }
}