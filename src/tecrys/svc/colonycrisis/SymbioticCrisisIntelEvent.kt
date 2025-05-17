package tecrys.svc.colonycrisis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.Script
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import org.magiclib.kotlin.addGlowyParticle
import tecrys.svc.MMM_FACTION_ID
import tecrys.svc.SVC_COLONY_CRISIS_INTEL_TEXT_KEY
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.listeners.CrisisFleetListener
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.world.fleets.*
import tecrys.svc.world.fleets.dialog.MastermindInteractionDialog
import java.awt.Color
import java.lang.ref.WeakReference

class SymbioticCrisisIntelEvent(private val market: MarketAPI) : BaseEventIntel() {

    companion object{
        const val MAX_NUM_FLEETS = 30
        const val FLEETS_DEFEATED_UNTIL_CLUE = 2
        const val FLEETS_DEFEATED_UNTIL_SECOND_CLUE = 4
        const val MIN_SPAWN_DISTANCE_FROM_PLAYER_FLEET = 1000f
        const val PROGRESS_BLOWBACK_PER_FLEET = 10
        const val FLEET_POWER_MODIFIER = 0.7f // Spawning lots of full power fleets is a bit overwhelming
        const val MEM_KEY = "\$SVC_COLONY_CRISIS_INTEL_EVENT_KEY"
        const val MEM_KEY_RESOLUTION_BOSS_FIGHT_WIN = "\$SVC_COLONY_CRISIS_RESOLVED_BOSS_FIGHT_WIN"
        const val MEM_KEY_DISABLE_TELEPATHY = "\$SVC_MASTERMIND_NO_TELEPATHY"
        const val MARKET_CONDITION = "svc_voidling_infestation"
        const val INFESTATION_HULLMOD = "svc_infestation_hm"
        var isBossDefeated by CampaignSettingDelegate(MEM_KEY_RESOLUTION_BOSS_FIGHT_WIN, false)
        val isBossObeyed get() = MastermindInteractionDialog.isSubmission
        val isWhaleSacrifice: Boolean get() = poisonLureLocation != null
        private var poisonLureLocation: SectorEntityToken? by CampaignSettingDelegate("\$svcLureConstructed", null)
        val isCrisisActive get() = (get() != null) && !SymbioticCrisisCause.isCrisisResolved()
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
        fun applyOrRemoveVoidlingInfestationToShipsInSystem(system: LocationAPI?){
            system?.fleets?.flatMap { it.fleetData.membersListCopy }?.forEach { member ->
                if(!SymbioticCrisisCause.isCrisisResolved()) {
                    member.variant.addMod(INFESTATION_HULLMOD)
                }else{
                    member.variant.removeMod(INFESTATION_HULLMOD)
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
    var fleetsDefeatedByPlayer = 0
        private set
    private val currentNumberOfFleets get() = crisisFleets.size
    private val defeatedFleetIds = mutableSetOf<Long>()
    private val timer = IntervalUtil(20f, 40f)
    private val crisisFleets = mutableMapOf<Long, WeakReference<CampaignFleetAPI>>()

    fun reportFleetDefeated(defeatedByPlayer: Boolean, id: Long){
        if(id in defeatedFleetIds) return
        defeatedFleetIds.add(id)
        if(defeatedByPlayer) {
            fleetsDefeatedByPlayer++
            HostileActivityEventIntel.get()?.addFactor(object : BaseOneTimeFactor(-PROGRESS_BLOWBACK_PER_FLEET) {
                override fun getDesc(intel: BaseEventIntel?): String {
                    return "Voidling fleets defeated"
                }

                override fun getMainRowTooltip(intel: BaseEventIntel?): TooltipMakerAPI.TooltipCreator {
                    return object : BaseFactorTooltip() {
                        override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                            tooltip?.addPara("If you are able to destroy enough fleets, you might be able to dissuade the void creatures.", 0f)
                        }
                    }
                }
            })
        }
        crisisFleets.remove(id)
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
        if(isBossDefeated || isBossObeyed || isWhaleSacrifice){
            SymbioticCrisisCause.resolveCrisis()
            return
        }
        timer.advance(amount)
        if(timer.intervalElapsed() ) {
            if(currentNumberOfFleets < MAX_NUM_FLEETS) spawnFleetIfNecessary()
            applyOrRemoveMarketConditions()
            applyOrRemoveVoidlingInfestationToShipsInSystem(market.containingLocation)
        }
        setProgress(fleetsDefeatedByPlayer)
        if(progress >= FLEETS_DEFEATED_UNTIL_SECOND_CLUE){
            if(!Global.getSector().memoryWithoutUpdate.contains(MASTERMIND_FLEET_MEMKEY)){
                val mastermindFleet = FleetManager().spawnMastermindFleet()
                Global.getSector().intelManager.addIntel(MastermindIntel(mastermindFleet))
                Global.getSector().memoryWithoutUpdate[FleetManager.MASTERMIND_FLEET_MEM_KEY] = mastermindFleet
            }
        }
    }

    fun solveViaPoison(location: SectorEntityToken){
        poisonLureLocation = location
        val maxDistInLy = 10f
        val affectedFleets = crisisFleets.values.mapNotNull { it.get() } +
                FleetSpawner.getFactionFleets(SVC_FACTION_ID).filter {
                    (it.locationInHyperspace - market.locationInHyperspace).length() < maxDistInLy
                }
        affectedFleets.forEach { fleet ->
            fleet.clearAssignments()
            fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, location, 999f) {
                fleet.containingLocation.addGlowyParticle(
                    fleet.location, fleet.velocity,
                    2f * fleet.radius, 1f, 5f, Color.GREEN
                )
            }
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

        FleetManager().spawnSvcFleet(
            location, true,
            FleetSpawnParameterCalculator(svcSettings).withModifiedPower(FLEET_POWER_MODIFIER)
        )?.let { fleet ->
            val id = random.nextLong()
            fleet.addEventListener(CrisisFleetListener(id)) // will call reportFleetDefeated to modify number of fleet values
            // emulate two sub-factions fighting against each other
            if(Math.random() > 0.5f) { fleet.setFaction(MMM_FACTION_ID)
                Misc.makeHostileToFaction(fleet, SVC_FACTION_ID, 999999f)
                fleet.memoryWithoutUpdate[com.fs.starfarer.api.impl.campaign.ids.MemFlags.MEMORY_KEY_NO_REP_IMPACT] = true
//                fleet.memoryWithoutUpdate[com.fs.starfarer.api.impl.campaign.ids.MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
            }
            crisisFleets[id] = WeakReference(fleet)
            return true
        }
        return false
    }

    override fun reportRemovedIntel() {
        Global.getSector().memoryWithoutUpdate.unset(MEM_KEY)
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Major Event", "Colony threats")
    }
}