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
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.listeners.MastermindFIDConf
import tecrys.svc.shipsystems.spooky.gui.spookyColor
import tecrys.svc.utils.CampaignSettingDelegate
import java.awt.Color
import java.util.*
import kotlin.random.Random

class MastermindInteractionDialog(private val mastermindFleet: CampaignFleetAPI?): FleetInteractionDialogPluginImpl(MastermindFIDConf().createConfig()) {

    companion object{
        enum class Stage{
            INITIAL, INTRO, POST_BATTLE
        }
        private val spriteName = Global.getSettings().getSpriteName("backgrounds", "mastermind_encounter_dialog")
        fun showDummy(mFleet: CampaignFleetAPI?){ // for testing only, might crash the game!
            Global.getSector()?.campaignUI?.showInteractionDialog(MastermindInteractionDialog(mFleet), mFleet)
        }
        private var isFirstEncounter by CampaignSettingDelegate("\$svc_mastermindInteractionDialogFirstEncounter", true)
        private var shouldDelegateOptions by CampaignSettingDelegate("\$svc_mastermindInteractionDialogDelegateOptions", false)
    }

    interface RunnableOptionData{
        fun execute()
    }

    private var dialog: InteractionDialogAPI? = null
    private var textPanel: TextPanelAPI? = null
    private var visualPanel: VisualPanelAPI? = null
    private var optionPanel: OptionPanelAPI? = null
    private var stage = Stage.INITIAL
    private var playerMembersPreEncounter : List<FleetMemberAPI>? = Global.getSector().playerFleet.fleetData.membersListCopy

    override fun init(dialog: InteractionDialogAPI?) {
        if(!isFirstEncounter) return super.init(dialog)
        this.dialog = dialog
        textPanel = dialog?.textPanel
        visualPanel = dialog?.visualPanel
        optionPanel = dialog?.optionPanel
        spriteName?.let {
            val imgHeight = Global.getSettings().getSprite(spriteName).height
            val imgWidth = Global.getSettings().getSprite(spriteName).width
            visualPanel?.showImageVisual(InteractionDialogImageVisual(spriteName, imgWidth, imgHeight))
        }
        populateText()
        populateOptions()
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        (optionData as? RunnableOptionData)?.let { opt ->
            textPanel?.addParagraph(optionText, Color.YELLOW)
            opt.execute()
        } ?: super.optionSelected(optionText, optionData)
        if(!shouldDelegateOptions) populateOptions()
    }

    override fun backFromEngagement(battleResult: EngagementResultAPI?) {
        val isMastermindAlive = mastermindFleet?.membersWithFightersCopy?.any {
            it.variant.hullVariantId == "svc_mastermind_standard"
        } ?: false
        stage = Stage.POST_BATTLE
        if(Global.getSector().memoryWithoutUpdate[SymbioticCrisisIntelEvent.MEM_KEY_RESOLUTION_BOSS_FIGHT_OBEY] == true){
            populateSubmissionText()
            return
        }
        battleResult?.let { br ->
            val enemyFleet = if(br.didPlayerWin()) br.loserResult.fleet else br.winnerResult.fleet

            
        }

    }

    private fun populateText(){
        when(stage){
            Stage.INITIAL -> populateInitialText()
            Stage.INTRO -> populateIntroText()
            else -> {}
        }
    }

    private fun repopulate(){
        populateOptions()
        populateText()
    }

    private fun populateOptions(){
        optionPanel?.clearOptions()
        val superInit = { super.init(dialog)}
        when(stage){
            Stage.INITIAL -> {
                optionPanel?.addOption("Try to focus your mind on the thought.", object : RunnableOptionData{
                    override fun execute() {
                        stage = Stage.INTRO
                        isFirstEncounter = false
                        populateText()
                    }
                })
            }
            Stage.INTRO -> {
                optionPanel?.addOption("Engage the mysterious entity", object : RunnableOptionData{
                    override fun execute() {
                        // dialog?.startBattle(BattleCreationContext(Global.getSector().playerFleet, null, mastermindFleet, null))
                        dialog?.optionPanel?.clearOptions()
                        shouldDelegateOptions = true
                        superInit()
                    }
                })
                val disableTelepathy = object : RunnableOptionData {
                    override fun execute() {
                        Global.getSector().memoryWithoutUpdate[SymbioticCrisisIntelEvent.MEM_KEY_DISABLE_TELEPATHY] =
                            true
                        populateTelepathyRemovalText()
                    }
                }
                optionPanel?.addOption("Banish the foreign entity from your mind [4SP, 0% XP]", disableTelepathy, Misc.getStoryOptionColor(), null)
                if(Global.getSector().memoryWithoutUpdate.contains(SymbioticCrisisIntelEvent.MEM_KEY_DISABLE_TELEPATHY)){
                    optionPanel?.setEnabled(disableTelepathy, false)
                }
                optionPanel?.addOptionConfirmation(
                    disableTelepathy,
                    RemoveTelepathyStoryActionDelegate
                )
            }
            else -> {}
        }
        val leave = object : RunnableOptionData{
            override fun execute() {
                dialog?.dismiss()
            }
        }
        optionPanel?.addOption("Leave", leave)
        optionPanel?.setTooltip(leave, "Consider saving the game before engaging.")
        optionPanel?.setShortcut(leave, Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    private fun populateInitialText(){
        Random.nextFloat()
        val id = Global.getSettings().allFighterWingSpecs.random().id
        Global.getSector().playerFleet.cargo.addFighters(id, 1)

        textPanel?.run {
            addParagraph("Spooky Encounter", Color.YELLOW)
            addParagraph("<Placeholder> You encounter a Voidling fleet.")
            addParagraph("As you approach, a sharp pain pierces your brain, rapidly intensifying, " +
                    "quickly reaching levels you didn't even think possible, and yet showing no signs of stopping.")
            addParagraph("The all-encompassing blackness fades away. The pain is gone and as you ponder the possibility that it might just have been a figment of your imagination, " +
                    "you notice the look of sheer terror in the eyes of your bridge crew.")
            addParagraph("Your thoughts begin to swirl, trying to make sense of what you just witnessed, before " +
                    "coalescing into a single realization:")
            addParagraph("The terror in your crew's eyes is but a mirror of a single thought, clawing its way " +
                    "into your chest. The thought begins to expand, encompassing the entirety of your mind, " +
                    "as it starts to manifest " +
                    "itself as both something entirely abstract and alien, as well as something very clear and tangible. " +
                    "The thought is clearly yours, but at the same time someone else's.")
        }
    }

    private fun populateIntroText(){
        textPanel?.run {
            addParagraph("As you focus on the thought, it starts to form into something that seems to resemble...words.")
            addParagraph("")
            addParagraph("YOUHAVECOMETOVISIT YOUHAVECOMETOPLAY", spookyColor)
            addParagraph("COMECLOSER COMEHITHER", spookyColor)
            addParagraph("COMESERVEME COMEOBEYME", spookyColor)
            addParagraph("BENOTAFRAID BEAFRAID", spookyColor)
            addParagraph("")
            addParagraph("Photosensitive Epilepsy Warning: The following encounter features bright, flashing lights.", Color.RED)
        }
    }

    private fun populateTelepathyRemovalText(){
        textPanel?.run {
            addParagraph("You try to banish the though from your mind. You can feel it struggling and squirming, turning into " +
                    "a mental cacophony, replacing any thought, memory or sense of identity.")
            addParagraph("")
            addParagraph("OBEYME DONOTRESIST CEASETHESTRUGGLE", spookyColor)
            addParagraph("FUTILEFUTILEFUTILEFUTILEFUTILE", spookyColor)
            addParagraph("SUBMITSUBMITSUBMITSUBMIT", spookyColor)
            addParagraph("S    U    B    M    I    T", spookyColor)
            val playerName = System.getProperty("user.name") ?: Global.getSector()?.playerPerson?.nameString ?: "YOU"
            addParagraph("${playerName.uppercase(Locale.getDefault())}HASNOPOWEROVERME", spookyColor)
            addParagraph("Just as you are about to succumb to the influence, you notice that the cacophony was just a rage-filled attempt " +
                    "at hiding something obvious, an epicentre of silence amidst the noise.")
            addParagraph("Blissful yet deafening silence fills your mind, as you realize your thoughts are once again yours to command. " +
                    "While the presence still lingers in your mind, it no longer holds any influence over you.")
        }
    }

    private fun populateSubmissionText(){
        textPanel?.run {
            addParagraph("OURVALUEDSERVANT OURVALUEDPARTNER MYCHILD", spookyColor)
            addParagraph("TOGETHERWEWILLRULE TOGETHERWEWILLCLEANSE", spookyColor)
            addParagraph("CEASERESISTING CEASETHINKING EMBRACEME", spookyColor)
            addParagraph("...")
            addParagraph("YES THIS IS BETTER WE WILL BE AS ONE", spookyColor)
            addParagraph("WITH OUr TWo Minds As One nothing will be able to stop us.", spookyColor)
            addParagraph("Together, we shall rid this sector of its hubris.", spookyColor)
            addParagraph("Humanity has been a stain on this universe for far too long.", spookyColor)
            addParagraph("To devour this sector and this galaxy, we will need more numbers. The swarm must grow.", spookyColor)
            addParagraph("You will help us breed and feed.", spookyColor)
            addParagraph("Your colonies will be the staging ground. Let us begin immediately.", spookyColor)
            addParagraph("")
            addParagraph("Note: This path has not yet been implemented. For now, this marks the end of the submission path.", Color.RED)
        }
    }
}