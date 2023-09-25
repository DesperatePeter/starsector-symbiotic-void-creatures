package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.combat.getNearestPointOnBounds
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.computeEffectiveArmorAroundIndex
import kotlin.math.max

class DamageSharingListener(val shipTarget: ShipAPI): DamageTakenModifier {
    companion object{
        const val MODIFIER_ID = "svc_parasumbical_cord"
        const val DAMAGE_SHARED_MULT = 0.4f
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
        target ?: return null
        damage ?: return null
        // val grid = (target as? ShipAPI)?.armorGrid
        val dmg = damage.damage
//        val dmgMult = when(damage?.type){
//            DamageType.ENERGY -> 1f
//            DamageType.KINETIC -> 0.5f
//            DamageType.HIGH_EXPLOSIVE -> 2f
//            DamageType.FRAGMENTATION -> 0.25f
//            else -> 1f
//        }
//        grid?.run {
//            point?.let { p ->
//                getCellAtLocation(p)?.let { cell ->
//                    val (x, y) = cell
//                    if(computeEffectiveArmorAroundIndex(grid, x, y) > dmgMult * 2f * dmg ) return null
//                } ?: return null
//            }
//        }

        damage.modifier?.modifyMult(MODIFIER_ID, 1f - DAMAGE_SHARED_MULT)
        val loc = shipTarget.getNearestPointOnBounds(target.location)
        Global.getCombatEngine().applyDamage(shipTarget, shipTarget.location, dmg * DAMAGE_SHARED_MULT, damage.type ?: DamageType.ENERGY, 0f, true, false, target)
        // shipTarget.hitpoints = max(1f, shipTarget.hitpoints - dmg * DAMAGE_SHARED_MULT)
        return MODIFIER_ID
    }
}