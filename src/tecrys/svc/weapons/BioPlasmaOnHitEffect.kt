package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import org.lwjgl.util.vector.Vector2f

class BioPlasmaOnHitEffect : OnHitEffectPlugin {
    companion object {
        const val DURATION = 5f
        const val EFFECT_MULT = 0.7f
        const val EFFECT_ID = "BioPlasmaOnHitEffect"
    }

    override fun onHit(
        projectile: DamagingProjectileAPI?,
        target: CombatEntityAPI?,
        point: Vector2f?,
        shieldHit: Boolean,
        damageResult: ApplyDamageResultAPI?,
        engine: CombatEngineAPI?
    ) {
        if (shieldHit) return
        val ship = target as? ShipAPI ?: return

        // Prevent stacking plugins: refresh the timer if it already exists
        if (ship.hasListenerOfClass(BioPlasmaListener::class.java)) {
            val listener = ship.getListeners(BioPlasmaListener::class.java).firstOrNull()
            if (listener != null) {
                listener.remainingDuration = DURATION
            }
        } else {
            ship.addListener(BioPlasmaListener(ship))
        }
    }

    class BioPlasmaListener(private val ship: ShipAPI) : AdvanceableListener {
        var remainingDuration = DURATION

        override fun advance(amount: Float) {
            remainingDuration -= amount
            val stats = ship.mutableStats

            // Direct stat modification prevents generating List/Iterator objects every frame
            if (remainingDuration <= 0f) {
                stats.acceleration.unmodify(EFFECT_ID)
                stats.turnAcceleration.unmodify(EFFECT_ID)
                stats.maxTurnRate.unmodify(EFFECT_ID)
                stats.ballisticRoFMult.unmodify(EFFECT_ID)
                stats.energyRoFMult.unmodify(EFFECT_ID)
                stats.missileRoFMult.unmodify(EFFECT_ID)
                ship.removeListener(this)
                return
            }

            stats.acceleration.modifyMult(EFFECT_ID, EFFECT_MULT)
            stats.turnAcceleration.modifyMult(EFFECT_ID, EFFECT_MULT)
            stats.maxTurnRate.modifyMult(EFFECT_ID, EFFECT_MULT)
            stats.ballisticRoFMult.modifyMult(EFFECT_ID, EFFECT_MULT)
            stats.energyRoFMult.modifyMult(EFFECT_ID, EFFECT_MULT)
            stats.missileRoFMult.modifyMult(EFFECT_ID, EFFECT_MULT)
        }
    }
}