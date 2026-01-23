package tecrys.svc.items

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import org.magiclib.kotlin.initConditionMarket
import tecrys.svc.utils.hasOrganics
import tecrys.svc.utils.hasVolatiles
import tecrys.svc.utils.showNotificationOnCampaignUi

class EnrichedFungusPlugin: BaseSpecialItemPlugin() {
    companion object{
        const val MAX_DIST_TO_PLANET = 100f
    }
    override fun performRightClickAction() {
        getRelevantPlanet()?.let { planet ->
            if(!planet.hasOrganics()) {
                planet.market?.addCondition(Conditions.ORGANICS_TRACE)
                planet.market?.getCondition(Conditions.ORGANICS_TRACE)?.isSurveyed = true
            }
            if(!planet.hasVolatiles()){
                planet.market?.addCondition(Conditions.VOLATILES_TRACE)
                planet.market?.getCondition(Conditions.VOLATILES_TRACE)?.isSurveyed = true
            }
            planet.market?.reapplyIndustries()
        } ?: run {
            showNotificationOnCampaignUi("Must be used while next to a habitable planet that doesn't already have both volatiles and organics.", "graphics/icons/svc_fungus.png")
        }
    }

    private fun getRelevantPlanet(): PlanetAPI?{
        val pf = Global.getSector().playerFleet ?: return null
        if(pf.isInHyperspace) return null
        val closestPlanet = pf.containingLocation?.planets?.filter {
            !it.hasCondition(Conditions.NO_ATMOSPHERE)
        }?.filterNot {
            it.hasVolatiles() && it.hasOrganics()
        }?.minByOrNull {
            (it.location - pf.location).length()
        } ?: return null
        if((closestPlanet.location - pf.location).length() - closestPlanet.radius <= MAX_DIST_TO_PLANET){
            return closestPlanet
        }
        return null
    }

    override fun hasRightClickAction(): Boolean = true

    override fun shouldRemoveOnRightClickAction(): Boolean = getRelevantPlanet() != null
}