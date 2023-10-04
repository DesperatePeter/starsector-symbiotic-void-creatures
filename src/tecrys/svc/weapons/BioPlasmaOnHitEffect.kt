package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lwjgl.util.vector.Vector2f

class BioPlasmaOnHitEffect(): OnHitEffectPlugin {
    companion object{
        const val DURATION = 5f
        const val EFFECT_STRENGTH = -0.3f
        const val EFFECT_ID = "BioPlasmaOnHitEffect"
    }

    private var remainingDuration = DURATION

    override fun onHit(
        projectile: DamagingProjectileAPI?,
        target: CombatEntityAPI?,
        point: Vector2f?,
        shieldHit: Boolean,
        damageResult: ApplyDamageResultAPI?,
        engine: CombatEngineAPI?
    ) {
        if(shieldHit) return
        val ship = target as? ShipAPI ?: return
        engine ?: return
        engine.addPlugin(BioPlasmaOnHitScript(ship, engine))
    }

    inner class BioPlasmaOnHitScript(private var ship: ShipAPI, private var engine: CombatEngineAPI?): BaseEveryFrameCombatPlugin() {
        private val statsToModify = ship.mutableStats.run {
            listOf(acceleration, turnAcceleration, maxTurnRate, ballisticRoFMult, energyRoFMult, missileRoFMult)
        }
        override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
            statsToModify.forEach { it.modifyMult(EFFECT_ID, EFFECT_STRENGTH) }
            remainingDuration -= amount
            if(remainingDuration <= 0f){
                statsToModify.forEach { it.unmodify(EFFECT_ID) }
                engine?.removePlugin(this)
            }
        }
    }
}