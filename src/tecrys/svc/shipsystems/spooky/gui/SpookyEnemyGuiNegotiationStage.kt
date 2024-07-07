package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.mission.FleetSide
import org.magiclib.combatgui.MagicCombatGuiBase
import org.magiclib.combatgui.buttongroups.MagicCombatButtonGroupAction
import org.magiclib.combatgui.buttongroups.MagicCombatCreateSimpleButtons
import org.magiclib.combatgui.buttons.MagicCombatButtonAction
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent

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
                    Global.getCombatEngine().endCombat(1f, FleetSide.PLAYER)
                    Global.getSector().memoryWithoutUpdate[SymbioticCrisisIntelEvent.MEM_KEY_RESOLUTION_BOSS_FIGHT_OBEY] = true
                }
            }
        }
        guiShower.shouldDistort = true
        val resistX = (0..4).random()
        val resistY = (0..4).random()
        for(y in 0..4){
            val nameList = mutableListOf("", "", "", "", "")
            val tooltipList = mutableListOf("", "", "", "", "")
            tooltipList.fill("OBEY")
            val valueList = mutableListOf(NegotiateResult.OBEY, NegotiateResult.OBEY, NegotiateResult.OBEY, NegotiateResult.OBEY, NegotiateResult.OBEY)
            if(y == resistY){
                tooltipList[resistX] = "RESIST"
                valueList[resistX] = NegotiateResult.RESIST
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