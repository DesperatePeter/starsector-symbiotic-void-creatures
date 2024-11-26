package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.ext.minus
import tecrys.svc.utils.getEffectiveShipTarget

class SymbioticTeleport : BaseShipSystemScript() {
    companion object{
        const val SYSTEM_RANGE = 1000f
    }

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        // TODO
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        ship?.run {
            val tgt = getEffectiveShipTarget() ?: return false
            return (location - tgt.location).length() <= SYSTEM_RANGE
        } ?: return false
    }
}