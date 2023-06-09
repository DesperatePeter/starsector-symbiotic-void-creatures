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
import kotlin.math.max

abstract class NotificationDialogBase(private val text: String, private val title: String, private val spriteName: String?): InteractionDialogPlugin {
    protected var dialog: InteractionDialogAPI? = null

    companion object{
        const val PANEL_WIDTH = 1200f
    }
    override fun init(dialog: InteractionDialogAPI?) {
        this.dialog = dialog
        var imgHeight = 600f
        var imgWidth = 10f
        spriteName?.let {
            imgHeight = Global.getSettings().getSprite(spriteName).height
            imgHeight = max(imgHeight, 600f)
            imgWidth = Global.getSettings().getSprite(spriteName).width
        }
        val panel = dialog?.visualPanel?.showCustomPanel(PANEL_WIDTH + 10f,  imgHeight + 10f, object : CustomUIPanelPlugin {
            override fun positionChanged(position: PositionAPI?) {}
            override fun renderBelow(alphaMult: Float) {}
            override fun render(alphaMult: Float) {}
            override fun advance(amount: Float) {}
            override fun processInput(events: MutableList<InputEventAPI>?) {}
            override fun buttonPressed(buttonId: Any?) {}
        })
        panel?.position?.inTMid(20f)

        val imgBox = panel?.createUIElement(imgWidth + 10f, imgHeight + 10f, false)

        spriteName?.let {
            imgBox?.addImage(it, 5f)
        }
        panel?.addUIElement(imgBox)?.inTL(1f, 1f)

        val textBox = panel?.createUIElement(PANEL_WIDTH - imgWidth - 10f, imgHeight + 10f, false)

        textBox?.addTitle(title)
        textBox?.addPara(text, 10f)

        panel?.addUIElement(textBox)?.rightOfMid(imgBox, 1f)

        dialog?.optionPanel?.let { addOptions(it) }
    }

    abstract fun addOptions(options: OptionPanelAPI)

    override fun optionMousedOver(optionText: String?, optionData: Any?) {}

    override fun advance(amount: Float) {}

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {}

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}