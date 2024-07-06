package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate

abstract class NotificationBase(
    notificationId: String,
    private val interactionTarget: CampaignFleetAPI = Global.getSector().playerFleet,
) {
    abstract fun create(): InteractionDialogPlugin
    private var hasNotificationBeenShown: Boolean
            by CampaignSettingDelegate("$" + SVC_MOD_ID + notificationId + "wasNotificationShown", false)
    var shouldBeShownOnce = false
    var shouldBeShownRepeatable = false
    private fun showNotification() {
        val notification = create()
        Global.getSector()?.campaignUI?.showInteractionDialog(notification, interactionTarget)
        hasNotificationBeenShown = true
        shouldBeShownOnce = false
        shouldBeShownRepeatable = false
    }

    fun showIfRequested() {
        if ((shouldBeShownOnce || showAutomaticallyIf()) && !hasNotificationBeenShown) showNotification()
        if (shouldBeShownRepeatable) showNotification()
    }

    open fun showAutomaticallyIf() = false
}