package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lwjgl.util.vector.Vector2f
import kotlin.math.min

class DamageSharingListener(val shipTarget: ShipAPI): DamageTakenModifier {
    companion object{
        const val MODIFIER_ID = "svc_parasumbical_cord"
        const val DAMAGE_SHARED_MULT = 0.4f
        const val MAX_REL_DPS = 0.05f // won't deal more than this value times enemy max hitpoints per second
    }

    private var lastClock = 0f
    private val maxDps = MAX_REL_DPS * shipTarget.maxHitpoints
    private var maxAllowedDamage = maxDps

    override fun modifyDamageTaken(
        param: Any?,
        target: CombatEntityAPI?,
        damage: DamageAPI?,
        point: Vector2f?,
        shieldHit: Boolean
    ): String? {
        if(shieldHit) return null
        if(shipTarget.hitpoints < 2f) return null
        target ?: return null
        damage ?: return null
        val dmg = damage.damage

        val timeElapsed = Global.getCombatEngine().getTotalElapsedTime(false) - lastClock
        lastClock = Global.getCombatEngine().getTotalElapsedTime(false)
        maxAllowedDamage = min(maxDps, maxAllowedDamage + timeElapsed * maxDps)


        damage.modifier?.modifyMult(MODIFIER_ID, 1f - DAMAGE_SHARED_MULT)
        val dmgToDeal = min(dmg * DAMAGE_SHARED_MULT, maxAllowedDamage)
        maxAllowedDamage -= dmgToDeal
        Global.getCombatEngine().applyDamage(shipTarget, shipTarget.location, dmgToDeal, damage.type ?: DamageType.ENERGY, 0f, true, false, target)
        return MODIFIER_ID
    }
}