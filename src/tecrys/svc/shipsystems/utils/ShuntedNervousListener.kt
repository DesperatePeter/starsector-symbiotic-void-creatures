package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.sqrt

class ShuntedNervousListener: DamageTakenModifier {

    companion object{
        const val MULT_ID = "svc_shunted_nerves"
    }

    data class DelayedDamageInfo(val relPoint: Vector2f, val shipFacing: Float, val damage: Float,
                                 val type: DamageType, val source: Any?)

    private val delayedDamageInstances = mutableListOf<DelayedDamageInfo>()
    override fun modifyDamageTaken(
        param: Any?,
        target: CombatEntityAPI?,
        damage: DamageAPI?,
        point: Vector2f?,
        shieldHit: Boolean
    ): String? {
        val ship = target as? ShipAPI ?: return null
        if(point == null || damage == null) return null
        if(shieldHit) return null
        val relPoint = point - ship.location
        val dmg = if(damage.isDps) damage.computeDamageDealt(0.1f) else damage.damage
        delayedDamageInstances.add(DelayedDamageInfo(relPoint, ship.facing, dmg, damage.type, param))
        damage.modifier.modifyMult(MULT_ID, 0f)
        return MULT_ID
    }

    /**
     * make sure to call AFTER removing the listener!
     */
    fun applyDelayedDamaged(ship: ShipAPI){
        val engine = Global.getCombatEngine() ?: return
        val instances = delayedDamageInstances.toList()
        instances.forEach {
            ship.exactBounds.update(ship.location, ship.facing)
            val relPos = VectorUtils.rotate(it.relPoint, ship.facing - it.shipFacing)
            engine.addHitParticle(relPos + ship.location, ship.velocity, 10f + 2f*sqrt(it.damage), 10f, Color.YELLOW)
            engine.applyDamage(ship, relPos + ship.location, it.damage, it.type, 0f, true, false, null, false)
        }
        delayedDamageInstances.clear()
    }
}