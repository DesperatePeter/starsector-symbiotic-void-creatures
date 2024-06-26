package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import java.awt.Color.blue

class DefeatedVoidlingsNotificationDialog : NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_voidlings_defeated_text"),
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_voidlings_defeated_title"),
    Global.getSettings().getSpriteName("backgrounds", "svc_encounter")) {

    private var phase = 0

    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            if(phase <= 0) addOption("Signal your security personnel to let the man speak.", "Speak")
            addOption("Leave", "Leave")
            setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, false)
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        (optionData as? String)?.let {
            when (it) {
                "Leave" -> {
                    dialog?.dismiss()
                }

                "Speak"-> {
                    dialog?.textPanel?.addParagraph("Signal your security personnel to let the man speak.", Global.getSettings().basePlayerColor)
                    dialog?.textPanel?.addParagraph(Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_voidlings_defeated_text_continue"))
                    phase = 1
                    dialog?.optionPanel?.let { opt ->
                        opt.clearOptions()
                        addOptions(opt)
                    }
                }

                else -> {}
            }
        }
    }
}