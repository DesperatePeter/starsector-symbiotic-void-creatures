package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent

class ReinitCrisis: BaseCommand {
    override fun runCommand(
        p0: String,
        p1: BaseCommand.CommandContext
    ): BaseCommand.CommandResult? {
        return if(SymbioticCrisisIntelEvent.reInit()) {
            BaseCommand.CommandResult.SUCCESS
        }else{
            BaseCommand.CommandResult.ERROR
        }
    }
}