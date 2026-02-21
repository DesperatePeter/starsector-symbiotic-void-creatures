package tecrys.svc.shipsystems.spooky.plugins

import com.fs.starfarer.api.combat.ShipAPI

class SabotageWeapons(targetShip: ShipAPI, intensity: Float = 0.5f) : Sabotage(targetShip, intensity) {
    companion object{
        const val BASE_DURATION = 10f
        const val SECONDARY_TICK_INTENSITY_MULTIPLIER = 0.25f // additionally multiplied by 1-progress
        @JvmStatic
        fun sabotageWeapons(targetShip: ShipAPI?, chance: Float = 0.5f){
            targetShip?.allWeapons?.filter { Math.random() < chance }?.forEach { w -> targetShip.applyCriticalMalfunction(w, false) }
        }
    }

    override val durationFor10DpShip: Float
        get() = BASE_DURATION

    override fun onStart() {
        sabotageWeapons(targetShip, chance = intensity)
    }

    override fun onFinish() {
    }

    override fun periodicTick() {
        sabotageWeapons(targetShip,
            chance = intensity * SECONDARY_TICK_INTENSITY_MULTIPLIER * (1f - progress) * TICK_PERIOD
        )
    }
}