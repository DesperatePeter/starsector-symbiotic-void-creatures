package tecrys.svc.shipsystems.spooky.plugins

import com.fs.starfarer.api.combat.ShipAPI

class SabotageCrew(targetShip: ShipAPI, intensity: Float = 1.0f) : Sabotage(targetShip, intensity) {
    companion object {
        const val BASE_DURATION = 10f
        const val TARGET_CR = 0.2f
        const val REL_CR_REDUCTION = 0.8f
        const val FLAT_CR_REDUCTION = 0.2f
    }
    override val durationFor10DpShip: Float
        get() = BASE_DURATION

    private val initialCrToReduce = (FLAT_CR_REDUCTION + ((targetShip.currentCR - TARGET_CR) * REL_CR_REDUCTION).coerceIn(0f, 100f)) * intensity
    private var remainingCrReduction = 0f

    override fun onStart() {
        targetShip.currentCR -= initialCrToReduce
        remainingCrReduction = initialCrToReduce
    }

    override fun onFinish() {
        targetShip.currentCR += remainingCrReduction
        remainingCrReduction = 0f
    }

    override fun periodicTick() {
        val crToAdd = remainingCrReduction * progress * progress * TICK_PERIOD
        targetShip.currentCR += crToAdd
        remainingCrReduction -= crToAdd
    }
}