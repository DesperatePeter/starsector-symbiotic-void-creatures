package tecrys.svc.hullmods.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AcidicBloodListener(private val ship: ShipAPI): DamageTakenModifier {
    companion object{
        const val DAMAGE_TAKEN_FOR_GUARANTEED_TRIGGER = 400f
        const val MAX_ARC_RANGE = 300f
        val DAMAGE_DEALT = mapOf(
            HullSize.FIGHTER to 50f,
            HullSize.FRIGATE to 100f,
            HullSize.DESTROYER to 200f,
            HullSize.CRUISER to 350f,
            HullSize.CAPITAL_SHIP to 500f
        )
        val ARC_CORE_COLOR = Color.GREEN
        val ARC_FRINGE_COLOR = Color.YELLOW
    }

    private val damageAmount = DAMAGE_DEALT[ship.hullSize] ?: 50f

    // ... inside AcidicBloodListener ...
    override fun modifyDamageTaken(param: Any?,
                                   target: CombatEntityAPI?,
                                   damage: DamageAPI?,
                                   point: Vector2f?,
                                   shieldHit: Boolean): String? {
        if(shieldHit || ship.hullLevel >= 1f || damage == null || point == null) return null

        val triggerChance = damage.damage / DAMAGE_TAKEN_FOR_GUARANTEED_TRIGGER
        if(Math.random() > triggerChance) return null

        val engine = Global.getCombatEngine() ?: return null

        // Scan only ships (which includes fighters), drastically reducing the search pool
        val validTargets = engine.ships.filter {
            it.owner != 100 && it.owner != ship.originalOwner && !it.isHulk &&
                    MathUtils.getDistanceSquared(it.location, point) <= MAX_ARC_RANGE * MAX_ARC_RANGE
        }

        if (validTargets.isNotEmpty()) {
            val randomTarget = validTargets.random()
            engine.spawnEmpArc(
                ship, point, ship, randomTarget, DamageType.ENERGY, damageAmount, 0f, MAX_ARC_RANGE + 100f, null, 5f,
                ARC_CORE_COLOR, ARC_FRINGE_COLOR
            )
        }
        return null
    }
}