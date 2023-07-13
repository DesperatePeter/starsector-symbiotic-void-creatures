package tecrys.svc.world

import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin
import org.magiclib.util.MagicSettings
import tecrys.svc.MAGIC_SETTINGS_MOD_KEY
import tecrys.svc.MAGIC_SETTINGS_RELATIONS_KEY
import tecrys.svc.SVC_FACTION_ID

class SectorGen: SectorGeneratorPlugin {
    private val relationshipToSet = -0.5f
    override fun generate(sector: SectorAPI?) {
        val ignoredFactions = MagicSettings.getList(MAGIC_SETTINGS_MOD_KEY, MAGIC_SETTINGS_RELATIONS_KEY) +
                listOf(SVC_FACTION_ID)
        sector?.run {
            val svc = getFaction(SVC_FACTION_ID)
                allFactions.filterNotNull().filterNot {
                ignoredFactions.contains(it.id)
            }.forEach {
                svc.setRelationship(it.id, relationshipToSet)
                it.setRelationship(SVC_FACTION_ID, relationshipToSet)
            }
        }
    }
}