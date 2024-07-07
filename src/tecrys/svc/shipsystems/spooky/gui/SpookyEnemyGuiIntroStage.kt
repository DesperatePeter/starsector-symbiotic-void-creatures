package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.CombatPlugin

class SpookyEnemyGuiIntroStage(private val guiShower: SpookyGuiShower): MagicCombatGuiBase(spookyGuiLayout) {

    init {
        val sleepAction = object : MagicCombatButtonAction{
            override fun execute() {
                Thread.sleep(1200)
                Global.getCombatEngine().combatUI?.addMessage(0, "java.nio.file.FileSystemException - LOGSWONTHELPYOU")
            }
        }
        val uninstallAction = object : MagicCombatButtonAction{
            override fun execute() {
                val dir = System.getProperty("user.dir") ?: "C:/Games/Starsector/"
                Global.getCombatEngine().combatUI?.run {
                    addMessage(0, "Removing $dir/data")
                    addMessage(0, "Removing $dir/graphics")
                    addMessage(0, "Removing $dir/saves")
                    addMessage(0, "Removing $dir/mods")
                    addMessage(0, "Removing $dir/sounds")
                    addMessage(0, "Successfully uninstalled Starsector!")
                    addMessage(0, "Thank you for your cooperation!")
                }
                CombatPlugin.shouldRenderBlackout = true
                guiShower.exit()
            }
        }
        val glitchAction = object : MagicCombatButtonAction{
            override fun execute() {
                CombatPlugin.shouldRenderGlitch = true
                guiShower.exit()
            }
        }
        addButton(uninstallAction, "OK", "OBEYME")
        addButton(glitchAction, "Cancel", "RESISTANCEISFUTILE")
        addButton(sleepAction, "Save logs", "THATWONTSAVEYOU")
        guiShower.shouldPreventPause = true
    }

    override fun getTitleString(): String {
        return "Please close the application and uninstall the game. Would you like to uninstall Starsector?"
    }

    override fun getMessageString(): String {
        return "9090202 [main] ERROR com.fs.starfarer.combat.SpookyMain - void.brain.YouShouldNotHaveComeHereException" +
                "\nvoid.brain.YouShouldNotHaveComeHereException" +
                "\n  at com.fs.starfarer.captain.brainfunction.produceIndependentThought(SynapseActivity.java:42)" +
                "\n  at com.fs.starfarer.independent.thought.haveFreeWill(MindControl.java:1337)" +
                "\n  at unknown.unknown.void.unknown.volition(Unknown Source)" +
                "\n  at unknown.thiswasamistake.o0Oo00o.unknown(Unknown unknown: unknown)"
    }
}