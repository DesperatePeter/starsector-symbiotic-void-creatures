package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.ext.minus
import tecrys.svc.shipsystems.SpookyActionAtADistance
import tecrys.svc.utils.getEffectiveShipTarget
import java.awt.Color

class SpookyAllyImpl(private val ship: ShipAPI?): SpookyActionAtADistance.SpookyImpl {
    private var alreadyTriggered = false
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        when (state) {
            ShipSystemStatsScript.State.ACTIVE -> {
                if(!alreadyTriggered) {
                    alreadyTriggered = true
                    applyEffect()
                }
            }
            else -> alreadyTriggered = false
        }
    }

    private fun applyEffect() {
        val target = ship?.getEffectiveShipTarget() ?: return
        listOf(
            {sabotageCrew(target)},
            {sabotageWeapons(target)},
            {sabotageDrive(target)},
            {mindControl(target)}
        ).random()()
        ship.setJitter(ship, Color.PINK, 0.5f, 4, 10f)
    }

    override fun isUsable(
        system: ShipSystemAPI?,
        ship: ShipAPI?
    ): Boolean {
        val tgt = ship?.getEffectiveShipTarget() ?: return false
        if(tgt.isHulk) return false
        return (tgt.location - ship.location).length() <= SpookyActionAtADistance.SYSTEM_RANGE
    }
}