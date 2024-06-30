package tecrys.svc.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel
import com.fs.starfarer.api.ui.TooltipMakerAPI
import tecrys.svc.defeatedHunterFleets

class HunterIntelEntry : BaseIntelPlugin() {
    override fun getIcon(): String {
        return Global.getSettings().getSpriteName("icons", "svc_hunter_intel_entry")
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info?.run {
            addTitle("Symbiotic Void Hunters")
            addPara(Global.getSettings().getString("svc_intel", "hunter_intel_entry"), 1f)
        }
    }

    override fun getName(): String {
        return "Symbiotic Void Hunters"
    }

    override fun advanceImpl(amount: Float) {
        super.advanceImpl(amount)
        if(defeatedHunterFleets >= 3){
            Global.getSector().intelManager.removeIntel(this)
        }
    }
}