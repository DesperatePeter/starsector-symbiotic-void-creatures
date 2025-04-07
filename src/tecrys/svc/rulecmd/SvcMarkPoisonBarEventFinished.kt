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

class SvcMarkPoisonBarEventFinished : BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        SvcShouldSpawnPoisonBarEvent.isDone = true
        return true
    }
}