package tecrys.svc.colonycrisis

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.events.BaseFactorTooltip
import com.fs.starfarer.api.impl.campaign.intel.events.BaseHostileActivityCause2
import com.fs.starfarer.api.impl.campaign.intel.events.HostileActivityEventIntel
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import tecrys.svc.SVC_COLONY_CRISIS_TEXT_KEY


class SymbioticCrisisCause(intel: HostileActivityEventIntel?) : BaseHostileActivityCause2(intel) {
    companion object {
        private const val MEM_KEY_CRISIS_RESOLVED = "\$SVC_COLONY_CRISIS_RESOLVED"
        private val MIN_DIST_FROM_CENTER_TO_CONTRIBUTE = Global.getSettings().getInt("sectorWidth").toFloat() * 0.1f
        private val DIST_FROM_CENTER_SCALING = Global.getSettings().getInt("sectorWidth").toFloat() * 0.05f
        private const val CONTRIBUTION_MULTIPLIER = 20f // FIXME: High value just for testing
        private const val MIN_CONTRIBUTION_PER_MARKET = 1f
        private const val MAX_CONTRIBUTION_PER_MARKET = 20f
        private const val MAX_TOTAL_PROGRESS = 200f // FIXME: limit?

        fun isCrisisResolved(): Boolean = Global.getSector().playerMemoryWithoutUpdate.getBoolean(
            MEM_KEY_CRISIS_RESOLVED
        )

        fun resolveCrisis() {
            Global.getSector().playerMemoryWithoutUpdate[MEM_KEY_CRISIS_RESOLVED] = true
        }

        fun initializeEvent(){
            HostileActivityEventIntel.get()?.run {
                if(factors.any { SymbioticCrisisFactor::class.java.isInstance(it) }) return
                addActivity(SymbioticCrisisFactor(this), SymbioticCrisisCause(this))
            }
        }

        fun getMarketContribution(market: MarketAPI): Float {
            return ( CONTRIBUTION_MULTIPLIER *
                    (market.locationInHyperspace.length() - MIN_DIST_FROM_CENTER_TO_CONTRIBUTE) /
                    DIST_FROM_CENTER_SCALING * (market.size - 2).toFloat()
                    ).coerceIn(MIN_CONTRIBUTION_PER_MARKET, MAX_CONTRIBUTION_PER_MARKET)
        }
    }

    override fun getDesc(): String {
        return Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "description")
    }

    override fun getTooltip(): TooltipMakerAPI.TooltipCreator {
        return object : BaseFactorTooltip(){
            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                tooltip?.addPara(Global.getSettings().getString(SVC_COLONY_CRISIS_TEXT_KEY, "tooltip"), 0f)
            }
        }
    }

    override fun getProgress(): Int {
        if (isCrisisResolved()) return 0
        val toReturn =
         Misc.getPlayerMarkets(false).mapNotNull { market ->
            getMarketContribution(market)
        }.sum().coerceIn(0f, MAX_TOTAL_PROGRESS).toInt()
        return toReturn
    }

    override fun getMagnitudeContribution(system: StarSystemAPI?): Float {
        val toReturn = Misc.getMarketsInLocation(system, Factions.PLAYER).mapNotNull { market ->
            getMarketContribution(market)
        }.sum().coerceIn(0f, MAX_TOTAL_PROGRESS) // / getProgress().toFloat()
        return toReturn
    }

}