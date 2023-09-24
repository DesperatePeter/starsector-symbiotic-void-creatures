package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import tecrys.svc.utils.orientTowards
import kotlin.math.abs
import kotlin.math.min

class ScoliacModuleTurner: BaseHullMod() {

    companion object{
        const val TARGETING_RANGE = 800f
    }


    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        ship ?: return
        if(!ship.isAlive) return
        val module = getModule(ship) ?: return
        val maxDelta = module.maxTurnRate * amount
        selectTarget(ship)?.let { tgt ->
            module.orientTowards(Misc.getAngleInDegrees(tgt.location, module.location), maxDelta)
        } ?: run {
            module.orientTowards(ship.facing, maxDelta)
        }
    }

    private fun selectTarget(thisShip: ShipAPI): CombatEntityAPI? {
        thisShip.shipTarget?.let {
            return it
        }
        (thisShip.aiFlags?.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? CombatEntityAPI)?.let {
            return it
        }
        return CombatUtils.getShipsWithinRange(thisShip.location, TARGETING_RANGE)?.filter {
            it.originalOwner != thisShip.originalOwner && it.originalOwner != 100
        }?.minByOrNull { (thisShip.location - it.location).lengthSquared() }
    }

    private fun getModule(thisShip: ShipAPI): ShipAPI? {
        return thisShip.childModulesCopy?.firstOrNull()
    }

}