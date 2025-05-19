package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SvcMarkHuntersDefeatedBarEventFinished: BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        SvcShouldSpawnHuntersDefeatedBarEvent.hasAlreadyTriggered = true
        val item = SpecialItemData("svc_attractor", null)
        val fakeCargo = Global.getFactory().createCargo(true)
        fakeCargo.addSpecial(item, 1f)
        val itemName = fakeCargo.stacksCopy[0]?.displayName ?: "Voidling Hatchery Blueprint"
        dialog?.textPanel?.run {
            setFontSmallInsignia()
            addParagraph("Gained $itemName", Misc.getPositiveHighlightColor())
            highlightInLastPara(Misc.getHighlightColor(), itemName)
            setFontInsignia()
        }
        Global.getSector()?.playerFleet?.cargo?.addSpecial(item, 1f)
        return true
    }
}