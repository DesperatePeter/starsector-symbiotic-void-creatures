package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import kotlin.math.max

class SpookyMindControl(private val ship: ShipAPI) : BaseEveryFrameCombatPlugin() {
    companion object {
        const val DURATION_DP_TIMES_SECONDS = 50f
        const val MIN_DURATION = 0.5f
        const val MAX_DURATION = 3f
    }

    init {
        ship.owner = if(ship.originalOwner == 0) 1 else 0
    }

    private var durationRemaining =
        (DURATION_DP_TIMES_SECONDS / max((ship.fleetMember?.deploymentPointsCost ?: 1f), 1f)).coerceIn(
            MIN_DURATION, MAX_DURATION
        )

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        durationRemaining -= amount
        if(durationRemaining <= 0f){
            onComplete()
        }
    }

    private fun onComplete(){
        ship.owner = ship.originalOwner
        Global.getCombatEngine()?.removePlugin(this)
    }
}