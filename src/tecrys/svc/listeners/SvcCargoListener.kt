package tecrys.svc.listeners

import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.listeners.ShowLootListener
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import tecrys.svc.SVC_FACTION_ID
import tecrys.svc.VOID_CHITIN_ID
import tecrys.svc.VWL_FACTION_ID
import kotlin.math.max

object SvcCargoListener: ShowLootListener {
    override fun reportAboutToShowLootToPlayer(loot: CargoAPI?, dialog: InteractionDialogAPI?) {
        val fleet = dialog?.interactionTarget as? CampaignFleetAPI
        val hullVar = ((dialog?.interactionTarget as? CustomCampaignEntityAPI)?.customPlugin as? DerelictShipEntityPlugin)?.data?.ship?.getVariant()?.hullVariantId
        if(fleet?.faction?.id == SVC_FACTION_ID || fleet?.faction?.id == VWL_FACTION_ID || hullVar?.startsWith("svc_") == true){
            loot?.run {
                val n = getCommodityQuantity("metals")
                addSpecial(SpecialItemData(VOID_CHITIN_ID, VOID_CHITIN_ID), Math.random().toFloat() * n)
                addCommodity("organics", n)
                addCommodity("volatiles", max(Math.random().toFloat() - 0.5f, 0f) * n)
                removeCommodity("metals", n)
                removeCommodity("heavy_machinery", getCommodityQuantity("heavy_machinery"))
            }
        }

    }
}