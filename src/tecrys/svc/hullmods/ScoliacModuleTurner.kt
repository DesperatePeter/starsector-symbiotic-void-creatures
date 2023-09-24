package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.adjustFacing
import kotlin.math.sin

class ScoliacModuleTurner: BaseHullMod() {

    companion object{
        const val TARGETING_RANGE = 1500f
        const val BASE_WOBBLE_RATE = 0.8f // in rad/s
        const val WOBBLE_RATE_VELOCITY_SCALING = 1.5f / 100f
        const val BASE_WOBBLE_MAGNITUDE = 2f
        const val WOBBLE_MAGNITUDE_VELOCITY_SCALING = 1f / 100f
    }

    private val facingByModule = mutableMapOf<String, Float>()
    private val sinArgByModule = mutableMapOf<String, Float>().withDefault { 0f }
    private val originalOffsetByModule = mutableMapOf<String, Vector2f>().withDefault { Vector2f(0f, 0f) }


    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        ship ?: return
        if(!ship.isAlive) return
        val module = getModule(ship) ?: return
        doSinWobble(ship, module, amount)
        // Note: Starsector resets module facing every frame. So we cache facing and overwrite what Starsector does
        if(!facingByModule.contains(module.id)){
            facingByModule[module.id] = module.facing
        }
        module.hullSpec.moduleAnchor
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

    private fun doSinWobble(thisShip: ShipAPI, module: ShipAPI, amount: Float){
        if(!sinArgByModule.contains(module.id)){
            sinArgByModule[module.id] = 0f
        }
        if(!originalOffsetByModule.contains(module.id)){
            // module.ensureClonedStationSlotSpec()
            originalOffsetByModule[module.id] = module.location - thisShip.location
        }
        val sinArg = (sinArgByModule[module.id] ?: 0f) + amount * (BASE_WOBBLE_RATE + thisShip.velocity.length() * WOBBLE_RATE_VELOCITY_SCALING)
        sinArgByModule[module.id] = sinArg
        var originalOffset = originalOffsetByModule[module.id] ?: Vector2f()
        originalOffset = Vector2f(originalOffset.x, originalOffset.y).rotate(thisShip.facing -90f)
        var offsetVector = Vector2f(
            0f,
            sin(sinArg) * (BASE_WOBBLE_MAGNITUDE + thisShip.velocity.length() * WOBBLE_MAGNITUDE_VELOCITY_SCALING)
        )
        offsetVector = offsetVector.rotate(module.facing - 90f)
        Vector2f.add(thisShip.location + originalOffset, offsetVector, module.location)

    }

}