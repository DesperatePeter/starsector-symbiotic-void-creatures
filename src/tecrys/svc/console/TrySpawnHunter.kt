package tecrys.svc.console

import com.fs.starfarer.api.Global
import org.lazywizard.console.BaseCommand
import org.lazywizard.console.Console
import tecrys.svc.world.fleets.FleetManager
import tecrys.svc.world.fleets.FleetSpawnParameterCalculator
import tecrys.svc.world.fleets.hunterFleetsToSpawn
import tecrys.svc.world.fleets.svcSettings

class TrySpawnHunter: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT
        if(hunterFleetsToSpawn.isEmpty()){
            Console.showMessage("No more hunter fleets available to spawn.")
            return BaseCommand.CommandResult.ERROR
        }
        if(Global.getSector().playerFleet?.isInHyperspace != true){
            Console.showMessage("Can only spawn hunter fleets while in hyperspace.")
            return BaseCommand.CommandResult.ERROR
        }
        val currentSpawnPower = FleetSpawnParameterCalculator(svcSettings).spawnPower
        Console.showMessage("The following hunter fleets are currently spawnable:")
        hunterFleetsToSpawn.filter {
            it.value.minSpawnPower <= currentSpawnPower
        }.forEach {
            Console.showMessage(it.value.name)
        }
        Console.showMessage("The following hunter fleets require more spawn power (current spawn power = $currentSpawnPower):")
        hunterFleetsToSpawn.filter {
            it.value.minSpawnPower > currentSpawnPower
        }.forEach {
            Console.showMessage("${it.value.name}: Required spawn power = ${it.value.minSpawnPower}")
        }
        if(FleetManager.tryToSpawnHunterFleet()){
            Console.showMessage("Roll successful. Hunter fleet spawned! Expect company soon.")
            return BaseCommand.CommandResult.SUCCESS
        }
        Console.showMessage("Roll failed. Try again later. Make sure you are in hyperspace, far from the core worlds.")
        Console.showMessage("Current spawn chance: ${(FleetManager.currentSpawnChance() * 100f).toInt()}%.")
        return BaseCommand.CommandResult.SUCCESS
    }
}