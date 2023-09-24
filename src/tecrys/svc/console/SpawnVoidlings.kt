package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import tecrys.svc.world.fleets.FleetManager

class SpawnVoidlings: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT
        if(FleetManager.spawnSvcFleetNowAtPlayer()){
            Console.showMessage("Successfully spawned voidling fleet.")
            return BaseCommand.CommandResult.SUCCESS
        }
        Console.showMessage("Failed to spawn voidling fleet. Try again later.")
        return BaseCommand.CommandResult.ERROR
    }
}