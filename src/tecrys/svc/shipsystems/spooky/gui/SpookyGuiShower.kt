package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.magiclib.combatgui.MagicCombatGuiBase
import tecrys.svc.CombatPlugin
import tecrys.svc.utils.executeAfterNFrames

class SpookyGuiShower(private val ship: ShipAPI, var gui: MagicCombatGuiBase? = null, private val shouldSuppressLeftMouse: Boolean = true):
    BaseEveryFrameCombatPlugin() {

    var isRunning = false
    var shouldDistort = false
    private val distortionTimer = IntervalUtil(1f, 1f)
    private val selectedWeaponGroupIdx = ship.weaponGroupsCopy?.indexOf(ship.selectedGroupAPI) ?: 0
    private val wasAutofire = ship.selectedGroupAPI?.isAutofiring == true

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

    fun exit(shouldUnPause: Boolean = false) {
        if(shouldUnPause) Global.getCombatEngine().isPaused = false;
        val ce = Global.getCombatEngine()
        ce.viewport.isExternalControl = false
        ce.removePlugin(this)
        shouldDistort = false
        isRunning = false
        gui = null
    }

    fun start(shouldPause: Boolean = false){
        if(shouldPause) Global.getCombatEngine().isPaused = true;
        isRunning = true
        if(shouldSuppressLeftMouse){
            // Note that, if a player clicks, the button will be held down for more than a single frame
            // therefore, this needs to happen until the player releases the button
            ship.giveCommand(ShipCommand.SELECT_GROUP, null, ship.weaponGroupsCopy?.size ?: 9)
            if(wasAutofire){
                ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, selectedWeaponGroupIdx)
            }
            Global.getCombatEngine().addPlugin(object: BaseEveryFrameCombatPlugin(){
                override fun advance(
                    amount: Float,
                    events: List<InputEventAPI?>?
                ) {
                    val leftMouseEvents = events?.filterNotNull()?.filter { it.isLMBEvent } ?: return
                    if(leftMouseEvents.any { it.isLMBUpEvent}){
                        Global.getCombatEngine()?.let{ ce ->
                            ce.removePlugin(this)
                            ce.executeAfterNFrames(1){
                                ship.giveCommand(ShipCommand.SELECT_GROUP, null, selectedWeaponGroupIdx)
                                if(wasAutofire){
                                    ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, selectedWeaponGroupIdx)
                                }
                            }
                        }
                    }
                }
            })

// No idea why this doesn't work...
//            Global.getCombatEngine().addPlugin(object: BaseEveryFrameCombatPlugin(){
//                override fun advance(
//                    amount: Float,w
//                    events: List<InputEventAPI?>?
//                ) {
//                    val leftMouseEvents = events?.filterNotNull()?.filter { it.isLMBEvent && !it.isConsumed } ?: return
//                    leftMouseEvents.forEach { it.consume() }
//                    if(leftMouseEvents.any { it.isLMBUpEvent}){
//                        Global.getCombatEngine()?.removePlugin(this)
//                    }
//                }
//            })
        }

        CombatPlugin.shouldRenderLowIntensityGlitch = true
        Global.getCombatEngine()?.addPlugin(this)
    }


}