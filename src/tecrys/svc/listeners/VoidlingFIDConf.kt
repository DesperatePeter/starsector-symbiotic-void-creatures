package tecrys.svc.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen

class VoidlingFIDConf: FIDConfigGen {
    override fun createConfig(): FIDConfig {
        val config = FIDConfig()
        config.run {
            alwaysPursue = true
            showCommLinkOption = false
            // straightToEngage = true
            alwaysAttackVsAttack = true
            leaveAlwaysAvailable = false
        }
        return config
    }
}

class MastermindInteractionDialog(params: FIDConfig? = null): FleetInteractionDialogPluginImpl(params) {
    companion object{
        val idsToReplace = listOf("initialWithStationVsLargeFleet", "initialAggressive", "initialAggressiveSide",
            "initialNeutral", "initialNeutralSide", "initialHoldVsStrongerEnemy", "initialHoldVsStrongerEnemySide")
    }
    override fun getString(id: String?): String {
        if(id in idsToReplace){
            return Global.getSettings().getString("svc_colony_crisis_intel", "mastermind_fleet_interaction_text")
        }
        return super.getString(id)
    }
}

class MastermindFIDConf: FIDConfigGen{
    override fun createConfig(): FIDConfig {
        val config = VoidlingFIDConf().createConfig()
        config.noSalvageLeaveOptionText
        return config
    }
}