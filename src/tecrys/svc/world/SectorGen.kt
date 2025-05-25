package tecrys.svc.world

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin
import org.magiclib.util.MagicSettings
import tecrys.svc.*
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.world.fleets.dialog.MastermindInteractionDialog


class SectorGen : SectorGeneratorPlugin {
    companion object {
        private const val RELATIONSHIP_TO_SET = -0.8f
        val ignoredFactions = MagicSettings.getList(MAGIC_SETTINGS_MOD_KEY, MAGIC_SETTINGS_RELATIONS_KEY) +
                listOf(SVC_FACTION_ID, MMM_FACTION_ID)
        var relationInitIsDone: Boolean by CampaignSettingDelegate("$${SVC_MOD_ID}isRelationshipIniDone", false)
    }

    override fun generate(sector: SectorAPI?) {
        if (!MastermindInteractionDialog.isSubmission && !relationInitIsDone) {
            Global.getSector()?.run {
                val mmm = getFaction(MMM_FACTION_ID)
                val svc = getFaction(SVC_FACTION_ID)
                val vwl = getFaction(VWL_FACTION_ID)
                allFactions.filterNotNull().filterNot {
                    SvcBasePlugin.ignoredFactions.contains(it.id)
                }.forEach {
                    svc.setRelationship(it.id, RepLevel.VENGEFUL)
                    mmm.setRelationship(it.id, RepLevel.VENGEFUL)
                }
                svc.setRelationship("player", RepLevel.VENGEFUL)
                mmm.setRelationship("player", RepLevel.VENGEFUL)
                vwl.setRelationship("player", RepLevel.FRIENDLY)
                svc.setRelationship(mmm.id, RepLevel.VENGEFUL)
                relationInitIsDone = true
            }
        }
    }
}