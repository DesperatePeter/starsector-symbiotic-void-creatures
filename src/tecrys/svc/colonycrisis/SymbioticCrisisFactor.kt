package tecrys.svc.colonycrisis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel.EventStageData
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityFactor
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityCause2
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel.HAERandomEventData
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import tecrys.svc.SVC_COLONY_CRISIS_TEXT_KEY
import tecrys.svc.SVC_FACTION_ID
import java.awt.Color
import kotlin.math.pow


class SymbioticCrisisFactor(intel: HostileActivityEventIntel?) : BaseHostileActivityFactor(intel) {
    override fun getDesc(intel: BaseEventIntel?): String {
        return Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "description2")
    }

    override fun getNameForThreatList(first: Boolean): String {
        return Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "threatFactor")
    }

    override fun getEventStageIcon(intel: HostileActivityEventIntel?, stage: BaseEventIntel.EventStageData?): String {
        return Global.getSettings().getSpriteName("icons", "svc_colony_crisis_icon")
    }

    override fun getEventFrequency(intel: HostileActivityEventIntel?, stage: BaseEventIntel.EventStageData?): Float {
        if (SymbioticCrisisCause.isCrisisResolved()) return 0f
        return 10000f // FIXME: This insanely high value is just for testing
    }

    override fun getNameColorForThreatList(): Color {
        return Global.getSector().getFaction(SVC_FACTION_ID).color
    }

    override fun getProgressStr(intel: BaseEventIntel?): String {
        return ""
    }

    override fun getDescColor(intel: BaseEventIntel?): Color {
        return Global.getSector().getFaction(SVC_FACTION_ID).color
    }

    override fun rollEvent(intel: HostileActivityEventIntel?, stage: BaseEventIntel.EventStageData?) {
        val data = HAERandomEventData(this, stage)
        stage?.rollData = HAERandomEventData(this, stage)
        intel?.sendUpdateIfPlayerHasIntel(data, false)
    }

    override fun getStageTooltipImpl(intel: HostileActivityEventIntel?, stage: BaseEventIntel.EventStageData): TooltipMakerAPI.TooltipCreator? {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                tooltip?.addTitle(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "event_description_title"))
                tooltip?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "event_description_text"), 1f)
            }
        }
    }

    override fun fireEvent(intel: HostileActivityEventIntel?, stage: BaseEventIntel.EventStageData?): Boolean {
        Global.getLogger(this.javaClass).info(">>>>>>>SVC COLONY CRISIS EVENT HAS FIRED<<<<<<<")
        val market = pickTargetMarket() ?: return false
        Global.getSector().intelManager.addIntel(SymbioticCrisisIntelEvent(market))
        return true
    }

    private fun pickTargetMarket(): MarketAPI? {
        val picker = WeightedRandomPicker<MarketAPI>(randomizedStageRandom)
        Misc.getPlayerMarkets(false).filterNotNull().asSequence().filter { m ->
            m.starSystem != null
        }.associateWith { m ->
            SymbioticCrisisCause.getMarketContribution(m).pow(2)
        }.forEach {
            picker.add(it.key, it.value)
        }
        return picker.pick()
    }

    @Deprecated("Deprecated in Java")
    override fun getMainRowTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip() {
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                tooltip?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "tooltip2"), 1f)
            }
        }
    }

    override fun shouldShow(intel: BaseEventIntel?): Boolean {
        return getProgress(intel) > 0
    }

    override fun addBulletPointForEvent(
        intel: HostileActivityEventIntel?,
        stage: EventStageData?,
        info: TooltipMakerAPI?,
        mode: IntelInfoPlugin.ListInfoMode?,
        isUpdate: Boolean,
        tc: Color?,
        initPad: Float
    ) {
        info?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "event_bullet_point"), 1f)
    }

    override fun addBulletPointForEventReset(
        intel: HostileActivityEventIntel?,
        stage: EventStageData?,
        info: TooltipMakerAPI?,
        mode: IntelInfoPlugin.ListInfoMode?,
        isUpdate: Boolean,
        tc: Color?,
        initPad: Float
    ) {
        info?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "event_averted_bullet_point"), 1f)
    }

    override fun addStageDescriptionForEvent(
        intel: HostileActivityEventIntel?,
        stage: EventStageData?,
        info: TooltipMakerAPI?
    ) {
        info?.addTitle(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "main_event_title"))
        info?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "main_event_text"), 1f)
        addBorder(info, Global.getSettings().basePlayerColor)
    }
}