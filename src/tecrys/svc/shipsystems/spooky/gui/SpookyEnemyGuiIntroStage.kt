package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.mission.FleetSide
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction

class SpookyEnemyGuiIntroStage(exitFun: () -> Unit): MagicCombatGuiBase(spookyGuiLayout) {

    init {
        val obeyAction = object : MagicCombatButtonAction{
            override fun execute() {
                exitFun()
            }
        }
        val uninstallAction = object : MagicCombatButtonAction{
            override fun execute() {
                val dir = System.getProperty("user.dir") ?: "C:/Games/Starsector/"
                Global.getCombatEngine().combatUI?.addMessage(0,"Removing $dir/data")
                Global.getCombatEngine().combatUI?.addMessage(0,"Removing $dir/graphics")
                Global.getCombatEngine().combatUI?.addMessage(0,"Removing $dir/saves")
                Global.getCombatEngine().combatUI?.addMessage(0,"Removing $dir/mods")
                Global.getCombatEngine().combatUI?.addMessage(0,"Removing $dir/sounds")
                Global.getCombatEngine().combatUI?.addMessage(0, "Successfully uninstalled Starsector!")
                exitFun()
            }
        }
        addButton(uninstallAction, "OK", "OBEYME")
        addButton(obeyAction, "Cancel", "RESISTANCEISFUTILE")
        addButton(obeyAction, "Save logs", "THATWONTSAVEYOU")
    }

    override fun getTitleString(): String {
        return "Please close the application and uninstall the game. You should not have come here."
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