package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.utils.giveSpecialItemToPlayer
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
            stableLocation.containingLocation.addCustomEntity("svc_meat", "Void Creature Lure",
                "svc_meat", Factions.INDEPENDENT, stableLocation)

            val system = Global.getSector().playerFleet.containingLocation
            val notificationText = "Your administrator reports that the void creatures that were harassing your colony" +
                    " appear to be leaving the system, heading towards the $system"
            val notificationSprite = Global.getSettings().getSpriteName("icons", "svc_meat_icon")
            showNotificationOnCampaignUi(notificationText, notificationSprite)
            giveSpecialItemToPlayer("svc_enriched_fungus", null, dialog.textPanel)
            return true
        }
        return false
    }
}