package tecrys.svc.world.notifications

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import tecrys.svc.MAGIC_BOUNTY_DEFEATED_KEY
import tecrys.svc.WHALES_ENCOUNTER_MEM_KEY

class NotificationShower : EveryFrameScript {

    companion object{
        const val VOIDLINGS_DEFEATED_ID = "voidlings_defeated"
        const val MAGIC_BOUNTY_DEFEATED_ID = "magic_bounty_defeated"
        const val WHALES_PROTECTED_ID = "whales_protected"
        const val WHALES_DEAD_ID = "whales_dead"
        const val HUNTER_FLEET_APPROACHING_ID = "hunters_approaching"
        const val HUNTERS_DEFEATED_ID = "hunters_defeated"
        private val notifications = mapOf(
            VOIDLINGS_DEFEATED_ID to object : NotificationBase(VOIDLINGS_DEFEATED_ID) {
                override fun create(): InteractionDialogPlugin = DefeatedVoidlingsNotificationDialog()
            },
            MAGIC_BOUNTY_DEFEATED_ID to object : NotificationBase(MAGIC_BOUNTY_DEFEATED_ID) {
                override fun create(): InteractionDialogPlugin = DefeatedMagicBountyDialog()
                override fun showAutomaticallyIf(): Boolean {
                    return Global.getSector()?.memory?.contains(MAGIC_BOUNTY_DEFEATED_KEY) == true
                            && Global.getSector()?.memory?.getBoolean(MAGIC_BOUNTY_DEFEATED_KEY) == true
                }
            },
            WHALES_PROTECTED_ID to object : NotificationBase(WHALES_PROTECTED_ID){
                override fun create(): InteractionDialogPlugin = ProtectedWhalesDialog(
                    Global.getSector()?.memory?.getFleet(WHALES_ENCOUNTER_MEM_KEY))
            },
            WHALES_DEAD_ID to object : NotificationBase(WHALES_DEAD_ID){
                override fun create(): InteractionDialogPlugin = WhalesDiedDialog()
            },
            HUNTER_FLEET_APPROACHING_ID to object : NotificationBase(HUNTER_FLEET_APPROACHING_ID){
                override fun create(): InteractionDialogPlugin = HunterFleetApproachingNotification()
            },
            HUNTERS_DEFEATED_ID to object : NotificationBase(HUNTERS_DEFEATED_ID){
                override fun create(): InteractionDialogPlugin = HuntersDefeatedNotification()
            }
        )
        fun showNotificationOnce(id: String){
            notifications[id]?.shouldBeShownOnce = true
        }
        fun showNotificationRepeatable(id: String){
            notifications[id]?.shouldBeShownRepeatable = true
        }
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = false

    override fun advance(amount: Float) {
        if (Global.getSector().isInNewGameAdvance || Global.getSector().campaignUI.isShowingDialog
            || Global.getCurrentState() == GameState.TITLE
        ) return
        notifications.values.forEach {
            it.showIfRequested()
        }
    }
}
