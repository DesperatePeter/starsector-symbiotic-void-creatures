package tecrys.svc.utils

import com.fs.starfarer.api.combat.ArmorGridAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipwideAIFlags
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getAngleDiff
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

const val degToRad: Float = PI.toFloat() / 180f
fun vectorFromAngleDeg(angle: Float): Vector2f {
    return Vector2f(cos(angle * degToRad), sin(angle * degToRad))
}

fun ShipAPI.getEffectiveShipTarget(fallbackRange: Float = 600f): ShipAPI?{
    shipTarget?.let { return it }
    (aiFlags?.getCustom(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) as? ShipAPI)?.let { return it }
    location?.let { loc ->
        return CombatUtils.getShipsWithinRange(loc, fallbackRange).filterNotNull().filter {
            it.owner != owner && it.owner != 100
        }.minByOrNull { (loc - it.location).length() }
    }
    return null
}

fun computeEffectiveArmorAroundIndex(armor: ArmorGridAPI, x: Int, y: Int) : Float{
    fun getWeighted(x2: Int, y2: Int): Float{
        val a = armor.getArmorValue(x2, y2)
        val distance = (abs(x - x2) * abs(x - x2)) + (abs(y - y2) * abs(y - y2))
        return when{
            distance <= 2 -> a
            distance <= 4 -> 0.5f * a
            else -> 0f
        }
    }
    var toReturn = 0f
    for(x2 in x - 2 until x + 3){
        for(y2 in y - 2 until y + 3){
            toReturn += getWeighted(x2, y2)
        }
    }
    return toReturn
}

fun adjustFacing(currentFacing: Float, targetFacing: Float, maxDelta: Float): Float{
    var facing = currentFacing
    val normalizedTargetFacing = Misc.normalizeAngle(targetFacing)
    if(normalizedTargetFacing.getAngleDiff(facing) < maxDelta){
        return targetFacing
    }
    val otherWayShorter = abs(normalizedTargetFacing - facing) > 180f
    val positiveDirection = targetFacing > facing
    facing += maxDelta * otherWayShorter.toFloat() * positiveDirection.toFloat()
    return facing
}