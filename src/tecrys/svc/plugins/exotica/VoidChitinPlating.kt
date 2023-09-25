package tecrys.svc.plugins.exotica

import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import tecrys.svc.VOID_CHITIN_ID

class VoidChitinPlating(key: String, settings: JSONObject) : Exotic(key, settings) {
    companion object{
        const val ITEM = VOID_CHITIN_ID
        const val REQUIRED_ITEM_QUANTITY = 1f
    }

    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return Utilities.getItemQuantity(fleet.cargo, ITEM) >= REQUIRED_ITEM_QUANTITY
    //fleet.cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, SpecialItemData(ITEM, ITEM)) >= REQUIRED_ITEM_QUANTITY
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
        Utilities.takeItemQuantity(fleet.cargo, ITEM, REQUIRED_ITEM_QUANTITY)
        return true
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        // TODO (the current code is just here to make it REALLY obvious for testing if the exotic is installed)
        stats.armorDamageTakenMult.modifyMult(id, 0.01f)
        stats.hullDamageTakenMult.modifyMult(id, 0.01f)
    }

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        title: UIComponentAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData,
        expand: Boolean
    ) {
        // TODO
    }

}