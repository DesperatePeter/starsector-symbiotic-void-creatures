package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.mission.FleetSide
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.shipsystems.spooky.SpookyMindControl
import java.util.Locale

class SpookyEnemyGuiSabotageStage(guiShower: SpookyGuiShower, private val useAltText: Boolean): MagicCombatGuiBase(spookyGuiLayout) {

    enum class Sabotage{
        WEAPONS, DRIVE, CREW
    }

    companion object{
        fun randomPlayerShip(): ShipAPI = Global.getCombatEngine().ships.filter { it.owner == 0 && it != Global.getCombatEngine().playerShip }.random()
    }

    class SabotageAction(private val guiShower: SpookyGuiShower, private val sabotages: List<Sabotage>) : MagicCombatButtonAction{
        private val pf: ShipAPI = kotlin.run {
            val toReturn = Global.getCombatEngine().playerShip
            if(toReturn.fleetMember == null) randomPlayerShip() else toReturn
        }
        override fun execute() {
            sabotages.forEach { s ->
                when(s){
                    Sabotage.WEAPONS -> {
                        pf.allWeapons.filter { Math.random() > 0.5f }.forEach { w -> pf.applyCriticalMalfunction(w) }
                        Global.getCombatEngine().combatUI?.addMessage(0, "Your weapons have suffered damage")
                    }
                    Sabotage.DRIVE -> {
                        pf.engineController.shipEngines.forEach { e -> pf.applyCriticalMalfunction(e) }
                        Global.getCombatEngine().combatUI?.addMessage(0, "Your engines have suffered damage")
                    }
                    Sabotage.CREW -> {
                        pf.currentCR -= 0.25f
                        Global.getCombatEngine().combatUI?.addMessage(0, "You have lost combat readiness")
                    }
                }
            }
            val ship = randomPlayerShip()
            Global.getCombatEngine()?.addPlugin(SpookyMindControl(ship))
            Global.getCombatEngine().combatUI?.addMessage(0, "${ship.name}: What is going on?? SHOOTTHETRAITORS!!")
            guiShower.exit()
        }
    }

    init {
        guiShower.shouldDistort = true
        guiShower.shouldPreventPause = true
        addButton(SabotageAction(guiShower, listOf(Sabotage.DRIVE, Sabotage.CREW)), "Protect bridge", "IAMINVINCIBLE")
        addButton(SabotageAction(guiShower, listOf(Sabotage.WEAPONS, Sabotage.CREW)), "Protect engineering", "YOUCANNOTRUN")
        addButton(SabotageAction(guiShower, listOf(Sabotage.WEAPONS, Sabotage.DRIVE)), "Protect crew", "YOUWILLBECONSUMED")
    }

    override fun getTitleString(): String {
        return "What futile resistance do you plan?"
    }

    override fun getMessageString(): String {
        return if(useAltText){
            val playerName = System.getProperty("user.name") ?: "YOU"
            val charName = Global.getSector().playerPerson.nameString
            var playerCountry = Locale.getDefault().country ?: "YOURWORLD"
            if(playerCountry == "US") playerCountry = "THEUSA"
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