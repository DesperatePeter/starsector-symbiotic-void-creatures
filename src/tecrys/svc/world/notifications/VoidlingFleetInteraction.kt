package tecrys.svc.world.notifications

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.OptionPanelAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import org.lazywizard.lazylib.ext.campaign.addShip
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY
import tecrys.svc.SVC_VARIANT_TAG
import tecrys.svc.utils.addLeaveOption

class VoidlingFleetInteraction(private val voidlings: CampaignFleetAPI): NotificationDialogBase(
    Global.getSettings().getString(SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY, "svc_fleet_interaction_text"),
    "Voidling Swarm",
    Global.getSettings().getSpriteName("backgrounds", "svc_encounter")
) {
    override fun addOptions(options: OptionPanelAPI) {
        options.run {
            addOption("Transfer ships", "Transfer")
            setTooltip("Transfer", "Select a Void Creature to transfer to your fleet." +
                    " This cannot be undone!")
            addLeaveOption()
        }
    }

    override fun optionSelected(optionText: String?, optionData: Any?) {

        when(optionData as? String){
            "Transfer" -> showTransferDisplay()
            else -> dialog?.dismiss()
        }
    }

    private fun showTransferDisplay(){
        dialog?.textPanel?.clear()
        dialog?.visualPanel?.fadeVisualOut()
        dialog?.showFleetMemberPickerDialog("Pick Voidlings to transfer",
            "Confirm",
            "Exit",
            5,
            6,
            100f,
            true,
            true,
            voidlings.membersWithFightersCopy.filter { !it.isFighterWing && it.hullSpec.hasTag(
                SVC_VARIANT_TAG) },
            object : FleetMemberPickerListener {
                override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                    selected?.forEach { member ->
                        Global.getSector().playerFleet.fleetData.addFleetMember(member)
                        voidlings.fleetData.removeFleetMember(member)
                    }
                    dialog?.dismiss()
                }
                override fun cancelledFleetMemberPicking() {
                    dialog?.dismiss()
                }
            })
    }
}