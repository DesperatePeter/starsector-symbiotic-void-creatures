package tecrys.svc.colonycrisis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import tecrys.svc.SVC_FACTION_ID

class MastermindIntel(private val mastermindFleet: CampaignFleetAPI?): BaseIntelPlugin() {
    override fun getIcon(): String = Global.getSettings().getSpriteName("icons", "svc_mastermind_intel_icon")
    override fun isDone(): Boolean = SymbioticCrisisIntelEvent.isBossObeyed || SymbioticCrisisIntelEvent.isBossDefeated
    override fun hasLargeDescription(): Boolean = false
    override fun hasSmallDescription(): Boolean = true
    override fun getSmallDescriptionTitle(): String = Global.getSettings().getString("svc_colony_crisis_intel", "mastermind_intel_title")
    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info?.addTitle(smallDescriptionTitle)
        info?.addPara(Global.getSettings().getString("svc_colony_crisis_intel", "mastermind_intel_text"), 1f)
    }

    override fun getName(): String = smallDescriptionTitle
    override fun isImportant(): Boolean = true
    override fun canTurnImportantOff(): Boolean = false
    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> = mutableSetOf(SVC_FACTION_ID, "Colony threats")
    override fun shouldRemoveIntel(): Boolean = isDone()
    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? = mastermindFleet
    override fun hasImportantButton(): Boolean = false
}