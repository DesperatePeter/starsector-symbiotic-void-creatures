package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.OptionPanelAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.canRecoverAlphas

class HuntersDefeatedNotification : NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_hunter_fleet_defeated_text") +
            if(canRecoverAlphas) "You can now recover alpha voidlings." else "You can now recover basic voidlings.",
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_hunter_fleet_defeated_title"),
    Global.getSettings().getSpriteName("backgrounds", "hunter_fleet")
) {
    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            addOption("Leave", "Leave")
            setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, false)
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        optionText?.let {
            when (it) {
                "Leave" -> dialog?.dismiss()
                else -> {}
            }
        }
    }
}