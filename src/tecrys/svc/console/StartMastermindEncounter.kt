package tecrys.svc.console

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import org.lazywizard.console.BaseCommand
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.dialog.MastermindInteractionDialog

class StartMastermindEncounter: BaseCommand {
    override fun runCommand(p0: String, p1: BaseCommand.CommandContext): BaseCommand.CommandResult {
        val mastermindFleet = ((Global.getSector().memoryWithoutUpdate[FleetManager.MASTERMIND_FLEET_MEM_KEY] as? CampaignFleetAPI) ?: FleetManager().spawnMastermindFleet()) ?: return BaseCommand.CommandResult.ERROR
        Global.getSector().memoryWithoutUpdate[FleetManager.MASTERMIND_FLEET_MEM_KEY] = mastermindFleet
        MastermindInteractionDialog.showDummy(mastermindFleet)
        return BaseCommand.CommandResult.SUCCESS
    }
}