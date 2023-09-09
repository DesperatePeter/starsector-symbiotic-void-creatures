package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.OptionPanelAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.WHALES_ENCOUNTER_MEM_KEY
import tecrys.svc.WHALES_ORIGINAL_STRENGTH_KEY
import tecrys.svc.world.fleets.FleetManager
import kotlin.math.truncate

class ProtectedWhalesDialog(private val whales: CampaignFleetAPI?): NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_whales_protected_text")
    + "\nYou managed to save ${whales?.fleetPoints ?: 0}/${whales?.customData?.get(WHALES_ORIGINAL_STRENGTH_KEY)} dp worth of whales.",
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_whales_title"),
    Global.getSettings().getSpriteName("backgrounds", "whale_encounter"),
) {
    companion object{
        const val SLAUGHTER_MONEY_PER_DP = 5000f
    }
    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            addOption("Leave", "Leave")
            setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, false)
            addOption("Accept whale friend", "Friend")
            addOption("Slaughter whales (currently: get money)", "Slaughter")
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        (optionData as? String)?.let {
            when(it){
                "Friend" -> {
                    val currentStrength = (whales?.fleetPoints?.toFloat() ?: 0f)
                    val originalStrength = (whales?.customData?.get(WHALES_ORIGINAL_STRENGTH_KEY) as? Int) ?: 0.01f
                    // get a bigger whale when you saved many
                    val relativeSaved = currentStrength / originalStrength.toFloat()
                    val whaleIndex = truncate((whales?.membersWithFightersCopy?.size ?: 0) * relativeSaved).toInt()
                    val whale = whales?.membersWithFightersCopy?.sortedBy { w -> w.deployCost }?.getOrNull(whaleIndex)
                    whale?.let {wh ->
                        // whales?.removeFleetMemberWithDestructionFlash(wh)
                        Global.getSector().playerFleet.fleetData.addFleetMember(wh)
                        Global.getSector().campaignUI?.addMessage("${wh.shipName} joined your fleet!")
                    }
                }
                "Slaughter" -> {
                    val money = (whales?.fleetData?.fleetPointsUsed ?: 0f) * SLAUGHTER_MONEY_PER_DP
                    Global.getSector().playerFleet.cargo.credits.add(money)
                    Global.getSector().campaignUI?.addMessage("Received $money credits! Everyone is sad!")
                }
                else -> {}
            }
        }
        Global.getSector().memory?.unset(WHALES_ENCOUNTER_MEM_KEY)
        whales?.despawn()
        FleetManager.whaleSpawnIntervalMultiplier += 1.0f
        dialog?.dismiss()
    }
}