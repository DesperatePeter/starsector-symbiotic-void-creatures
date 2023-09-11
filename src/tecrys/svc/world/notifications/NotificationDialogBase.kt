package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
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
        spriteName?.let {
            val imgHeight = Global.getSettings().getSprite(spriteName).height
            val imgWidth = Global.getSettings().getSprite(spriteName).width
            dialog?.visualPanel?.showImageVisual(InteractionDialogImageVisual(spriteName, imgWidth, imgHeight))
        }
        dialog?.textPanel?.addParagraph(title)
        dialog?.textPanel?.addParagraph(text)
        dialog?.optionPanel?.let { addOptions(it) }

    }

    abstract fun addOptions(options: OptionPanelAPI)

    override fun optionMousedOver(optionText: String?, optionData: Any?) {}

    override fun advance(amount: Float) {}

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {}

    override fun getContext(): Any? = null

    override fun getMemoryMap(): MutableMap<String, MemoryAPI>? = null
}