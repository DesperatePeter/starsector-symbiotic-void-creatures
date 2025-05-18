package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent

class ReinitCrisis: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        SymbioticCrisisIntelEvent.reInit()
        return BaseCommand.CommandResult.SUCCESS
    }
}