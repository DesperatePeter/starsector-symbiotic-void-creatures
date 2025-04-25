package tecrys.svc.console

import com.fs.starfarer.api.Global
import org.lazywizard.console.BaseCommand
import tecrys.svc.SVC_FLEET_DEFEATED_MEM_KEY
import tecrys.svc.defeatedHunterFleets
import tecrys.svc.internalWhaleReputation
import tecrys.svc.rulecmd.SvcShouldSpawnHunterBarEvent
import tecrys.svc.rulecmd.SvcShouldSpawnHuntersDefeatedBarEvent
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.FleetSpawnParameterCalculator
import tecrys.svc.world.fleets.hunterFleetsThatHaveBeenDefeated

class ResetCommand: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT
        hunterFleetsThatHaveBeenDefeated.clear()
        defeatedHunterFleets = 0
        internalWhaleReputation = 100f
        SvcShouldSpawnHuntersDefeatedBarEvent.hasAlreadyTriggered = false
        SvcShouldSpawnHunterBarEvent.hasAlreadyTriggered = false
        FleetManager.whaleSpawnIntervalMultiplier = 1f
        FleetSpawnParameterCalculator.extraSpawnPower = 0f
        Global.getSector().memory.unset(SVC_FLEET_DEFEATED_MEM_KEY)
        return BaseCommand.CommandResult.SUCCESS
    }
}