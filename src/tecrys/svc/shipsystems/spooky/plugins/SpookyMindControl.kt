package tecrys.svc.shipsystems.spooky.plugins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import kotlin.math.max

class SpookyMindControl(ship: ShipAPI, durationMultiplier: Float) : Sabotage(ship, durationMultiplier = durationMultiplier) {
    companion object {
        const val BASE_DURATION = 3f
    }
    override val durationFor10DpShip: Float
        get() = BASE_DURATION

    override fun onStart() {
        targetShip.owner = if(targetShip.originalOwner == 0) 1 else 0
    }

    override fun onFinish() {
        targetShip.owner = targetShip.originalOwner
    }

    override fun periodicTick() {
    }
}