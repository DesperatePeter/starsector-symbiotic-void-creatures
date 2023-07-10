package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.OptionPanelAPI
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.utils.CampaignSettingDelegate
// svc_magic_bounty_defeated_title
class DefeatedMagicBountyDialog: NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_magic_bounty_defeated_text"),
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_magic_bounty_defeated_title"),
    Global.getSettings().getSpriteName("backgrounds", "victory_notification_img"), 80f, 500f
) {
    companion object{
        var shouldSpawnVoidlings by CampaignSettingDelegate("$" + SVC_MOD_ID + "allowSpawningVoidlingFleets", true)
    }
    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            addOption("Stop Spawning", false)
            addOption("Continue Spawning", true)
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        when(optionData){
            is Boolean -> {
                shouldSpawnVoidlings = optionData
                dialog?.dismiss()
            }
        }
    }
}