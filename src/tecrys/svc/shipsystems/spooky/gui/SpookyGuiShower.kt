package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.magiclib.combatgui.MagicCombatGuiBase

class SpookyGuiShower(var gui: MagicCombatGuiBase? = null): BaseEveryFrameCombatPlugin() {

    var isRunning = false

    override fun renderInUICoords(viewport: ViewportAPI?) {
        gui?.render()
    }
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(Global.getCombatEngine().isPaused){
            Global.getCombatEngine().isPaused = false
            Global.getCombatEngine().combatUI?.addMessage(0, "java.lang.IllegalTimeFlowException - YOUDONOTCONTROLTIME ICONTROLTIME")
            Global.getSoundPlayer().playUISound("ui_button_disabled_pressed", 1f, 1f)
        }
        Global.getCombatEngine().viewport.isExternalControl = true;
        gui?.advance()
    }

    fun exit(){
        // Global.getCombatEngine().isPaused = false;
        Global.getCombatEngine().viewport.isExternalControl = false;
        Global.getCombatEngine()?.removePlugin(this)
        isRunning = false
    }

    fun start(){
        isRunning = true
        Global.getCombatEngine()?.addPlugin(this)
    }


}