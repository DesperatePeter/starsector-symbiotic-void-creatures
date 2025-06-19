package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.magiclib.combatgui.MagicCombatGuiBase
import tecrys.svc.CombatPlugin

class SpookyGuiShower(private val ship: ShipAPI, var gui: MagicCombatGuiBase? = null): BaseEveryFrameCombatPlugin() {

    var isRunning = false
    var shouldDistort = false
    private val distortionTimer = IntervalUtil(1f, 1f)

    init {
        distortionTimer.advance(1f)
    }

    override fun renderInUICoords(viewport: ViewportAPI?) {
        gui?.render()
    }
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(!ship.isAlive){
            exit()
            return
        }
        Global.getCombatEngine().viewport.isExternalControl = true;
        gui?.advance()
        if(shouldDistort){
            distortionTimer.advance(amount)
            if(distortionTimer.intervalElapsed()){
                val ship = Global.getCombatEngine()?.playerShip ?: return
                DistortionShader.addDistortion(RippleDistortion(ship.location, ship.velocity).apply {
                    size = 10000f
                    //  intensity = ship.shieldRadiusEvenIfNoShield * 2f
                    // arcAttenuationWidth = 450f
                    this.intensity = 400f
                    fadeInSize(0.15f)
                    fadeOutIntensity(0.7f)
                })
            }
        }
    }

    fun exit(){
        // Global.getCombatEngine().isPaused = false;
        Global.getCombatEngine().viewport.isExternalControl = false;
        Global.getCombatEngine()?.removePlugin(this)
        shouldDistort = false
        isRunning = false
        gui = null
    }

    fun start(){
        isRunning = true
        CombatPlugin.shouldRenderLowIntensityGlitch = true
        Global.getCombatEngine()?.addPlugin(this)
    }


}