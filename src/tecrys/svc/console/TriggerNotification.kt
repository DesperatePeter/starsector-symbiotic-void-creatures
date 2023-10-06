package tecrys.svc.console

import org.lazywizard.console.BaseCommand
import tecrys.svc.world.notifications.NotificationShower

class TriggerNotification: BaseCommand {
    override fun runCommand(args: String, ctx: BaseCommand.CommandContext): BaseCommand.CommandResult {
        if(ctx != BaseCommand.CommandContext.CAMPAIGN_MAP) return BaseCommand.CommandResult.WRONG_CONTEXT
        val notificationIds = args.split(" ")
        if (notificationIds.isEmpty()) return BaseCommand.CommandResult.BAD_SYNTAX
        notificationIds.forEach { id ->
            NotificationShower.showNotificationRepeatable(id)
        }
        return BaseCommand.CommandResult.SUCCESS
    }
}