package tecrys.svc.rulecmd

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.defeatedHunterFleets
import tecrys.svc.utils.CampaignSettingDelegate

class SvcShouldSpawnHuntersDefeatedBarEvent: BaseCommandPlugin() {
    companion object{
        var hasAlreadyTriggered: Boolean by CampaignSettingDelegate("$" + SVC_MOD_ID + "hasSvcSpawnHuntersDefeatedBarEventTriggered", false)
    }
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        if(hasAlreadyTriggered) return false
        return defeatedHunterFleets >= 3
    }
}