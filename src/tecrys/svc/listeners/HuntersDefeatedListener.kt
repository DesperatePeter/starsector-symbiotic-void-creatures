package tecrys.svc.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import tecrys.svc.canRecoverAlphas
import tecrys.svc.canRecoverVoidlings
import tecrys.svc.defeatedHunterFleets
import tecrys.svc.rulecmd.SvcShouldSpawnHunterBarEvent
import tecrys.svc.utils.unlockVoidlingRecovery
import tecrys.svc.world.fleets.HunterFleetConfig
import tecrys.svc.world.fleets.hunterFleetsById
import tecrys.svc.world.fleets.hunterFleetsToSpawn
import tecrys.svc.world.notifications.NotificationShower

class HuntersDefeatedListener(private val hunterId: String): FleetEventListener {
    override fun reportFleetDespawnedToListener(
        fleet: CampaignFleetAPI?,
        reason: CampaignEventListener.FleetDespawnReason?,
        param: Any?
    ) {
        if(reason != CampaignEventListener.FleetDespawnReason.DESTROYED_BY_BATTLE){
            reAddToSpawn()
        }
    }

    private fun reAddToSpawn(){
        hunterFleetsById[hunterId]?.let {
            hunterFleetsToSpawn[hunterId] = it
        }
    }

    private fun addItemToPlayerWithText(itemId: String){
        Global.getSector()?.playerFleet?.cargo?.addSpecial(SpecialItemData(itemId, itemId),1f)
        Global.getSector()?.campaignUI?.addMessage("You discovered a blueprint among the debris!")
        Global.getSector()?.campaignUI?.addMessage("Added 1 ${Global.getSettings()?.getSpecialItemSpec(itemId)?.name}!")
        if(SvcShouldSpawnHunterBarEvent.hasAlreadyTriggered){
            Global.getSector()?.campaignUI?.addMessage("This must be the equipment that the man in the bar was talking about!")
        }
    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        if (primaryWinner?.isPlayerFleet == true) {
            defeatedHunterFleets++
            when(defeatedHunterFleets){
                1 -> addItemToPlayerWithText("svc_control_collar_bp")
                2 -> addItemToPlayerWithText("svc_alpha_collar_bp")
            }
        }else{
            reAddToSpawn()
        }
    }
}