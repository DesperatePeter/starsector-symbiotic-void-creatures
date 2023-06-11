package tecrys.svc.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate

class NotificationShower : EveryFrameScript {
    companion object{
        var shouldNotificationBeShown = false
            set(value) {field = value && !hasNotificationBeenShown}
        private var hasNotificationBeenShown : Boolean
        by CampaignSettingDelegate("$" + SVC_MOD_ID + "wasNotificationShown", false)
    }

    override fun isDone(): Boolean = hasNotificationBeenShown

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if (Global.getSector().isInNewGameAdvance || Global.getSector().campaignUI.isShowingDialog
            || Global.getCurrentState() == GameState.TITLE
        ) return
        if(shouldNotificationBeShown){
            val notification = DefeatedVoidlingsNotificationDialog()
            Global.getSector()?.campaignUI?.showInteractionDialog(notification , Global.getSector().playerFleet)
            hasNotificationBeenShown = true
            shouldNotificationBeShown = false
        }
    }
}
