package tecrys.svc

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import tecrys.svc.shipsystems.AggressivePheromones
import tecrys.svc.utils.postRender
import tecrys.svc.utils.preRender
import tecrys.svc.utils.setColor
import java.awt.Color
import kotlin.math.sin

class CombatPlugin : BaseEveryFrameCombatPlugin() {

    private var pulseTimer = 0f
    companion object{
        data class AuraInfo(val center: Vector2f, val radius: Float, val color: Color)
        val aurasToRenderOneFrame = mutableListOf<AuraInfo>()
        private const val CIRCLE_POINTS = 50
        private fun multiplyAlpha(color: Color, mult: Float): Color{
            return color.setAlpha((color.alpha * mult).toInt())
        }
    }
    override fun renderInWorldCoords(viewport: ViewportAPI?) {
        val viewMult = viewport?.viewMult ?: return
        pulseTimer += 0.005f
        if(pulseTimer >= 1000000000f) pulseTimer = 0f
        preRender()

        aurasToRenderOneFrame.forEach {
            val lineColor = it.color
            val fillColor = multiplyAlpha(it.color, 0.1f + 0.03f * sin(5f * pulseTimer))//it.color.setAlpha(20 + (7f * sin(5f * pulseTimer)).toInt())
            setColor(lineColor)
            DrawUtils.drawCircle(
                viewport.convertWorldXtoScreenX(it.center.x),
                viewport.convertWorldYtoScreenY(it.center.y),
                it.radius / viewMult,
                CIRCLE_POINTS,
                false
            )
            setColor(fillColor)
            DrawUtils.drawCircle(
                viewport.convertWorldXtoScreenX(it.center.x),
                viewport.convertWorldYtoScreenY(it.center.y),
                it.radius / viewMult,
                CIRCLE_POINTS,
                true
            )
            for(i in 1 until 6){
                val pulseColor = multiplyAlpha(it.color, 0.3f - i.toFloat() * 0.03f)
                setColor(pulseColor)
                DrawUtils.drawCircle(
                    viewport.convertWorldXtoScreenX(it.center.x),
                    viewport.convertWorldYtoScreenY(it.center.y),
                    it.radius / viewMult * ((pulseTimer * i.toFloat() + 0.02f * Math.random().toFloat()) % 1f),
                    CIRCLE_POINTS,
                    false
                )
            }

        }
        postRender()
        aurasToRenderOneFrame.clear()
    }
}