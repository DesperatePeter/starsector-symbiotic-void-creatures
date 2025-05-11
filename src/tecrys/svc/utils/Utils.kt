package tecrys.svc.utils

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.ui.impl.a
import org.lazywizard.lazylib.FastTrig
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getAngleDiff
import org.magiclib.util.MagicRender
import tecrys.svc.*
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

const val degToRad: Float = PI.toFloat() / 180f
fun vectorFromAngleDeg(angle: Float): Vector2f {
    return Vector2f(cos(angle * degToRad), sin(angle * degToRad))
}

operator fun Vector2f.times(vector2f: Vector2f?): Float {
    return x * (vector2f?.x ?: 0f) + y * (vector2f?.y ?: 0f)
}

operator fun Float.times(vec: Vector2f?): Vector2f{
    vec ?: return Vector2f()
    return Vector2f(vec.x * this, vec.y * this)
}

fun Color.randomlyVaried(variation: Float): Color{
    fun d(c: Int) = MathUtils.clamp(c + (2f * (Math.random() - 0.5) * variation).toInt(), 0, 255)
    return Color( d(red),  d(green), d(blue), alpha)
}

fun showNotificationOnCampaignUi(text: String, spriteName: String){
    val intel = MessageIntel(text, Misc.getBasePlayerColor())
    intel.icon = spriteName
    Global.getSector().campaignUI.addMessage(intel)
}

fun renderCustomAfterimage(ship: ShipAPI, color: Color, duration: Float) { // idk why i put this here
    val sprite = ship.spriteAPI
    val offsetX = sprite.width / 2f - sprite.centerX
    val offsetY = sprite.height / 2f - sprite.centerY
    val trueOffsetX = FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetX - FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetY
    val trueOffsetY = FastTrig.sin(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetX + FastTrig.cos(Math.toRadians((ship.facing - 90f).toDouble())).toFloat() * offsetY
    MagicRender.battlespace(
        Global.getSettings().getSprite(ship.hullSpec.spriteName),
        Vector2f(ship.location.getX() + trueOffsetX, ship.location.getY() + trueOffsetY),
        Vector2f(0f, 0f),
        Vector2f(ship.spriteAPI.width, ship.spriteAPI.height),
        Vector2f(0f, 0f),
        ship.facing - 90f,
        0f,
        color,
        true,
        0f,
        0f,
        0f,
        0f,
        0f,
        0.01f,
        0.1f,
        duration,
        CombatEngineLayers.BELOW_SHIPS_LAYER
    )
}

fun unlockVoidlingRecovery(){
    if(!canRecoverVoidlings) return
    Global.getSettings().allShipHullSpecs.filter {
        it.tags.contains(SVC_VARIANT_TAG)
    }.filter {
        !it.tags.contains(SVC_ALPHA_TAG) || canRecoverAlphas || !it.tags.contains(SVC_SPECIAL_TAG)
    }.forEach {
        it.hints.remove(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE)
    }
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
        val distance = (abs(x - x2) * abs(y - y2)) + (abs(y - y2) * abs(y - y2))
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
        return normalizedTargetFacing
    }
    val otherWayShorter = abs(normalizedTargetFacing - facing) > 180f + 10f // stop deadlock by adding some tolerance
    val positiveDirection = normalizedTargetFacing > facing
    facing += maxDelta * (!otherWayShorter).toFloat() * positiveDirection.toFloat()
    return Misc.normalizeAngle(facing)
}

fun giveSpecialItemToPlayer(id: String, data: String, textPanel: TextPanelAPI?){
    val item = SpecialItemData(id, data)
    val fakeCargo = Global.getFactory().createCargo(true)
    fakeCargo.addSpecial(item, 1f)
    val itemName = fakeCargo.stacksCopy[0]?.displayName ?: id
    textPanel?.run {
        setFontSmallInsignia()
        addParagraph("Gained $itemName", Misc.getPositiveHighlightColor())
        highlightInLastPara(Misc.getHighlightColor(), itemName)
        setFontInsignia()
    }
    Global.getSector()?.playerFleet?.cargo?.addSpecial(item, 1f)
}

fun randomizeColor(color: Color, amount: Int): Color {
    return Color(
        (color.red + Random.nextInt(-amount, amount + 1)).coerceIn(0, 255),
        (color.green + Random.nextInt(-amount, amount + 1)).coerceIn(0, 255),
        (color.blue + Random.nextInt(-amount, amount + 1)).coerceIn(0, 255),
        color.alpha
    )
}

private fun generateArc(arc: Float, facing: Float): Pair<Float, Float>{
    var startAngle = Misc.normalizeAngle(facing - arc / 2f)

    if (startAngle < 0f) {
        startAngle += 360f;
    }

    var endAngle = Misc.normalizeAngle(facing + arc / 2f)

    if (endAngle > 360f) {
        endAngle -= 360f;
    }

    return startAngle to endAngle
}

private fun calculateAngle(from: Vector2f, to: Vector2f): Float {
    val dx = to.x - from.x
    val dy = to.y - from.y
    return Misc.normalizeAngle((atan2(dy.toDouble(), dx.toDouble()) * 180 / Math.PI).toFloat())
}

fun getProjectilesWithinRangeArc(location: Vector2f, range: Float, arc: Float, facing: Float): MutableList<DamagingProjectileAPI> {
    val projectiles: MutableList<DamagingProjectileAPI> = ArrayList()
    val (startAngle, endAngle) = generateArc(arc, facing)

    for (tmp in Global.getCombatEngine().projectiles) {
        if (tmp !is MissileAPI && MathUtils.isWithinRange(tmp.location, location, range)) {
            // Calculate angle from location to projectile
            val angleToProjectile = calculateAngle(location, tmp.location)
            
            // For incoming projectiles, we need to check if they're coming towards us
            // Which means they're in the opposite direction (180° difference)
            val isInArc = if (startAngle > endAngle) {
                // Arc wraps around 0/360
                (angleToProjectile >= startAngle || angleToProjectile <= endAngle)
            } else {
                // Normal arc
                (angleToProjectile >= startAngle && angleToProjectile <= endAngle)
            }
            
            if (isInArc) {
                projectiles.add(tmp)
            }
        }
    }

    return projectiles
}

fun getMissilesWithinRangeArc(location: Vector2f, range: Float, arc: Float, facing: Float): MutableList<MissileAPI> {
    val missiles: MutableList<MissileAPI> = arrayListOf()
    val (startAngle, endAngle) = generateArc(arc, facing)
    val iter = Global.getCombatEngine().missileGrid.getCheckIterator(location, range * 2.0f, range * 2.0f)

    while (iter.hasNext()) {
        val tmp = iter.next() as MissileAPI
        if (MathUtils.isWithinRange(tmp.location, location, range)) {
            // Calculate angle from location to missile
            val angleToMissile = calculateAngle(location, tmp.location)
            
            // For incoming missiles, we need to check if they're coming towards us
            // Which means they're in the opposite direction (180° difference)
            val isInArc = if (startAngle > endAngle) {
                // Arc wraps around 0/360
                (angleToMissile >= startAngle || angleToMissile <= endAngle)
            } else {
                // Normal arc
                (angleToMissile >= startAngle && angleToMissile <= endAngle)
            }
            
            if (isInArc) {
                missiles.add(tmp)
            }
        }
    }

    return missiles
}