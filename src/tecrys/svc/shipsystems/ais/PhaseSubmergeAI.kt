package tecrys.svc.shipsystems.ais

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAIScript
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.combat.WeaponAPI
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.estimateBeamDamageToBeTaken
import tecrys.svc.utils.estimateDamageToBeTaken
import tecrys.svc.utils.isUsable

class PhaseSubmergeAI: ShipSystemAIScript {

    companion object{
        const val SYSTEM_ID = "svc_phasesubmerge"
        const val SYNAPSE_ID = "svc_bigbrain"
        const val ACTIVATE_AT_DANGER = 2000f
        const val DANGER_PER_DAMAGE = 1f
        const val DANGER_REDUCTION_IF_SYNAPSE_FIRING = 5000f
        const val DANGER_PER_COOLDOWN = 1000f
    }

    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    private var engine: CombatEngineAPI? = null

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
        this.system = system
        this.engine = engine
    }

    private fun getSynapses(): List<WeaponAPI>{
        return ship?.allWeapons?.filter { it.id == SYNAPSE_ID } ?: listOf()
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val sys = (if(system?.id == SYSTEM_ID) system else ship?.phaseCloak) ?: return
        if(!sys.isUsable()) return
        ship?.run {
            // damage about to be taken, minus some value if synapse is firing or charging up, plus some value if cooling down
            val damage = estimateDamageToBeTaken()
            val synapseFiring =  getSynapses().count {
                it.cooldownRemaining < 0.01f && it.chargeLevel > 0.01f
            }
            val synapsesReloading = getSynapses().map{
                it.cooldownRemaining / it.cooldown
            }.sum() * DANGER_PER_COOLDOWN
            val danger = damage * DANGER_PER_DAMAGE -
                    synapseFiring * DANGER_REDUCTION_IF_SYNAPSE_FIRING +
                    synapsesReloading * DANGER_PER_COOLDOWN
            if(danger > ACTIVATE_AT_DANGER){
                sys.forceState(ShipSystemAPI.SystemState.IN, 0f)
            }
        }
    }
}