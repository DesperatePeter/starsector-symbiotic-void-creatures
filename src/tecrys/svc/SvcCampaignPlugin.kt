package tecrys.svc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.BaseCampaignPlugin
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CampaignPlugin
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import tecrys.svc.industries.VoidlingHatchery
import tecrys.svc.listeners.MastermindFleetListener
import tecrys.svc.listeners.MastermindInteractionDialog
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.MASTERMIND_FLEET_MEMKEY
import tecrys.svc.world.notifications.HatchlingFleetInteraction
import tecrys.svc.world.notifications.NeutralWhaleFleetInteraction

class SvcCampaignPlugin: BaseCampaignPlugin() {
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        val fleet = interactionTarget as? CampaignFleetAPI ?: return null
        if(fleet.battle != null) return null
        if(fleet.memoryWithoutUpdate.contains(MASTERMIND_FLEET_MEMKEY)){
            return PluginPick(MastermindInteractionDialog(), CampaignPlugin.PickPriority.HIGHEST)
        }
        if(fleet.customData?.containsKey(FleetManager.WHALE_FLEET_IDENTIFICATION_KEY) == true){
            return PluginPick(NeutralWhaleFleetInteraction(fleet), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        }
        if(fleet.customData?.containsKey(VoidlingHatchery.HATCHLING_FLEET_KEY) == true){
            return PluginPick(HatchlingFleetInteraction(fleet), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        }
        return null
    }


    override fun isTransient(): Boolean = true
    override fun getId(): String = SVC_MOD_ID + "_CampaignPlugin"
}