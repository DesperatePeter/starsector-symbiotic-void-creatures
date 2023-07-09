package tecrys.svc.world.notifications

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import tecrys.svc.MAGIC_BOUNTY_DEFEATED_KEY
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate

class NotificationShower : EveryFrameScript {

    companion object{
        private val notifications = mapOf(
            "voidlings_defeated" to object : NotificationBase("voidlings_defeated") {
                override fun create(): InteractionDialogPlugin = DefeatedVoidlingsNotificationDialog()
            },
            "magic_bounty_defeated" to object : NotificationBase("magic_bounty_defeated") {
                override fun create(): InteractionDialogPlugin = DefeatedMagicBountyDialog()
                override fun showAutomaticallyIf(): Boolean {
                    return Global.getSector()?.memory?.contains(MAGIC_BOUNTY_DEFEATED_KEY) == true
                            && Global.getSector()?.memory?.getBoolean(MAGIC_BOUNTY_DEFEATED_KEY) == true
                }
            }
        )
        fun showNotificationOnce(id: String){
            notifications[id]?.shouldBeShown = true
        }
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if (Global.getSector().isInNewGameAdvance || Global.getSector().campaignUI.isShowingDialog
            || Global.getCurrentState() == GameState.TITLE
        ) return
        notifications.values.forEach {
            it.showOnceIfRequested()
        }
    }
}
