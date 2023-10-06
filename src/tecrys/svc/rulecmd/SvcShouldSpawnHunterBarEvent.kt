package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.SVC_FLEET_DEFEATED_MEM_KEY
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate

class SvcShouldSpawnHunterBarEvent: BaseCommandPlugin() {
    companion object{
        var hasAlreadyTriggered: Boolean by CampaignSettingDelegate("$" + SVC_MOD_ID + "hasSvcSpawnHunterBarEventTriggered", false)
    }
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        if(!Global.getSector().memory.contains(SVC_FLEET_DEFEATED_MEM_KEY)){
            return false
        }
        if(hasAlreadyTriggered) return false
        return true
    }
}