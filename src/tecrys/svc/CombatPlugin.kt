package tecrys.svc

import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.opengl.DrawUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import tecrys.svc.shipsystems.spooky.GlitchRenderer
import tecrys.svc.utils.isMastermindFleet
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
    private var glitchRenderer: GlitchRenderer? = null

    companion object {

        // constants
        private const val CIRCLE_POINTS = 50
        const val IS_BATTLE_THEME_PLAYING_MEM_KEY = "\$SVC_BATTLE_MUSIC_PLAYING"
        const val BLACKOUT_PROGRESS_MULTIPLIER = 0.3f
        const val BLACKOUT_END = 2.4f

        // external interface
        data class AuraInfo(val center: Vector2f, val radius: Float, val color: Color)

        val aurasToRenderOneFrame = mutableListOf<AuraInfo>()
        var shouldRenderBlackout = false
        var shouldRenderGlitch = false
        var shouldRenderLowIntensityGlitch = false
        var shouldPreventPauseFor = 0f
        var preventPauseMessage = ""
        fun multiplyAlpha(color: Color, mult: Float): Color {
            return color.setAlpha((color.alpha * mult).toInt())
        }
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        // skip when on title screen
        if (Global.getCurrentState() == GameState.TITLE) wasFirstSuccessfulAdvanceCall = true
        shouldPreventPauseFor -= amount
        if(shouldPreventPauseFor > 0f && Global.getCombatEngine().isPaused){
            Global.getCombatEngine().isPaused = false
            Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1f, 1f)
            Global.getCombatEngine().combatUI?.addMessage(0, preventPauseMessage)
        }
        advanceBlackout(amount)
        if(shouldRenderGlitch && glitchRenderer == null){
            glitchRenderer = GlitchRenderer()
            shouldRenderGlitch = false
        }
        if(shouldRenderLowIntensityGlitch && glitchRenderer == null){
            glitchRenderer = GlitchRenderer(0.1f, 0.2f, 0.0f)
            shouldRenderLowIntensityGlitch = false
        }
        if(glitchRenderer?.isFinished() == true){
            glitchRenderer = null
        }
        glitchRenderer?.advance(amount)
        if (!wasFirstSuccessfulAdvanceCall) {
            val enemyFleet = Global.getCombatEngine()?.getFleetManager(FleetSide.ENEMY)?.deployedCopy?.filterNotNull()
                ?.firstOrNull()?.fleetData?.fleet ?: return
            val enemyFaction = enemyFleet.faction ?: return
            wasFirstSuccessfulAdvanceCall = true
            if(enemyFleet.isMastermindFleet()){
                Global.getSoundPlayer().playCustomMusic(1, 1, "svc_voidling_battle_theme_glitched", true)
                Global.getSector().memoryWithoutUpdate[IS_BATTLE_THEME_PLAYING_MEM_KEY] = true
                return
            }
            if (enemyFaction == Global.getSector().getFaction(SVC_FACTION_ID) || enemyFaction == Global.getSector().getFaction(MMM_FACTION_ID)) {
                Global.getSoundPlayer().playCustomMusic(1, 1, "svc_voidling_battle_theme", true)
                Global.getSector().memoryWithoutUpdate[IS_BATTLE_THEME_PLAYING_MEM_KEY] = true
                return
            }
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
        glitchRenderer?.render()
    }

    private fun renderAuras(viewport: ViewportAPI?) {
        val viewMult = viewport?.viewMult ?: return
        preRender()
        aurasToRenderOneFrame.forEach {
            val scaling = Global.getSettings().screenScaleMult

            val lineColor = it.color
            val fillColor = multiplyAlpha(
                it.color,
                0.1f + 0.03f * sin(5f * pulseTimer)
            )//it.color.setAlpha(20 + (7f * sin(5f * pulseTimer)).toInt())
            setColor(lineColor)
            DrawUtils.drawCircle(
                viewport.convertWorldXtoScreenX(it.center.x)  * scaling,
                viewport.convertWorldYtoScreenY(it.center.y) * scaling,
                it.radius / viewMult * scaling,
                CIRCLE_POINTS,
                false
            )
            setColor(fillColor)
            DrawUtils.drawCircle(
                viewport.convertWorldXtoScreenX(it.center.x) * scaling,
                viewport.convertWorldYtoScreenY(it.center.y) * scaling,
                it.radius / viewMult * scaling,
                CIRCLE_POINTS,
                true
            )
            for (i in 1 until 6) {
                val pulseColor = multiplyAlpha(it.color, 0.3f - i.toFloat() * 0.03f)
                setColor(pulseColor)
                DrawUtils.drawCircle(
                    viewport.convertWorldXtoScreenX(it.center.x) * scaling,
                    viewport.convertWorldYtoScreenY(it.center.y) * scaling,
                    it.radius / viewMult * ((pulseTimer * i.toFloat() + 0.02f * Math.random().toFloat()) % 1f) * scaling,
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
}