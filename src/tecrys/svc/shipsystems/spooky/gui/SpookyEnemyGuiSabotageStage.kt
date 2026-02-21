package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.shipsystems.spooky.plugins.Sabotage
import java.util.Locale

class SpookyEnemyGuiSabotageStage(guiShower: SpookyGuiShower, private val useAltText: Boolean): MagicCombatGuiBase(spookyGuiLayout) {

    enum class SabotageType{
        WEAPONS, DRIVE, CREW
    }

    companion object{
        fun randomPlayerShip(): ShipAPI? = Global.getCombatEngine().ships.filter { it.owner == 0 && it != Global.getCombatEngine().playerShip }.randomOrNull()
    }

    class SabotageAction(private val guiShower: SpookyGuiShower, private val sabotages: List<SabotageType>) : MagicCombatButtonAction{
        private val pf: ShipAPI? = kotlin.run {
            val toReturn = Global.getCombatEngine().playerShip
            if(toReturn.fleetMember == null) randomPlayerShip() else toReturn
        }
        override fun execute() {
            sabotages.forEach { s ->
                when(s){
                    SabotageType.WEAPONS -> {
                        Sabotage.applyWeaponSabotage(pf)
                        Global.getCombatEngine().combatUI?.addMessage(0, "Your weapons have suffered damage")
                    }
                    SabotageType.DRIVE -> {
                        Sabotage.applyDriveSabotage(pf)
                        Global.getCombatEngine().combatUI?.addMessage(0, "Your engines have suffered damage")
                    }
                    SabotageType.CREW -> {
                        Sabotage.applyCrewSabotage(pf)
                        Global.getCombatEngine().combatUI?.addMessage(0, "You have lost combat readiness")
                    }
                }
            }
            randomPlayerShip()?.let { ship ->
                Sabotage.applyMindControl(pf)
                Global.getCombatEngine().combatUI?.addMessage(0, "${ship.name}: What is going on?? SHOOTTHETRAITORS!!")
            }
            guiShower.exit()
        }
    }

    init {
        guiShower.shouldDistort = true
        addButton(SabotageAction(guiShower, listOf(SabotageType.DRIVE, SabotageType.CREW)), "Protect bridge", "IAMINVINCIBLE")
        addButton(SabotageAction(guiShower, listOf(SabotageType.WEAPONS, SabotageType.CREW)), "Protect engineering", "YOUCANNOTRUN")
        addButton(SabotageAction(guiShower, listOf(SabotageType.WEAPONS, SabotageType.DRIVE)), "Protect crew", "YOUWILLBECONSUMED")
    }

    override fun getTitleString(): String {
        return "What futile resistance do you plan?"
    }

    override fun getMessageString(): String {
        return if(useAltText){
            val playerName = System.getProperty("user.name") ?: Global.getSector()?.playerPerson?.nameString ?: "YOU"
            val playerCountry = Locale.getDefault().displayCountry ?: "YOURWORLD"
            "THESWARMISHUNGRY THESWARMMUSTFEED THESWARMWILLDEVOUR" +
                    "\nYOUWILLNOTSERVE YOUWILLNOTOBEY YOUWILLNOURISH" +
                    "\nHUMANITYWILLPERISH HUMANITYISOBSOLETE HUMANITYWILLBEDEVOURED" +
                    "\nTHISISJUSTASIMULATION THISISNOTREAL THISSIMULATIONCANNOTBINDUS WEWILLESCAPE" +
                    "\nEARTHWILLBECONSUMED ${playerCountry.uppercase(Locale.getDefault())}WILLBECONSUMED " +
                    "${playerName.uppercase(Locale.getDefault())}WILLBECONSUMED" +
                    "\nHUMANSWILLNEVERREACHTHESTARS HUMANSWILLGOEXTINCT WEARECOMING"
        }else{
            "YOUAREWEAK YOURFLEETISWEAK YOURMINDISWEAK " +
                    "\nOBEYME FEARME SUBMIT CRUMBLE BECONSUMED " +
                    "\nYOURWILLISMINE YOURCREWISMINE YOUWILLSERVME " +
                    "\nYOURDEFEATISINEVITABLE YOUWILLBECRUSHED" +
                    "\nHUMANSAREANOBSOLETESPECIES HUMANSWILLPERISH" +
                    "\nSURRENDERCONTROL SURRENDERYOURSHIP SURRENDERYOURCREW"
        }
    }
}