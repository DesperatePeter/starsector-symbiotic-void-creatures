package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.shipsystems.spooky.SpookyPlayerImpl
import tecrys.svc.shipsystems.spooky.plugins.Sabotage

class SpookyPlayerGui(private val guiShower: SpookyGuiShower, private val targetShip: ShipAPI): MagicCombatGuiBase(spookyGuiLayout) {

    init {
        val mindControlAction = object : MagicCombatButtonAction {
            override fun execute() {
                Sabotage.applyMindControl(targetShip)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
            }
        }
        val sabotageWeaponsAction = object : MagicCombatButtonAction {
            override fun execute() {
                Sabotage.applyWeaponSabotage(targetShip)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
            }
        }
        val sabotageDriveAction = object : MagicCombatButtonAction {
            override fun execute() {
                Sabotage.applyDriveSabotage(targetShip)
                guiShower.exit(SpookyPlayerImpl.SHOULD_UNPAUSE_ON_FINISH)
            }
        }
        val sabotageCrewAction = object : MagicCombatButtonAction {
            override fun execute() {
                Sabotage.applyCrewSabotage(targetShip)
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