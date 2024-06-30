package tecrys.svc.hullmods.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamageAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AcidicBloodListener(private val ship: ShipAPI): DamageTakenModifier {
    companion object{
        const val DAMAGE_TAKEN_FOR_GUARANTEED_TRIGGER = 500f
        const val MAX_ARC_RANGE = 300f
        val DAMAGE_DEALT = mapOf(
            HullSize.FIGHTER to 25f,
            HullSize.FRIGATE to 50f,
            HullSize.DESTROYER to 80f,
            HullSize.CRUISER to 150f,
            HullSize.CAPITAL_SHIP to 250f
        )
        val ARC_CORE_COLOR = Color.GREEN
        val ARC_FRINGE_COLOR = Color.YELLOW
    }

    private val damageAmount = DAMAGE_DEALT[ship.hullSize] ?: 50f

    override fun modifyDamageTaken(param: Any?,
                                   target: CombatEntityAPI?,
                                   damage: DamageAPI?,
                                   point: Vector2f?,
                                   shieldHit: Boolean): String? {
        if(shieldHit) return null
        if(ship.hullLevel >= 1f) return null
        damage ?: return null
        point ?: return null
        val triggerChance = damage.damage / DAMAGE_TAKEN_FOR_GUARANTEED_TRIGGER
        if(Math.random() > triggerChance) return null
        try {
            val targets = CombatUtils.getEntitiesWithinRange(point, MAX_ARC_RANGE).filter {
                it.owner != 100 && it.owner != ship.originalOwner }.random()?.let { target ->
                Global.getCombatEngine().spawnEmpArc(
                    ship, point, ship, target, DamageType.ENERGY, damageAmount, 0f, MAX_ARC_RANGE + 100f, null, 5f,
                    ARC_CORE_COLOR, ARC_FRINGE_COLOR
                )
            }
        }catch (e: NoSuchElementException){
            return null
        }
        return null
    }
}