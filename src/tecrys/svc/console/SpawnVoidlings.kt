package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import tecrys.svc.world.fleets.FleetManager

class SpawnVoidlings: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT
        if(FleetManager.spawnSvcFleetNowAtPlayer()){
            return BaseCommand.CommandResult.SUCCESS
        }
        return BaseCommand.CommandResult.ERROR
    }
}