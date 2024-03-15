package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.fleet.FleetAPI
import org.lwjgl.input.Keyboard
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.WHALE_REPUTATION_MIN
import tecrys.svc.internalWhaleReputation
import tecrys.svc.world.fleets.FleetManager
import kotlin.math.truncate

class NeutralWhaleFleetInteraction(private val whales: SectorEntityToken): NotificationDialogBase(


    kotlin.run {
        var txt = Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_whales_only_encounter_text")
        txt += if (internalWhaleReputation > WHALE_REPUTATION_MIN) "\nThey appear to be friendly and trusting." else "\nThey appear to be scared."
        txt
    },
    "Whale Encounter",
    Global.getSettings().getSpriteName("backgrounds", "whale_encounter")
) {

    companion object{
        const val FEED_AMOUNT = 200f
    }

    override fun addOptions(options: OptionPanelAPI) {
        if (internalWhaleReputation > WHALE_REPUTATION_MIN){
            val playerCargo = Global.getSector().playerFleet?.cargo ?: return
            options.addOption("Feed the whales", "Feed")
            val canFeed = (playerCargo.supplies) > FEED_AMOUNT ||
                    (playerCargo.getCommodityQuantity("food")) > FEED_AMOUNT
            options.setEnabled("Feed", canFeed)
            options.setTooltip("Feed", "Feed whales with $FEED_AMOUNT food or supplies. Will use food if available.")
        }
        options.addOption("Slaughter the whales to harvest oil.", "Slaughter")
        options.addOption("Leave", "Leave")
        options.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, false)
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        when(optionData as? String){
            null -> return
            "Feed" -> feedWhales()
            "Slaughter" -> ProtectedWhalesDialog.slaughterWhales(whales as? CampaignFleetAPI)
            "Leave" -> {
                if (internalWhaleReputation < WHALE_REPUTATION_MIN){
                    internalWhaleReputation += 1f
                    Global.getSector().campaignUI?.addMessage("The whales seem to become a little more relaxed before disappearing into the void!")
                }
            }
        }
        (whales as? CampaignFleetAPI)?.despawn()
        FleetManager.whaleSpawnIntervalMultiplier += 1.0f
        dialog?.dismiss()
    }

    private fun feedWhales(){
        val playerCargo = Global.getSector().playerFleet?.cargo ?: return
        if(playerCargo.getCommodityQuantity("food") > FEED_AMOUNT){
            playerCargo.removeCommodity("food", FEED_AMOUNT)
        }else{
            playerCargo.removeSupplies(FEED_AMOUNT)
        }
        val whale = (whales as? CampaignFleetAPI)?.membersWithFightersCopy?.random()
        whale?.let { wh ->
            Global.getSector().playerFleet.fleetData.addFleetMember(wh)
            Global.getSector().campaignUI?.addMessage("${wh.shipName} joined your fleet!")
            internalWhaleReputation += 5f
        }
        Global.getSector().campaignUI?.addMessage("The whales disappear into the void, thankful for the food!")
    }
}