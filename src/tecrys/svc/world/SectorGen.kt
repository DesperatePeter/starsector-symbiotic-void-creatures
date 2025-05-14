package tecrys.svc.world

import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin
import org.magiclib.util.MagicSettings
import tecrys.svc.*
import tecrys.svc.SvcBasePlugin.Companion

class SectorGen : SectorGeneratorPlugin {
    companion object {
        private const val RELATIONSHIP_TO_SET = -0.8f
        val ignoredFactions = MagicSettings.getList(MAGIC_SETTINGS_MOD_KEY, MAGIC_SETTINGS_RELATIONS_KEY) +
                listOf(SVC_FACTION_ID)
    }

    override fun generate(sector: SectorAPI?) {
        sector?.run {
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
        }
    }
}