package tecrys.svc.world

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin
import org.magiclib.util.MagicSettings
import tecrys.svc.*
import tecrys.svc.world.fleets.dialog.MastermindInteractionDialog


class SectorGen : SectorGeneratorPlugin {
    companion object {
        private const val RELATIONSHIP_TO_SET = -0.8f
        val ignoredFactions = MagicSettings.getList(MAGIC_SETTINGS_MOD_KEY, MAGIC_SETTINGS_RELATIONS_KEY) +
                listOf(SVC_FACTION_ID)
        var RelationIsDone: Boolean = false
    }

    override fun generate(sector: SectorAPI?) {

        val system = sector!!.createStarSystem("Viuoarg")
        system.location.set(20000f, 20000f);
        val Viuoarg = system.initStar("Viuoarg", "star_yellow", 600f, 350f, 11f, 1.3f, 2f)

//        val svcStable: SectorEntityToken = system.addCustomEntity("base_campaign_objective_svc", "SVC Stable Location", "stable_location", null);
//        svcStable.setCircularOrbitPointingDown(Viuoarg, -60f, 7800f, 400f);

        if ((!MastermindInteractionDialog.isSubmission
                    || (Global.getSector()?.getFaction(SVC_FACTION_ID)?.relToPlayer?.isAtWorst(RepLevel.FRIENDLY) == false))
            && !RelationIsDone
        ) {
            Global.getSector()?.run {
                val svc = getFaction(SVC_FACTION_ID)
                allFactions.filterNotNull().filterNot {
                    SvcBasePlugin.ignoredFactions.contains(it.id)
                }.forEach {
                    svc.setRelationship(it.id, RepLevel.VENGEFUL)
//                    svc.setRelationship(it.id, RELATIONSHIP_TO_SET)
                }
                svc.setRelationship(svc.id, RepLevel.VENGEFUL)
                val vwl = getFaction(VWL_FACTION_ID)
                vwl.setRelationship(svc.id, RepLevel.VENGEFUL)
//                vwl.setRelationship(svc.id, RELATIONSHIP_TO_SET)
                vwl.setRelationship("player", RepLevel.FRIENDLY)
                val mmm = getFaction(MMM_FACTION_ID)
                mmm.setRelationship("player", RepLevel.VENGEFUL)
                RelationIsDone = true
            }
        }
    }
}