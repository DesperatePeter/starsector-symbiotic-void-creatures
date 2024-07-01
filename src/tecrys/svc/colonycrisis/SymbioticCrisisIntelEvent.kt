package tecrys.svc.colonycrisis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import tecrys.svc.SVC_COLONY_CRISIS_INTEL_TEXT_KEY
import tecrys.svc.listeners.CrisisFleetListener
import tecrys.svc.world.fleets.FleetManager

class SymbioticCrisisIntelEvent(private val market: MarketAPI) : BaseEventIntel() {

    companion object{
        const val MAX_NUM_FLEETS = 18
        const val MEM_KEY = "\$SVC_COLONY_CRISIS_INTEL_EVENT_KEY"
        const val MEM_KEY_RESOLUTION_GENOCIDE = "\$SVC_COLONY_CRISIS_RESOLVED_GENOCIDE"
        const val MEM_KEY_RESOLUTION_BOSS_FIGHT = "\$SVC_COLONY_CRISIS_RESOLVED_BOSS_FIGHT"
        const val MEM_KEY_RESOLUTION_WHALE_SACRIFICE = "\$SVC_COLONY_CRISIS_RESOLVED_WHALE_SACRIFICE"
        fun get() : SymbioticCrisisIntelEvent? = Global.getSector().memoryWithoutUpdate[MEM_KEY] as? SymbioticCrisisIntelEvent
        fun reportFleetDefeated(defeatedByPlayer: Boolean){
            get()?.reportFleetDefeated(defeatedByPlayer)
        }
    }

    init {
        setMaxProgress(MAX_NUM_FLEETS)
        setProgress(0)
        Global.getSector().memoryWithoutUpdate[MEM_KEY] = this
    }

    // to prevent the event from ending itself by e.g. patrol fleets killing voidlings, only count ones defeated
    // by the player
    private var fleetsDefeatedByPlayer = 0
    private var currentNumberOfFleets = 0

    fun reportFleetDefeated(defeatedByPlayer: Boolean){
        if(defeatedByPlayer) fleetsDefeatedByPlayer++
        currentNumberOfFleets--
    }

    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("icons", "svc_colony_crisis_icon")
    }

    override fun getName(): String {
        return Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_title_long")
    }

    override fun getSmallDescriptionTitle(): String {
        return Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_title_short")
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info?.addTitle(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_title_short"))
        info?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_short"), 1f)
    }

    override fun createLargeDescription(panel: CustomPanelAPI?, width: Float, height: Float) {
        val pad = 10.0f
        this.uiWidth = width
        val main = panel?.createUIElement(width, height, true) ?: return
        main.setTitleOrbitronVeryLarge()
        main.addTitle(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_title_long"))
        main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_long"), pad)
        when{
            progress < 2 -> main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_few_kills"), pad)
            progress < 10 -> main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_some_kills"), pad)
            progress > 10 -> main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_many_kills"), pad)
        }
        panel.addUIElement(main).inTL(0f, 0f)
    }

    override fun hasSmallDescription(): Boolean = true

    override fun hasLargeDescription(): Boolean = true

    override fun shouldRemoveIntel(): Boolean = SymbioticCrisisCause.isCrisisResolved()

    override fun advanceImpl(amount: Float) {
        while(currentNumberOfFleets < MAX_NUM_FLEETS){
            val fleet = FleetManager().spawnSvcFleet(market.primaryEntity, true)
            fleet?.addEventListener(CrisisFleetListener()) // will call reportFleetDefeated to modify number of fleet values
            currentNumberOfFleets++
        }
        setProgress(fleetsDefeatedByPlayer)
        if(progress >= MAX_NUM_FLEETS){
            Global.getSector().memoryWithoutUpdate[MEM_KEY_RESOLUTION_GENOCIDE] = true
            SymbioticCrisisCause.resolveCrisis()
        }
        if(Global.getSector().memoryWithoutUpdate.getBoolean(MEM_KEY_RESOLUTION_BOSS_FIGHT) ||
            Global.getSector().memoryWithoutUpdate.getBoolean(MEM_KEY_RESOLUTION_WHALE_SACRIFICE)){
            SymbioticCrisisCause.resolveCrisis()
        }
    }

    override fun reportRemovedIntel() {
        Global.getSector().memoryWithoutUpdate.unset(MEM_KEY)
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Major Event", "Colony threats")
    }
}