package tecrys.svc.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import tecrys.svc.*
import tecrys.svc.world.notifications.NotificationShower

object SvcFleetListener : FleetEventListener {
    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {
    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?
    ) {
        if (primaryWinner?.isPlayerFleet == true) {
            if (!Global.getSector().memory.contains(SVC_FLEET_DEFEATED_MEM_KEY)) {
                NotificationShower.showNotificationOnce(NotificationShower.VOIDLINGS_DEFEATED_ID)
            }
            // mem-key enables magic bounty
            Global.getSector().memory.set(SVC_FLEET_DEFEATED_MEM_KEY, true)
        }
    }
}

