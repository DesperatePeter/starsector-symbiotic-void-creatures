package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

class DamageSharingListener(val shipTarget: ShipAPI): DamageTakenModifier {
    companion object{
        const val MODIFIER_ID = "svc_parasumbical_cord"
        const val DAMAGE_SHARED_MULT = 0.5f
    }
    override fun modifyDamageTaken(
        param: Any?,
        target: CombatEntityAPI?,
        damage: DamageAPI?,
        point: Vector2f?,
        shieldHit: Boolean
    ): String? {
        if(shieldHit) return null
        if(shipTarget.hitpoints < 2f) return null
        val dmg = damage?.damage ?: 0f
        damage?.modifier?.modifyMult(MODIFIER_ID, DAMAGE_SHARED_MULT)
        shipTarget.hitpoints = max(1f, shipTarget.hitpoints - dmg * DAMAGE_SHARED_MULT)
        return MODIFIER_ID
    }
}