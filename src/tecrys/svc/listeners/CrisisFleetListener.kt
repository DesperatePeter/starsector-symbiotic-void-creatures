package tecrys.svc.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import tecrys.svc.SVC_FLEET_DEFEATED_MEM_KEY
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.world.notifications.NotificationShower

class CrisisFleetListener : FleetEventListener {
    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?,
                                                reason: CampaignEventListener.FleetDespawnReason?,
                                                param: Any?) {
        SymbioticCrisisIntelEvent.reportFleetDefeated(false)
    }

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        if (primaryWinner?.isPlayerFleet == true) {
            SymbioticCrisisIntelEvent.reportFleetDefeated(true)
        }else{
            SymbioticCrisisIntelEvent.reportFleetDefeated(false)
        }
    }
}