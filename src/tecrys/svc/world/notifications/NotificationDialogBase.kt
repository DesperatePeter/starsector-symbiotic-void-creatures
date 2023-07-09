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

abstract class NotificationDialogBase(private val text: String, private val title: String, private val spriteName: String?): InteractionDialogPlugin {
    protected var dialog: InteractionDialogAPI? = null
    override fun init(dialog: InteractionDialogAPI?) {
        this.dialog = dialog
        val panel = dialog?.visualPanel?.showCustomPanel(1210f, 650f, object : CustomUIPanelPlugin {
            override fun positionChanged(position: PositionAPI?) {}
            override fun renderBelow(alphaMult: Float) {}
            override fun render(alphaMult: Float) {}
            override fun advance(amount: Float) {}
            override fun processInput(events: MutableList<InputEventAPI>?) {}
            override fun buttonPressed(buttonId: Any?) {}
        })
        panel?.position?.inTMid(20f)
        val imgBox = panel?.createUIElement(1200f, 640f, false)
        imgBox?.addTitle(title)
        imgBox?.addPara(text, 10f)
        spriteName?.let {
            imgBox?.addImage(it, 10f)
        }

        panel?.addUIElement(imgBox)
        dialog?.optionPanel?.let { addOptions(it) }
    }

    abstract fun addOptions(options: OptionPanelAPI)

    override fun optionMousedOver(optionText: String?, optionData: Any?) {}

    override fun advance(amount: Float) {}

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {}

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}