package tecrys.svc.shipsystems.spooky.plugins

import com.fs.starfarer.api.combat.ShipAPI

class SabotageDrive(targetShip: ShipAPI, intensity: Float = 1.0f) : Sabotage(targetShip, intensity) {
    companion object{
        const val BASE_DURATION = 10f
        const val SECONDARY_TICK_INTENSITY_MULTIPLIER = 0.5f // additionally multiplied by 1-progress
        @JvmStatic
        fun sabotageDrive(targetShip: ShipAPI?, chance: Float = 1.0f) {
            targetShip?.engineController?.shipEngines?.filter{Math.random() < chance}?.forEach { e -> targetShip.applyCriticalMalfunction(e, false) }

        }
    }

    override val durationFor10DpShip: Float
        get() = BASE_DURATION

    override fun onStart() {
        sabotageDrive(targetShip, chance = intensity)
    }

    override fun onFinish() {
    }

    override fun periodicTick() {
        sabotageDrive(targetShip,
            chance = intensity * SECONDARY_TICK_INTENSITY_MULTIPLIER * (1f - progress) * TICK_PERIOD
        )
    }
}