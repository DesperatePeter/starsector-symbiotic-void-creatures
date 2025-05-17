package tecrys.svc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import tecrys.svc.industries.VoidlingHatchery
import tecrys.svc.listeners.MastermindFleetListener
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.MASTERMIND_FLEET_MEMKEY
import tecrys.svc.world.fleets.dialog.MastermindInteractionDialog
import tecrys.svc.world.notifications.HatchlingFleetInteraction
import tecrys.svc.world.notifications.NeutralWhaleFleetInteraction
import tecrys.svc.world.notifications.VoidlingFleetInteraction

class SvcCampaignPlugin: BaseCampaignPlugin() {
    override fun pickInteractionDialogPlugin(interactionTarget: SectorEntityToken?): PluginPick<InteractionDialogPlugin>? {
        val fleet = interactionTarget as? CampaignFleetAPI ?: return null
        if(fleet.battle != null) return null
        if(fleet.memoryWithoutUpdate.contains(MASTERMIND_FLEET_MEMKEY)){
            return PluginPick(MastermindInteractionDialog(fleet), CampaignPlugin.PickPriority.HIGHEST)
        }
        if(fleet.customData?.containsKey(FleetManager.WHALE_FLEET_IDENTIFICATION_KEY) == true){
            return PluginPick(NeutralWhaleFleetInteraction(fleet), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        }
        if(fleet.customData?.containsKey(VoidlingHatchery.HATCHLING_FLEET_KEY) == true){
            return PluginPick(HatchlingFleetInteraction(fleet), CampaignPlugin.PickPriority.MOD_SPECIFIC)
        }
        if(fleet.customData?.containsKey(FleetManager.SVC_FLEET_IDENTIFICATION_KEY) == true &&
//            fleet.faction.relToPlayer.equals(RepLevel.COOPERATIVE)
            Global.getSector().getFaction(SVC_FACTION_ID).relToPlayer.isAtWorst(RepLevel.FRIENDLY)
            )


            return PluginPick(VoidlingFleetInteraction(fleet), CampaignPlugin.PickPriority.MOD_SPECIFIC)

        return null
    }


    override fun isTransient(): Boolean = true
    override fun getId(): String = SVC_MOD_ID + "_CampaignPlugin"
}