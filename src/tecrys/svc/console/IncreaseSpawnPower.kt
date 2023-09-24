package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import tecrys.svc.world.fleets.FleetSpawnParameterCalculator
import tecrys.svc.world.fleets.svcSettings

class IncreaseSpawnPower: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT
        try {
            val amount = args.toFloat()
            FleetSpawnParameterCalculator.extraSpawnPower += amount
            Console.showMessage("Voidling spawn power is now: ${FleetSpawnParameterCalculator(svcSettings).spawnPower}")
        }catch(e: NumberFormatException){
            return BaseCommand.CommandResult.BAD_SYNTAX
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}