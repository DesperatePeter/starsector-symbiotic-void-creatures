package tecrys.svc.listeners

import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import tecrys.svc.canRecoverAlphas
import tecrys.svc.canRecoverVoidlings
import tecrys.svc.utils.unlockVoidlingRecovery
import tecrys.svc.world.notifications.NotificationShower

object HuntersDefeatedListener: FleetEventListener {
    override fun reportFleetDespawnedToListener(
        fleet: CampaignFleetAPI?,
        reason: CampaignEventListener.FleetDespawnReason?,
        param: Any?
    ) {}

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        if (primaryWinner?.isPlayerFleet == true) {
            val showNotification = !canRecoverAlphas
            if(canRecoverVoidlings){
                canRecoverAlphas = true
            }else{
                canRecoverVoidlings = true
            }
            unlockVoidlingRecovery()
            if(showNotification) NotificationShower.showNotificationRepeatable(NotificationShower.HUNTERS_DEFEATED_ID)
        }
    }
}