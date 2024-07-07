package tecrys.svc

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import tecrys.svc.utils.postRender
import tecrys.svc.utils.preRender
import tecrys.svc.utils.setColor
import java.awt.Color
import kotlin.math.sin

// FIXME: Split this class into multiple classes for all the different this this class does

class CombatPlugin : BaseEveryFrameCombatPlugin() {

    private var pulseTimer = 0f
    private var wasFirstSuccessfulAdvanceCall = false
    private var blackoutProgress = 0f
    private var glitchIntensity = 0f

    data class GlitchInfo(val x1: Vector2f, val x2: Vector2f, val color: Color)

    private var glitches = mutableListOf<GlitchInfo>()

    companion object {

        // constants
        private const val CIRCLE_POINTS = 50
        const val IS_BATTLE_THEME_PLAYING_MEM_KEY = "\$SVC_BATTLE_MUSIC_PLAYING"
        const val BLACKOUT_PROGRESS_MULTIPLIER = 0.3f
        const val BLACKOUT_END = 2.4f
        const val GLITCH_PROGRESS_MULTIPLIER = 0.75f
        const val GLITCH_END = 12f
        val screenWidth = Global.getSettings().screenWidth
        val screenHeight = Global.getSettings().screenHeight

        // external interface
        data class AuraInfo(val center: Vector2f, val radius: Float, val color: Color)

        val aurasToRenderOneFrame = mutableListOf<AuraInfo>()
        var shouldRenderBlackout = false
        var shouldRenderGlitch = false
        private fun multiplyAlpha(color: Color, mult: Float): Color {
            return color.setAlpha((color.alpha * mult).toInt())
        }

        private fun randomGlitchColor(): Color =
            Color((50..150).random(), (0..50).random(), (150..255).random(), 255)


    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        // skip when on title screen
        if (Global.getCurrentState() == GameState.TITLE) wasFirstSuccessfulAdvanceCall = true
        advanceBlackout(amount)
        advanceGlitch(amount)
        if (!wasFirstSuccessfulAdvanceCall) {
            val enemyFaction = Global.getCombatEngine()?.getFleetManager(FleetSide.ENEMY)?.deployedCopy?.filterNotNull()
                ?.firstOrNull()?.fleetData?.fleet?.faction
            wasFirstSuccessfulAdvanceCall = enemyFaction != null
            if (enemyFaction == Global.getSector().getFaction(SVC_FACTION_ID)) {
                Global.getSoundPlayer().playCustomMusic(1, 1, "svc_voidling_battle_theme", true)
                Global.getSector().memoryWithoutUpdate[IS_BATTLE_THEME_PLAYING_MEM_KEY] = true
            }
        }
    }

    private fun advanceGlitch(amount: Float) {
        if(Global.getCombatEngine()?.isPaused == true) return
        if (shouldRenderGlitch) {
            glitchIntensity += GLITCH_PROGRESS_MULTIPLIER * amount
            if(Math.random() < glitchIntensity / GLITCH_END / 15f){
                val volume = 0.3f + 0.4f * glitchIntensity / GLITCH_END + 0.2f * Math.random().toFloat()
                Global.getSoundPlayer().playUISound("svc_glitch", (Math.random().toFloat() * 0.5f + 0.5f), volume )
            }
        }
        if (glitchIntensity >= GLITCH_END) {
            glitchIntensity = 0f
            shouldRenderGlitch = false
        }
    }

    private fun advanceBlackout(amount: Float) {
        if(Global.getCombatEngine()?.isPaused == true) return

        if (shouldRenderBlackout) {
            Global.getCombatEngine()?.combatUI?.hideShipInfo()
            Global.getCombatEngine()?.combatUI?.isDisablePlayerShipControlOneFrame = true
            blackoutProgress += (amount * BLACKOUT_PROGRESS_MULTIPLIER)

        }
        if (blackoutProgress >= BLACKOUT_END) {
            shouldRenderBlackout = false
            blackoutProgress = 0f
            Global.getCombatEngine()?.combatUI?.reFanOutShipInfo()
        }
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {
        if (Global.getCombatEngine()?.isPaused != true) pulseTimer += 0.005f
        if (pulseTimer >= 1000000000f) pulseTimer = 0f

        renderAuras(viewport)
        if (shouldRenderBlackout) {
            renderBlackout(blackoutProgress)
        }
        if (shouldRenderGlitch) {
            renderGlitchEffects(glitchIntensity)
        }
    }

    private fun renderAuras(viewport: ViewportAPI?) {
        val viewMult = viewport?.viewMult ?: return
        preRender()
        aurasToRenderOneFrame.forEach {
            val lineColor = it.color
            val fillColor = multiplyAlpha(
                it.color,
                0.1f + 0.03f * sin(5f * pulseTimer)
            )//it.color.setAlpha(20 + (7f * sin(5f * pulseTimer)).toInt())
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
            for (i in 1 until 6) {
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
        if (Global.getCombatEngine()?.isPaused != true)
            aurasToRenderOneFrame.clear()
    }

    private fun renderBlackout(progress: Float) {
        preRender()
        val color = Color(0, 0, 0, 255)
        setColor(color)
        DrawUtils.drawCircle(100f, 100f, 10000f, 10, true)
        postRender()
    }

    private fun renderGlitchEffects(intensity: Float) {
        preRender()

        GL11.glLineWidth(intensity * 10f)
        GL11.glBegin(GL11.GL_LINES)
        (0 until ((intensity * 10f).toInt() + 1)).forEach { i ->
            while (i >= glitches.size) {
                glitches.add(
                    GlitchInfo(
                        Vector2f(Math.random().toFloat() * screenWidth, Math.random().toFloat() * screenHeight),
                        Vector2f(Math.random().toFloat() * screenWidth, Math.random().toFloat() * screenHeight),
                        randomGlitchColor()
                    )
                )
            }
            val color = glitches[i].color
            if(Math.random() > 0.3f){
                setColor(multiplyAlpha(color, (0.5f + Math.random().toFloat()*0.5f) * (glitchIntensity/ GLITCH_END * 0.5f + 0.5f)))
                GL11.glVertex2f(glitches[i].x1.x, glitches[i].x1.y)
                GL11.glVertex2f(glitches[i].x2.x, glitches[i].x2.y)
            }
        }
        GL11.glEnd()

        postRender()
    }
}