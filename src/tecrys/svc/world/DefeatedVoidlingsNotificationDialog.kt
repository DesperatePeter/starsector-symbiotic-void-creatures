package tecrys.svc.world

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY

class DefeatedVoidlingsNotificationDialog : InteractionDialogPlugin {
    private var dialog: InteractionDialogAPI? = null
    override fun init(dialog: InteractionDialogAPI?) {
        this.dialog = dialog
        val panel = dialog?.visualPanel?.showCustomPanel(1210f, 650f, object : CustomUIPanelPlugin{
            override fun positionChanged(position: PositionAPI?) {}
            override fun renderBelow(alphaMult: Float) {}
            override fun render(alphaMult: Float) {}
            override fun advance(amount: Float) {}
            override fun processInput(events: MutableList<InputEventAPI>?) {}
            override fun buttonPressed(buttonId: Any?) {}
        })
        panel?.position?.inTMid(20f)
        val imgBox = panel?.createUIElement(1200f, 640f, false)
        imgBox?.addTitle(Global.getSettings().getString(
            SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_voidlings_defeated_title"
        ))
        imgBox?.addPara(Global.getSettings().getString(
            SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_voidlings_defeated_text"
        ), 10f)
        imgBox?.addImage(Global.getSettings().getSpriteName("backgrounds", "victory_notification_img"), 10f)
        panel?.addUIElement(imgBox)
        dialog?.optionPanel?.addOption("Leave", "Leave")
        dialog?.optionPanel?.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, false)
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