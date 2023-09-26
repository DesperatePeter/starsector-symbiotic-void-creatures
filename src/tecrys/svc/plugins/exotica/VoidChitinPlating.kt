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
        const val REQUIRED_ITEM_QUANTITY = 1f
        const val INSTALLED_CUSTOM_MEM_KEY = "$" + SVC_MOD_ID + "VCPInstalled"
        val backupHullspecs = mutableMapOf<String, ShipHullSpecAPI>()
        val installedOn: MutableSet<String> by CampaignSettingDelegate(INSTALLED_CUSTOM_MEM_KEY, mutableSetOf())
    }

    override fun canAfford(fleet: CampaignFleetAPI, market: MarketAPI?): Boolean {
        return Utilities.getItemQuantity(fleet.cargo, ITEM) >= REQUIRED_ITEM_QUANTITY
    //fleet.cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, SpecialItemData(ITEM, ITEM)) >= REQUIRED_ITEM_QUANTITY
    }

    override fun removeItemsFromFleet(fleet: CampaignFleetAPI, member: FleetMemberAPI, market: MarketAPI?): Boolean {
        Utilities.takeItemQuantity(fleet.cargo, ITEM, REQUIRED_ITEM_QUANTITY)
        return true
    }

    override fun canApply(member: FleetMemberAPI, variant: ShipVariantAPI, mods: ShipModifications?): Boolean {
        return variant.hullSpec.defenseType != ShieldAPI.ShieldType.NONE
    }

    override fun canDropFromFleets(): Boolean = false

    override fun getCannotApplyReasons(member: FleetMemberAPI, mods: ShipModifications?): List<String> {
        val toReturn = mutableListOf<String>()
        if(member.variant.hullSpec.defenseType == ShieldAPI.ShieldType.NONE){
            toReturn.add("Cannot be added to ships with no defensive system")
        }
        return toReturn
    }

    override fun onInstall(member: FleetMemberAPI) {
        if(!installedOn.contains(member.id)) installOnMember(member)
    }

    override fun advanceInCampaign(
        member: FleetMemberAPI,
        mods: ShipModifications,
        amount: Float,
        exoticData: ExoticData
    ) {
        onInstall(member)
    }

    private fun installOnMember(member: FleetMemberAPI){
        backupHullspecs[member.id] = member.hullSpec
        val clone = cloneHullSpec(member.hullSpec)
        member.variant.setHullSpecAPI(clone)
        member.variant.hullSpec.shipDefenseId = "parry"
        val shieldSpec = member.variant.hullSpec.shieldSpec
        setShieldType(shieldSpec, ShieldAPI.ShieldType.PHASE)
        installedOn.add(member.id)
    }


    override fun onDestroy(member: FleetMemberAPI) {
        val baseSpec = backupHullspecs[member.id] ?: Global.getSettings().getHullSpec(member.hullId) ?: return
        backupHullspecs.remove(member.id)
        installedOn.remove(member.id)
        member.variant.setHullSpecAPI(baseSpec)
//        member.variant.hullSpec.shipDefenseId = baseSpec.shipDefenseId
//        val shieldSpec = member.variant.hullSpec.shieldSpec
//        setShieldType(shieldSpec, baseSpec.shieldSpec.type)
    }

    private fun cloneHullSpec(hullSpec: ShipHullSpecAPI): ShipHullSpecAPI{
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass,
            "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        var foundMethod: Any? = null

        for (method in hullSpec::class.java.declaredMethods as Array<Any>)
        {
            if (method.toString().contains("clone"))
            {
                foundMethod = method
                break
            }
        }
        return invokeMethod.invoke(foundMethod, hullSpec) as ShipHullSpecAPI
    }

    private fun setShieldType(shieldSpec: ShieldSpecAPI, type: ShieldAPI.ShieldType){
        val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
        val invokeMethod = MethodHandles.lookup().findVirtual(methodClass,
            "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

        var foundMethod: Any? = null

        for (method in shieldSpec::class.java.declaredMethods as Array<Any>)
        {
            if (method.toString().contains("setType"))
            {
                foundMethod = method
            }
        }
        invokeMethod.invoke(foundMethod, shieldSpec, type)
    }

    override fun applyToShip(
        id: String,
        member: FleetMemberAPI,
        ship: ShipAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
//        ship.setShield(ShieldAPI.ShieldType.NONE, 0f, 0f, 0f)
//        Global.getSettings().getShipSystemSpec("")
    }

    override fun applyExoticToStats(
        id: String,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        exoticData: ExoticData
    ) {
        // TODO (the current code is just here to make it REALLY obvious for testing if the exotic is installed)
        // stats.armorDamageTakenMult.modifyMult(id, 0.01f)
        // stats.hullDamageTakenMult.modifyMult(id, 0.01f)
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