package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.shipsystems.spooky.SpookyMindControl
import java.awt.Color

class SpookyPlayerGui(private val guiShower: SpookyGuiShower, private val targetShip: ShipAPI): MagicCombatGuiBase(spookyGuiLayout) {

    init {
        fun floatyText(txt: String){
            Global.getCombatEngine()?.addFloatingText(targetShip.location, txt,
                28f, Color.RED, targetShip, 2f, 4.0f)
        }
        val mindControlAction = object : MagicCombatButtonAction {
            override fun execute() {
                Global.getCombatEngine()?.addPlugin(SpookyMindControl(targetShip))
                floatyText("Insane Captain")
                guiShower.exit()
            }
        }
        val sabotageWeaponsAction = object : MagicCombatButtonAction {
            override fun execute() {
                targetShip.allWeapons?.filter { Math.random() > 0.5f }?.forEach { w -> targetShip.applyCriticalMalfunction(w, false) }
                floatyText("Weapons Crew Compromised")
                guiShower.exit()
            }
        }
        val sabotageDriveAction = object : MagicCombatButtonAction {
            override fun execute() {
                targetShip.engineController?.shipEngines?.forEach { e -> targetShip.applyCriticalMalfunction(e, false) }
                floatyText("Rogue Engineer")
                guiShower.exit()
            }
        }
        val sabotageCrewAction = object : MagicCombatButtonAction {
            override fun execute() {
                targetShip.currentCR = targetShip.currentCR.minus(0.25f).coerceIn(0f, 100f)
                floatyText("Violent Mutiny")
                guiShower.exit()
            }
        }
        addButton(sabotageDriveAction, "Drive", "Fire in the reactor room!")
        addButton(mindControlAction, "Captain", "High voltage in the captain's mind!")
        addButton(sabotageCrewAction, "Crew", "Cause a riot!")
        addButton(sabotageWeaponsAction, "Weapons", "Danger danger!")
    }

    override fun getTitleString(): String {
        return "OooOoh theIr bRainS aRe RIpE and deLICIous!"
    }

    override fun getMessageString(): String {
        return "Choose an effect to apply"
    }
}