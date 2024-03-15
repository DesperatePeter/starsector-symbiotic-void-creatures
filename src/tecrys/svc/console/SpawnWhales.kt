package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.FleetSpawnParameterCalculator
import tecrys.svc.world.fleets.svcSettings

class SpawnWhales: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT

        Console.showMessage("Rollin to spawn whale encounter. Spawn chance: ${FleetManager.currentSpawnChance()}")

        if(FleetManager.tryToSpawnWhales()){
            Console.showMessage("Successfully spawned wale encounter!")
        }else{
            Console.showMessage("Try again!")
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}