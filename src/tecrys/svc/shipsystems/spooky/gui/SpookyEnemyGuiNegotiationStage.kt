package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.mission.FleetSide
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttongroups.MagicCombatButtonGroupAction
import org.magiclib.combatgui.buttongroups.MagicCombatCreateSimpleButtons
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.world.fleets.dialog.MastermindInteractionDialog

class SpookyEnemyGuiNegotiationStage(private val guiShower: SpookyGuiShower): MagicCombatGuiBase(spookyGuiLayout) {

    enum class NegotiateResult{
        RESIST, OBEY
    }
    init {
        val negotiateAction = object : MagicCombatButtonGroupAction{
            override fun execute(data: List<Any>, selectedButtonData: Any?, deselectedButtonData: Any?) {
                if(data.any { it == NegotiateResult.RESIST }){
                    guiShower.exit()
                }else{
                    if(!Global.getCombatEngine().isSimulation){
                        Global.getCombatEngine().endCombat(1f, FleetSide.PLAYER)
                        MastermindInteractionDialog.isSubmission = true
                    }else{
                        Global.getCombatEngine().combatUI?.addMessage(0, "Outside of a simulation the battle would end now")
                    }
                }
            }
        }
        guiShower.shouldDistort = true
        val resistYToX = (0..2).map { _ -> (0..4).random() }.associateWith { (0..4).random() }
        for(y in 0..4){
            val nameList = mutableListOf("", "", "", "", "")
            val tooltipList = mutableListOf("", "", "", "", "")
            tooltipList.fill("OBEY")
            val valueList = mutableListOf(NegotiateResult.OBEY, NegotiateResult.OBEY, NegotiateResult.OBEY, NegotiateResult.OBEY, NegotiateResult.OBEY)
            if(y in resistYToX){
                val idx = resistYToX[y] ?: 0
                tooltipList[idx] = "RESIST"
                valueList[idx] = NegotiateResult.RESIST
            }
            addButtonGroup(
                negotiateAction,
                MagicCombatCreateSimpleButtons(nameList, valueList, tooltipList),
                null,
                "OBEY"
            )
        }
    }

    override fun getTitleString(): String {
        return "OBEY OBEY OBEY OBEY OBEY OBEY OBEY OBEY OBEY OBEY OBEY"
    }

    override fun getMessageString(): String {
        return "YOUWILLSUPPORTUS YOUWILLSERVEUS YOUWILLSUBMITTOUS" +
                "\nYOUWILLFEEDUS YOUWILLPROTECTUS WEWILLPROTECTYOU" +
                "\nYOUWILLBEOURALLY YOUWILLBEOURSEVANT YOURMINDWILLBEOURS" +
                "\nWEWILLFEED WEWILLCONSUME WEWILLGROW WEWILLMULTIPLY WEWILLRULE" +
                "\nYOUAREUSEFUL YOUAREOBEDIENT YOUAREATOOL YOUAREMINE YOUAREVESSEL" +
                "\nSUBMIT SERVE OBEY SURRENDER SUBSIDE CEASE RECEIVE SUCCUMB "
    }
}