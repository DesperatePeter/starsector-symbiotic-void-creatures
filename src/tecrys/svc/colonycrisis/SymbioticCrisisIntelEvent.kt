package tecrys.svc.colonycrisis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import tecrys.svc.SVC_COLONY_CRISIS_INTEL_TEXT_KEY
import tecrys.svc.listeners.CrisisFleetListener
import tecrys.svc.world.fleets.*

class SymbioticCrisisIntelEvent(private val market: MarketAPI) : BaseEventIntel() {

    companion object{
        const val MAX_NUM_FLEETS = 10
        const val FLEETS_DEFEATED_UNTIL_CLUE = 2
        const val FLEETS_DEFEATED_UNTIL_SECOND_CLUE = 4
        const val MIN_SPAWN_DISTANCE_FROM_PLAYER_FLEET = 2000f
        const val FLEET_POWER_MODIFIER = 0.8f // Spawning lots of full power fleets is a bit overwhelming
        const val MEM_KEY = "\$SVC_COLONY_CRISIS_INTEL_EVENT_KEY"
        const val MEM_KEY_RESOLUTION_GENOCIDE = "\$SVC_COLONY_CRISIS_RESOLVED_GENOCIDE"
        const val MEM_KEY_RESOLUTION_BOSS_FIGHT_WIN = "\$SVC_COLONY_CRISIS_RESOLVED_BOSS_FIGHT_WIN"
        const val MEM_KEY_DISABLE_TELEPATHY = "\$SVC_MASTERMIND_NO_TELEPATHY"
        const val MEM_KEY_RESOLUTION_BOSS_FIGHT_OBEY = "\$SVC_COLONY_CRISIS_RESOLVED_BOSS_FIGHT_OBEY"
        const val MEM_KEY_RESOLUTION_WHALE_SACRIFICE = "\$SVC_COLONY_CRISIS_RESOLVED_WHALE_SACRIFICE"
        const val MARKET_CONDITION = "svc_voidling_infestation"
        fun isBossDefeated(): Boolean = Global.getSector().memoryWithoutUpdate.getBoolean(MEM_KEY_RESOLUTION_BOSS_FIGHT_WIN)
        fun isBossObeyed(): Boolean = Global.getSector().memoryWithoutUpdate.getBoolean(MEM_KEY_RESOLUTION_BOSS_FIGHT_OBEY)
        fun isVoidlingGenocide(): Boolean = Global.getSector().memoryWithoutUpdate.getBoolean(MEM_KEY_RESOLUTION_GENOCIDE)
        fun get() : SymbioticCrisisIntelEvent? = Global.getSector().memoryWithoutUpdate[MEM_KEY] as? SymbioticCrisisIntelEvent
        fun reportFleetDefeated(defeatedByPlayer: Boolean, id: Long){
            get()?.reportFleetDefeated(defeatedByPlayer, id)
        }
        fun applyOrRemoveMarketConditions(){
            if(!SymbioticCrisisCause.isCrisisResolved()){
                Misc.getPlayerMarkets(false).filterNotNull().forEach { market ->
                    if(!market.hasCondition(MARKET_CONDITION)) market.addCondition(MARKET_CONDITION)
                }
            }else{
                Misc.getPlayerMarkets(false).filterNotNull().forEach { market ->
                    if(market.hasCondition(MARKET_CONDITION)) market.removeCondition(MARKET_CONDITION)
                }
            }
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
    private val defeatedFleetIds = mutableSetOf<Long>()
    private val timer = IntervalUtil(20f, 40f)

    fun reportFleetDefeated(defeatedByPlayer: Boolean, id: Long){
        if(id in defeatedFleetIds) return
        defeatedFleetIds.add(id)
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
            progress < FLEETS_DEFEATED_UNTIL_CLUE -> main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_few_kills"), pad)
            progress < FLEETS_DEFEATED_UNTIL_SECOND_CLUE -> {
                main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_some_kills"), pad)
            }
            progress > FLEETS_DEFEATED_UNTIL_SECOND_CLUE -> main.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_INTEL_TEXT_KEY, "event_text_many_kills"), pad)
        }
        panel.addUIElement(main).inTL(0f, 0f)
    }

    override fun hasSmallDescription(): Boolean = true

    override fun hasLargeDescription(): Boolean = true

    override fun shouldRemoveIntel(): Boolean = SymbioticCrisisCause.isCrisisResolved()



    override fun advanceImpl(amount: Float) {
        timer.advance(amount)
        if(timer.intervalElapsed() ) {
            if(currentNumberOfFleets < MAX_NUM_FLEETS) spawnFleetIfNecessary()
            applyOrRemoveMarketConditions()
        }
        setProgress(fleetsDefeatedByPlayer)
        if(progress >= MAX_NUM_FLEETS){
            Global.getSector().memoryWithoutUpdate[MEM_KEY_RESOLUTION_GENOCIDE] = true
            SymbioticCrisisCause.resolveCrisis()
        }
        if(progress >= FLEETS_DEFEATED_UNTIL_SECOND_CLUE){
            if(!Global.getSector().memoryWithoutUpdate.contains(MASTERMIND_FLEET_MEMKEY)){
                val mastermindFleet = FleetManager().spawnMastermindFleet()
                Global.getSector().intelManager.addIntel(MastermindIntel(mastermindFleet))
                Global.getSector().memoryWithoutUpdate[FleetManager.MASTERMIND_FLEET_MEM_KEY] = mastermindFleet
            }
        }
        if(isBossDefeated() || isBossObeyed()){
            SymbioticCrisisCause.resolveCrisis()
        }
    }



    private fun spawnFleetIfNecessary(): Boolean {
        val pf = Global.getSector().playerFleet ?: return false
        val location = market.containingLocation.allEntities.filterNotNull().filter {
            FleetSpawner.isValidSpawnableEntity(it) &&
                    if (pf.containingLocation == market.containingLocation)
                        (it.location - pf.location).length() > MIN_SPAWN_DISTANCE_FROM_PLAYER_FLEET
                    else true
        }.randomOrNull() ?: return false
        val fleet = FleetManager().spawnSvcFleet(
            location, true,
            FleetSpawnParameterCalculator(svcSettings).withModifiedPower(FLEET_POWER_MODIFIER)
        )
        fleet?.addEventListener(CrisisFleetListener(random.nextLong())) // will call reportFleetDefeated to modify number of fleet values
        currentNumberOfFleets++
        return true
    }

    override fun reportRemovedIntel() {
        Global.getSector().memoryWithoutUpdate.unset(MEM_KEY)
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Major Event", "Colony threats")
    }
}