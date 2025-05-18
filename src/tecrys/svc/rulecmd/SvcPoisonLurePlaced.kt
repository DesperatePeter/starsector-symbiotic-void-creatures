package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.utils.showNotificationOnCampaignUi

class SvcPoisonLurePlaced: BaseCommandPlugin() {
    override fun execute(
        p0: String?,
        dialog: InteractionDialogAPI?,
        p2: List<Misc.Token?>?,
        memoryMap: Map<String?, MemoryAPI?>?
    ): Boolean {
        dialog?.interactionTarget?.let { stableLocation ->
            SymbioticCrisisIntelEvent.get()?.solveViaPoison(stableLocation)
            val system = Global.getSector().playerFleet.containingLocation
            val notificationText = "Your administrator reports that the void creatures that were harassing your colony" +
                    " appear to be leaving the system, heading towards the $system"
            // FIXME @ Tecrys (replace with Global.getSettings().getSprite call for actual sprite
            val notificationSprite = Global.getSettings().getSpriteName("illustrations", "svc_food")
            showNotificationOnCampaignUi(notificationText, notificationSprite)
            return true
        }
        return false
    }
}