package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.loading.Description
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.utils.CampaignSettingDelegate
// svc_magic_bounty_defeated_title
class DefeatedMagicBountyDialog: NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_magic_bounty_defeated_text"),
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_magic_bounty_defeated_title"),
    Global.getSettings().getSpriteName("backgrounds", "victory_notification_img"),
) {
    companion object{
        var shouldSpawnVoidlings by CampaignSettingDelegate("$" + SVC_MOD_ID + "allowSpawningVoidlingFleets", true)
    }
    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            addOption("Toggle Spawning " + if (shouldSpawnVoidlings) "off" else "on", !shouldSpawnVoidlings)
            setTooltip(!shouldSpawnVoidlings, "If you leave this GUI now, voidlings will ${if (shouldSpawnVoidlings) "" else "not "} spawn.")

            // Options for blueprints
            addOption("Learn Ship Blueprint: Hemalisk", "svc_hemalisk")
            setTooltip("svc_hemalisk", getDescription("svc_hemalisk", Description.Type.SHIP))
            addOption("Learn Weapon Blueprint: Ink-Spitter", "svc_inksac")
            setTooltip("svc_inksac", getDescription("svc_inksac", Description.Type.WEAPON))
        }
    }

    private fun getDescription(id: String, type: Description.Type): String{
        val descriptionId = when(type){
            Description.Type.SHIP -> Global.getSettings().getHullSpec(id)?.descriptionId ?: return "No description available"
            Description.Type.WEAPON -> id
            else -> return "No description available"
        }
        return Global.getSettings().getDescription(descriptionId, type)?.text1 ?: return "No description available"
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        when(optionData){
            is Boolean -> {
                shouldSpawnVoidlings = optionData
                dialog?.optionPanel?.let {
                    it.clearOptions()
                    addOptions(it)
                }
            }
            is String -> {
                if(Global.getSettings().allShipHullSpecs?.any { it.hullId == optionData} == true){
                    Global.getSector().playerFaction.addKnownShip(optionData, true)
                }
                if(Global.getSettings().allWeaponSpecs?.any { it.weaponId == optionData } == true){
                    Global.getSector().playerFaction.addKnownWeapon(optionData, true)
                }
                dialog?.dismiss()
            }
        }
    }
}