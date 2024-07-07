package tecrys.svc.econ

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin

class VoidlingInfestation: BaseMarketConditionPlugin() {
    companion object{
        const val ACCESSIBILITY_PENALTY = 0.25f
        const val INCOME_PENALTY = 0.1f
        const val ADDITIONAL_HAZARD = 0.25f
    }

    override fun apply(id: String?) {
        market.accessibilityMod.modifyFlat(id, -ACCESSIBILITY_PENALTY, "Voidlings are disrupting trade")
        market.incomeMult.modifyFlat(id, -INCOME_PENALTY, "Voidlings are disrupting operations")
        market.hazard.modifyFlat(id, ADDITIONAL_HAZARD, "Swarms of voidlings make life more dangerous")
    }

    override fun unapply(id: String?) {
        market.accessibilityMod.unmodify(id)
        market.incomeMult.unmodify(id)
        market.hazard.unmodify(id)
    }
}