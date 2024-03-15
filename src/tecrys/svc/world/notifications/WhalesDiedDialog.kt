package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.OptionPanelAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.utils.addLeaveOption

class WhalesDiedDialog: NotificationDialogBase(
Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_whales_dead_text"),
Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_whales_title"),
Global.getSettings().getSpriteName("backgrounds", "whale_encounter")) {

    override fun addOptions(options: OptionPanelAPI) {
        options.addLeaveOption()
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        optionText?.let {
            when(it){
                "Leave" -> dialog?.dismiss()
                else -> {}
            }
        }
    }
}