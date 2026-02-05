package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.shipsystems.spooky.SpookyMindControl
import tecrys.svc.shipsystems.spooky.SpookyPlayerImpl
import tecrys.svc.shipsystems.spooky.mindControl
import tecrys.svc.shipsystems.spooky.sabotageCrew
import tecrys.svc.shipsystems.spooky.sabotageDrive
import tecrys.svc.shipsystems.spooky.sabotageWeapons
import java.awt.Color

class SpookyPlayerGui(private val guiShower: SpookyGuiShower, private val targetShip: ShipAPI): MagicCombatGuiBase(spookyGuiLayout) {

    init {
        val mindControlAction = object : MagicCombatButtonAction {
            override fun execute() {
                mindControl(targetShip, durationMultiplier = 1.0f)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
            }
        }
        val sabotageWeaponsAction = object : MagicCombatButtonAction {
            override fun execute() {
                sabotageWeapons(targetShip, chance = 0.7f)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
            }
        }
        val sabotageDriveAction = object : MagicCombatButtonAction {
            override fun execute() {
                sabotageDrive(targetShip, chance = 1.0f)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
            }
        }
        val sabotageCrewAction = object : MagicCombatButtonAction {
            override fun execute() {
                sabotageCrew(targetShip, amount = 0.4f)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
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
        return ""
    }
}