package tecrys.svc

import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.notifications.NeutralWhaleFleetInteraction

class SvcCampaignPlugin: BaseCampaignPlugin() {
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        val fleet = interactionTarget ?: return null
        if(fleet.customData?.containsKey(FleetManager.WHALE_FLEET_IDENTIFICATION_KEY) != true) return null
        return PluginPick(NeutralWhaleFleetInteraction(fleet), CampaignPlugin.PickPriority.MOD_SPECIFIC)
    }

    override fun isTransient(): Boolean = true
    override fun getId(): String = SVC_MOD_ID + "_CampaignPlugin"
}