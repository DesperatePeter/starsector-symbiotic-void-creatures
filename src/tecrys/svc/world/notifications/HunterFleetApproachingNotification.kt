package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.OptionPanelAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.utils.addLeaveOption

class HunterFleetApproachingNotification: NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_hunter_fleet_approaching_text"),
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_hunter_fleet_approaching_title"),
    Global.getSettings().getSpriteName("backgrounds", "hunter_fleet")
) {
    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            addLeaveOption()
        }
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