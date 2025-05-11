package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.world.fleets.FleetManager

class SvcAttractorPlaced: BaseCommandPlugin() {
    override fun execute(
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        val system = Global.getSector().playerFleet.containingLocation
        FleetManager.swarmSystemViaAttractor(system)
        Global.getSector().campaignUI.addMessage("The genie is very much out of the bottle. While there are no effects immediately visible to regular sensor equipment, you are fairly certain that ${system.nameWithTypeShort} won't be much more than a voidling infested wasteland within a few cycles.")
        return true
    }
}