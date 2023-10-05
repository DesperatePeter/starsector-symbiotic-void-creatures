package tecrys.svc.listeners

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen

class VoidlingFIDConf: FIDConfigGen {
    override fun createConfig(): FleetInteractionDialogPluginImpl.FIDConfig {
        val config = FIDConfig()
        config.run {
            alwaysPursue = true
            showCommLinkOption = false
            straightToEngage = true
            alwaysAttackVsAttack = true
            leaveAlwaysAvailable = false
        }
        return config
    }
}