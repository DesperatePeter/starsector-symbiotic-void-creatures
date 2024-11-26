package tecrys.svc.world.fleets.dialog

import com.fs.starfarer.api.campaign.BaseStoryPointActionDelegate
import com.fs.starfarer.api.ui.TooltipMakerAPI

object RemoveTelepathyStoryActionDelegate: BaseStoryPointActionDelegate(){
    override fun getLogText(): String = "You manage to shut the thought out"
    override fun getRequiredStoryPoints(): Int {
        return 4
    }
    override fun getBonusXPFraction(): Float {
        return 0f
    }
    override fun withSPInfo(): Boolean = true
    override fun getTitle(): String {
        return "Banish the entity"
    }
    override fun createDescription(info: TooltipMakerAPI?) {
        info?.addTitle("Spend considerable mental effort to combat the influence the entity seems" +
                " to have on you and your fleet. This should make the battle to come considerably less stressful.")
    }
}