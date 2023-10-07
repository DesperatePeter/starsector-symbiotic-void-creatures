package tecrys.svc.plugins.exotica

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShieldSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.util.Utilities
import org.json.JSONObject
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.VOID_CHITIN_ID
import tecrys.svc.utils.CampaignSettingDelegate
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

class VoidChitinPlating(key: String, settings: JSONObject) : Exotic(key, settings) {
    companion object{
        const val ITEM = VOID_CHITIN_ID
        const val REQUIRED_ITEM_QUANTITY = 1000f
    }

    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        if(Global.getSettings().isDevMode) return true
        return Utilities.getItemQuantity(fleet.cargo, ITEM) >= REQUIRED_ITEM_QUANTITY
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
        if(Global.getSettings().isDevMode) return true
        Utilities.takeItemQuantity(fleet.cargo, ITEM, REQUIRED_ITEM_QUANTITY)
        return true
    }

    override fun canApply(member: FleetMemberAPI, variant: ShipVariantAPI, mods: ShipModifications?): Boolean {
        return variant.hullSpec?.defenseType != ShieldAPI.ShieldType.NONE && variant.hullSpec?.shipDefenseId != "parry"
    }

    override var canDropFromCombat: Boolean = false

    override fun getCannotApplyReasons(member: FleetMemberAPI, mods: ShipModifications?): List<String> {
        val toReturn = mutableListOf<String>()
        if(member.variant.hullSpec.defenseType == ShieldAPI.ShieldType.NONE){
            toReturn.add("Cannot be added to ships with no defensive system.")
        }
        if(member.variant.hullSpec.shipDefenseId == "parry"){
            toReturn.add("Cannot be added to ships that already have the parry defense system.")
        }
        return toReturn
    }
    override fun onInstall(member: FleetMemberAPI) {
        VoidChitinPlatingListener.installOnMember(member)
    }

    override fun onDestroy(member: FleetMemberAPI) {
        VoidChitinPlatingListener.uninstallFromMember(member)
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