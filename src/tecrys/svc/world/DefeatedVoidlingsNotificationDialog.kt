package tecrys.svc.world

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import tecrys.svc.SVC_FLEET_DEFEATED_NOTIFICATION_TEXT_KEY
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY

class DefeatedVoidlingsNotificationDialog : InteractionDialogPlugin {
    private var dialog: InteractionDialogAPI? = null
    override fun init(dialog: InteractionDialogAPI?) {
        this.dialog = dialog
        dialog?.textPanel?.addPara(
            Global.getSettings().getString(
            SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, SVC_FLEET_DEFEATED_NOTIFICATION_TEXT_KEY
            ))
        dialog?.optionPanel?.addOption("Leave", "Leave")
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        optionText?.let {
            when(it){
                "Leave" -> dialog?.dismiss()
                else -> {}
            }
        }
    }

    override fun optionMousedOver(optionText: String?, optionData: Any?) {}

    override fun advance(amount: Float) {}

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {}

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null

}