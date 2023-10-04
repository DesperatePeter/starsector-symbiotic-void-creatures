package tecrys.svc.listeners

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.BaseFIDDelegate
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfigGen

class VoidlingFIDConf: FIDConfigGen {
    override fun createConfig(): FleetInteractionDialogPluginImpl.FIDConfig {
        val config = FIDConfig()
        config.delegate = object : BaseFIDDelegate() {
            override fun notifyLeave(dialog: InteractionDialogAPI?) {

            }

        }
        return config
    }
}