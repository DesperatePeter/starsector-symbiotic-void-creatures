package tecrys.svc.world.fleets.dialog

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.campaign.VisualPanelAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.util.Misc

class MastermindInteractionDialog(mastermindFleet: CampaignFleetAPI): InteractionDialogPlugin {

    companion object{
        enum class Stage{
            INITIAL
        }
        private val spriteName = Global.getSettings().getSpriteName("backgrounds", "mastermind_encounter_dialog")
    }

    private var dialog: InteractionDialogAPI? = null
    private var textPanel: TextPanelAPI? = null
    private var visualPanel: VisualPanelAPI? = null
    private var optionPanel: OptionPanelAPI? = null
    private var stage = Stage.INITIAL
    override fun init(dialog: InteractionDialogAPI?) {
        this.dialog = dialog
        textPanel = dialog?.textPanel
        visualPanel = dialog?.visualPanel
        optionPanel = dialog?.optionPanel
        spriteName?.let {
            val imgHeight = Global.getSettings().getSprite(spriteName).height
            val imgWidth = Global.getSettings().getSprite(spriteName).width
            visualPanel?.showImageVisual(InteractionDialogImageVisual(spriteName, imgWidth, imgHeight))
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        TODO("Not yet implemented")
    }

    override fun optionMousedOver(optionText: String?, optionData: Any?) {
        TODO("Not yet implemented")
    }

    override fun advance(amount: Float) {
        TODO("Not yet implemented")
    }

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {
        TODO("Not yet implemented")
    }

    override fun getContext(): Any {
        TODO("Not yet implemented")
    }

    override fun getMemoryMap(): MutableMap<String, MemoryAPI> {
        TODO("Not yet implemented")
    }

    private fun populateText(stage: Stage){
        when(stage){
            Stage.INITIAL -> populateInitialText()
        }
    }

    private fun populateInitialText(){

    }
}