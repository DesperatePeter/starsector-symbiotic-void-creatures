package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.utils.ShuntedNervousListener

class ShuntNervousSystem: BaseShipSystemScript() {
    private var ship: ShipAPI? = null
    private var listener: ShuntedNervousListener? = null
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        when(state){
            ShipSystemStatsScript.State.IN -> {
                ship = stats?.entity as? ShipAPI
                if(listener == null){
                    listener = ShuntedNervousListener()
                    ship?.addListener(listener)
                }
            }
            ShipSystemStatsScript.State.OUT -> {
                ship?.let {
                    listener?.run {
                        it.removeListener(this)
                        applyDelayedDamaged(it)
                    }
                    it.alphaMult = 1f
                }
                listener = null
            }
            ShipSystemStatsScript.State.ACTIVE -> {
                ship?.alphaMult = 0.5f
            }
            else -> {}
        }
    }
}