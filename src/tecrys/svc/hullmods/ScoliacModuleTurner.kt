package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import tecrys.svc.utils.adjustFacing
import tecrys.svc.utils.orientTowards
import kotlin.math.abs
import kotlin.math.min

class ScoliacModuleTurner: BaseHullMod() {

    companion object{
        const val TARGETING_RANGE = 800f
    }

    private val facingByModule = mutableMapOf<String, Float>()


    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        ship ?: return
        if(!ship.isAlive) return
        val module = getModule(ship) ?: return
        // Note: Starsector resets facing every frame. So we cache facing and overwrite what Starsector does
        if(!facingByModule.contains(module.id)){
            facingByModule[module.id] = module.facing
        }
        val originalFacing = facingByModule[module.id] ?: module.facing
        val maxDelta = module.maxTurnRate * amount
        selectTarget(ship)?.let { tgt ->
            facingByModule[module.id] = adjustFacing(originalFacing, Misc.getAngleInDegrees(module.location, tgt.location), maxDelta)
        } ?: run {
            facingByModule[module.id] = adjustFacing(originalFacing, ship.facing, maxDelta)
        }
        facingByModule[module.id]?.let {
            module.facing = it
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