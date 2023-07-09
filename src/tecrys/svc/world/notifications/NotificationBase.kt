package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate

abstract class NotificationBase(val notificationId: String) {
    abstract fun create(): InteractionDialogPlugin
    private var hasNotificationBeenShown : Boolean
            by CampaignSettingDelegate("$" + SVC_MOD_ID + notificationId + "wasNotificationShown", false)
    var shouldBeShown = false
    private fun showNotification(){
        val notification = create()
        Global.getSector()?.campaignUI?.showInteractionDialog(notification, Global.getSector().playerFleet)
        hasNotificationBeenShown = true
        shouldBeShown = false
    }
    fun showOnceIfRequested(){
        if((shouldBeShown || showAutomaticallyIf()) && !hasNotificationBeenShown) showNotification()
    }
    open fun showAutomaticallyIf() = false
}