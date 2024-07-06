package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.WHALE_OIL_ITEM_ID
import tecrys.svc.WHALE_REPUTATION_MIN
import tecrys.svc.internalWhaleReputation
import tecrys.svc.utils.addLeaveOption
import tecrys.svc.world.fleets.FleetManager

class NeutralWhaleFleetInteraction(private val whales: SectorEntityToken) : NotificationDialogBase(


    kotlin.run {
        var txt = Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_whales_only_encounter_text")
        txt += if (internalWhaleReputation > WHALE_REPUTATION_MIN) "\nThey appear to be friendly and trusting." else "\nThey appear to be scared."
        txt
    },
    "Whale Encounter",
    Global.getSettings().getSpriteName("backgrounds", "whale_encounter"),
    "svc_whale_theme"
) {

    companion object {
        const val FEED_AMOUNT = 200f
        const val REPUTATION_GAIN_FEED = 25f
        const val REPUTATION_GAIN_LEAVE = 1f
        // at MIN_REPUTATION_FOR_OIL_FROM_FEEDING, you gain between 0% and 0% of MAX_OIL_GAINED,
        // at OIL_GAIN_REPUTATION_CAP you gain between 10% and 100% of MAX_OIL_GAINED
        // so, average oil gained scales between 0% (at min reputation) and 55% of max oil gained
        const val MIN_REPUTATION_FOR_OIL_FROM_FEEDING = 100f
        const val OIL_GAIN_REPUTATION_CAP = 250f
        const val MAX_OIL_GAINED_FROM_FEEDING = 200f
        const val OIL_RANDOMNESS = 0.9f // how much the amount depends on RNG
    }

    override fun addOptions(options: OptionPanelAPI) {
        if (internalWhaleReputation > WHALE_REPUTATION_MIN) {
            Global.getSector().playerFleet?.cargo?.let { playerCargo ->
                options.addOption("Feed the whales", "Feed")

                val food = playerCargo.getCommodityQuantity("food")
                val canFeed = playerCargo.supplies > FEED_AMOUNT || food > FEED_AMOUNT
                options.setEnabled("Feed", canFeed)
                options.setTooltip(
                    "Feed", "Feed whales with $FEED_AMOUNT food or supplies." +
                            " Will use ${if (food > FEED_AMOUNT) "food" else "supplies."}"
                )
            }
        }
        options.addOption("Slaughter the whales to harvest oil.", "Slaughter")
        options.setTooltip("Slaughter", "Whale oil is valuable and can be used as fuel. If you encounter other whales they will know what you did.")
        options.addLeaveOption()
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {
        when (optionData as? String) {
            null -> return
            "Feed" -> feedWhales()
            "Slaughter" -> ProtectedWhalesDialog.slaughterWhales(whales as? CampaignFleetAPI)
            "Leave" -> {
                if (internalWhaleReputation < WHALE_REPUTATION_MIN) {
                    internalWhaleReputation += REPUTATION_GAIN_LEAVE
                    Global.getSector().campaignUI?.addMessage("The whales seem to become a little more relaxed before disappearing into the void!")
                }
            }
        }
        (whales as? CampaignFleetAPI)?.despawn()
        FleetManager.whaleSpawnIntervalMultiplier += 0.05f
        Global.getSoundPlayer().pauseCustomMusic()
        dialog?.dismiss()
    }

    private fun feedWhales() {
        val playerCargo = Global.getSector().playerFleet?.cargo ?: return
        if (playerCargo.getCommodityQuantity("food") > FEED_AMOUNT) {
            playerCargo.removeCommodity("food", FEED_AMOUNT)
            Global.getSector().campaignUI?.addMessage("Lost 200 food!")
        } else {
            playerCargo.removeSupplies(FEED_AMOUNT)
            Global.getSector().campaignUI?.addMessage("Lost 200 supplies!")
        }

        val oilRepFactor = ((Math.random().toFloat() * OIL_RANDOMNESS + (1.0f - OIL_RANDOMNESS)) * (internalWhaleReputation - MIN_REPUTATION_FOR_OIL_FROM_FEEDING) /
                (OIL_GAIN_REPUTATION_CAP - MIN_REPUTATION_FOR_OIL_FROM_FEEDING)).coerceIn(0f, 1f)
        val oilQuantity = (oilRepFactor * MAX_OIL_GAINED_FROM_FEEDING).toInt()
        if(oilQuantity > 0){
            val oilItem = SpecialItemData(WHALE_OIL_ITEM_ID, WHALE_OIL_ITEM_ID)
            playerCargo.addItems(CargoAPI.CargoItemType.SPECIAL, oilItem, oilQuantity.toFloat())
            Global.getSector().campaignUI?.addMessage("Thankful for the food, the whales leave you a present: " +
                    "Gained $oilQuantity Stjarwhale Oil")
        }

        internalWhaleReputation += REPUTATION_GAIN_FEED
        Global.getSector().campaignUI?.addMessage("The whales disappear into the void, thankful for the food!")
    }
}