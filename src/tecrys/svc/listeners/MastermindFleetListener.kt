package tecrys.svc.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignEventListener
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.listeners.FleetEventListener
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent.Companion.MEM_KEY_RESOLUTION_BOSS_FIGHT_WIN

class MastermindFleetListener: FleetEventListener {
    override fun reportFleetDespawnedToListener(
        fleet: CampaignFleetAPI?,
        reason: CampaignEventListener.FleetDespawnReason?,
        param: Any?
    ) {}

    override fun reportBattleOccurred(fleet: CampaignFleetAPI?, primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        if(primaryWinner != Global.getSector().playerFleet) return
        if(fleet?.membersWithFightersCopy?.none { it.hullSpec?.baseHull?.hullId == "svc_mastermind" } == true){
            Global.getSector().memoryWithoutUpdate[MEM_KEY_RESOLUTION_BOSS_FIGHT_WIN] = true
        }
    }
}