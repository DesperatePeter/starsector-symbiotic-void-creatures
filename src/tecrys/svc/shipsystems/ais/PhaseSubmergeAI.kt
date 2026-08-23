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
        const val DANGER_REDUCTION_IF_SYNAPSE_FIRING = 15000f
        const val DANGER_PER_COOLDOWN = 1000f
    }

    private var ship: ShipAPI? = null
    private var system: ShipSystemAPI? = null
    private var engine: CombatEngineAPI? = null
    private var synapses: List<WeaponAPI> = emptyList()

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
        this.system = system
        this.engine = engine
        // Cache the list of synapse weapons once
        this.synapses = ship?.allWeapons?.filter { it.id == SYNAPSE_ID } ?: emptyList()
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        val sys = (if(system?.id == SYSTEM_ID) system else ship?.phaseCloak) ?: return
        if(!sys.isUsable()) return

        ship?.run {
            val damage = estimateDamageToBeTaken()

            var synapseFiringCount = 0
            var synapsesReloadingPenalty = 0f

            // Do all the math in a single pass over our cached list
            for (i in 0 until synapses.size) {
                val w = synapses[i]
                if (w.cooldownRemaining < 0.01f && w.chargeLevel > 0.01f) {
                    synapseFiringCount++
                }
                synapsesReloadingPenalty += (w.cooldownRemaining / Math.max(0.01f, w.cooldown))
            }

            synapsesReloadingPenalty *= DANGER_PER_COOLDOWN

            val danger = damage * DANGER_PER_DAMAGE -
                    synapseFiringCount * DANGER_REDUCTION_IF_SYNAPSE_FIRING +
                    synapsesReloadingPenalty

            if(danger > ACTIVATE_AT_DANGER){
                sys.forceState(ShipSystemAPI.SystemState.IN, 0f)
            }
        }
    }
}