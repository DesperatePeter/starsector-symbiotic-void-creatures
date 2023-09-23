package tecrys.svc.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import tecrys.svc.WHALES_ENCOUNTER_MEM_KEY
import tecrys.svc.world.notifications.NotificationShower

object WhaleFleetListener: FleetEventListener {
    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: CampaignEventListener.FleetDespawnReason?, param: Any?) {
    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        battle ?: return
        if(!battle.isPlayerInvolved) return
        if(fleet == null || fleet.isEmpty){
            NotificationShower.showNotificationOnce(NotificationShower.WHALES_DEAD_ID)
        }else{
            Global.getSector().memory.set(WHALES_ENCOUNTER_MEM_KEY, fleet)
            NotificationShower.showNotificationRepeatable(NotificationShower.WHALES_PROTECTED_ID)
        }
    }
}