package tecrys.svc.rulecmd

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin
import com.fs.starfarer.api.util.Misc
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent

class SvcPoisonLurePlaced: BaseCommandPlugin() {
    override fun execute(
        p0: String?,
        dialog: InteractionDialogAPI?,
        p2: List<Misc.Token?>?,
        memoryMap: Map<String?, MemoryAPI?>?
    ): Boolean {
        dialog?.interactionTarget?.let { stableLocation ->
            SymbioticCrisisIntelEvent.get()?.solveViaPoison(stableLocation)
            return true
        }
        return false
    }
}