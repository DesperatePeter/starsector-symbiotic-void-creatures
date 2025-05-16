package tecrys.svc.hullmods.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.shipsystems.utils.ShuntedNervousListener
import tecrys.svc.utils.ExtraDamageInfo

class DefensiveBloodListener(ship: ShipAPI) : DamageTakenModifier {

    // damage taken gets reduced based on damage taken in the last couple seconds
    // whenever damage is taken, the current value will be incremented by that much
    // every second, that value gets reduced by the amount defined by DECAY_PER_SEC
    // when the value exceeds MIN_VALUE, all damage gets reduced
    // the reduction scales linearly between MIN_VALUE and MAX_VALUE, being 0 at MIN_VALUE
    // and MAX_REDUCTION at MAX_VALUE

    companion object{
        val DECAY_PER_SEC = mapOf(
            HullSize.FIGHTER to 100f,
            HullSize.FRIGATE to 900f,
            HullSize.DESTROYER to 1200f,
            HullSize.CRUISER to 1500f,
            HullSize.CAPITAL_SHIP to 2000f
        )
        val MAX_VALUE = DECAY_PER_SEC.mapValues { it.value * 5f } // same as DECAY_PER_SEC with values * 5
        val MIN_VALUE = DECAY_PER_SEC.mapValues { it.value * 1f }
        val MAX_REDUCTION =  mapOf(
        HullSize.FIGHTER to 0.6f,
        HullSize.FRIGATE to 0.5f,
        HullSize.DESTROYER to 0.4f,
        HullSize.CRUISER to 0.3f,
        HullSize.CAPITAL_SHIP to 0.2f
        )
        const val ID = "SVC_BLOOD_CLOTTING"
        private val DAMAGE_VALUE_MODIFIER = mapOf(
            DamageType.ENERGY to 1f,
            DamageType.FRAGMENTATION to 0.5f,
            DamageType.HIGH_EXPLOSIVE to 1.25f,
            DamageType.KINETIC to 0.75f,
            DamageType.OTHER to 1f
        )
    }

    private val maxValue = MAX_VALUE[ship.hullSize] ?: 500f
    private val minValue = MIN_VALUE[ship.hullSize] ?: 100f
    private val decay = DECAY_PER_SEC[ship.hullSize] ?: 100f
    private val reduction = MAX_REDUCTION[ship.hullSize] ?: 100f
    private var lastClock = 0f
    private var value = 0f

    override fun modifyDamageTaken(
        param: Any?,
        target: CombatEntityAPI?,
        damage: DamageAPI?,
        point: Vector2f?,
        shieldHit: Boolean
    ): String? {
        if(shieldHit) return null
        damage ?: return null
        if (param != null) {
            if (param is ExtraDamageInfo && param.modifiedBy == ShuntedNervousListener.MULT_ID) {
                return null
            }
        }
        val deltaT = Global.getCombatEngine().getTotalElapsedTime(false) - lastClock
        lastClock = Global.getCombatEngine().getTotalElapsedTime(false)
        value = (value - deltaT * decay).coerceIn(0f, maxValue)
        if(value < minValue){
            value += damage.damage * (DAMAGE_VALUE_MODIFIER[damage.type] ?: 0f)
            return null
        }
        val mult = (value - minValue) / (maxValue - minValue) * reduction
        damage.modifier.modifyMult(ID, 1f - mult)
        value += damage.damage * (DAMAGE_VALUE_MODIFIER[damage.type] ?: 0f)
        return ID
    }
}