package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.CombatPlugin.Companion.multiplyAlpha
import tecrys.svc.utils.*

import java.awt.Color

class GlitchRenderer(private val intensityModifier: Float = 1f, durationModifier: Float = 1f, private val disableControlChance: Float = 0.4f) {

    companion object{
        const val GLITCH_PROGRESS_MULTIPLIER = 0.75f
        const val MAX_GLITCH_EFFECTS = 800
        const val MAX_INTENSITY = 12f
        private fun randomGlitchColor(intensity: Float): Color {
            val greyChance = 0.1f + 0.8f * intensity / MAX_INTENSITY
            return if(Math.random() > greyChance) Color((50..150).random(), (0..50).random(), (150..255).random(), 255)
            else Color((200..255).random(), (200..255).random(), (200..255).random(), 255)
        }
    }

    private val maxIntensity = MAX_INTENSITY * durationModifier

    private var glitchIntensity = 0f
    private val adjustedIntensity: Float
        get() = glitchIntensity * (1.5f - (Global.getCombatEngine()?.playerShip?.hullLevel ?: 1f)) * intensityModifier
    private var glitchTimer = IntervalUtil(0.3f, 0.6f)

    data class GlitchInfo(val x1: Vector2f, val x2: Vector2f, val color: Color)

    private var glitches = generateGlitches(0.1f)

    fun isFinished(): Boolean = glitchIntensity >= maxIntensity

    fun advance(amount: Float){
        advanceGlitch(amount)
        glitchTimer.advance(amount)
        if(glitchTimer.intervalElapsed()){
            if(Math.random() > 0.9f){
                glitches = emptyList()
            }else{
                glitches = generateGlitches(adjustedIntensity)
            }
            playGlitchSound(adjustedIntensity)
        }
    }

    fun render(){
        renderGlitchEffects(adjustedIntensity)
    }

    private fun generateGlitch(intensity: Float): GlitchInfo{
        val length = screenWidth * 0.1f * Math.random().toFloat()
        val x1 = Vector2f(Math.random().toFloat() * (screenWidth - length),  Math.random().toFloat() * screenHeight)
        return GlitchInfo(
            x1,
            Vector2f(x1.x + length, x1.y),
            randomGlitchColor(intensity)
        )
    }

    private fun generateGlitches(intensity: Float) : List<GlitchInfo>{
        val numGlitches = (intensity/ maxIntensity * 0.9f + Math.random().toFloat() * 0.1f) * MAX_GLITCH_EFFECTS.toFloat()
        return (0 until numGlitches.toInt()).map { _ -> generateGlitch(intensity) }
    }

    private fun playGlitchSound(intensity: Float){
        val volume = 0.4f + 0.6f * intensity / maxIntensity
        Global.getSoundPlayer().playUISound("svc_glitch", (Math.random().toFloat() * 0.5f + 0.5f), volume )
    }

    private fun renderGlitchEffects(intensity: Float) {
        preRender()
        val lineWidth = 1f + 6f * (intensity/ maxIntensity) + 3f * Math.random().toFloat()
        GL11.glLineWidth(lineWidth)
        GL11.glBegin(GL11.GL_LINES)

        glitches.forEach { glitch ->
            val color = glitch.color
            val xOffset = Math.random().toFloat() * intensity
            if(Math.random() > 0.3f){
                setColor(multiplyAlpha(color, (0.5f + Math.random().toFloat()*0.5f) * (glitchIntensity/ maxIntensity * 0.5f + 0.5f)))
                GL11.glVertex2f(glitch.x1.x + xOffset, glitch.x1.y)
                GL11.glVertex2f(glitch.x2.x + xOffset, glitch.x2.y)
            }
        }
        GL11.glEnd()

        postRender()
    }
    private fun advanceGlitch(amount: Float) {
        if(Global.getCombatEngine()?.isPaused == true) return
        glitchIntensity += GLITCH_PROGRESS_MULTIPLIER * amount
        if(Math.random() < disableControlChance){
            Global.getCombatEngine().combatUI?.isDisablePlayerShipControlOneFrame = true
        }
    }
}