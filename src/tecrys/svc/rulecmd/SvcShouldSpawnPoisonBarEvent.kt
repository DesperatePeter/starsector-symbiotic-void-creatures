package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.getMarketsCopy
import tecrys.svc.SVC_FLEET_DEFEATED_MEM_KEY
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.getSpecialQuantity

class SvcShouldSpawnPoisonBarEvent : BaseCommandPlugin() {
    companion object{
        var isDone: Boolean by CampaignSettingDelegate("$" + SVC_MOD_ID + "poisonAlreadyDone", false)
        fun getMarket(memoryMap: MutableMap<String, MemoryAPI>?): MarketAPI? {
            val marketId = memoryMap?.get("market")?.get("\$id") ?: return null
            return Global.getSector().allFactions.flatMap { it.getMarketsCopy() }.firstOrNull { it.id == marketId }
        }
    }
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        if ((SymbioticCrisisIntelEvent.isCrisisActive) && ((Global.getSector()?.playerFleet?.cargo?.getSpecialQuantity("svc_poison") ?: 0f) == 0f)) return true

        // if((SymbioticCrisisIntelEvent.get()?.fleetsDefeatedByPlayer ?: 0) <= 0 ) return false
//        if(!Global.getSector().memory.contains(SVC_FLEET_DEFEATED_MEM_KEY)){
//            return false
//        }

        if (isDone) return false
        return getMarket(memoryMap)?.factionId == "luddic_path"
    }
}