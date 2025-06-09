package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.shipsystems.SpookyActionAtADistance
import tecrys.svc.shipsystems.spooky.gui.SpookyEnemyGuiIntroStage
import tecrys.svc.shipsystems.spooky.gui.SpookyEnemyGuiNegotiationStage
import tecrys.svc.shipsystems.spooky.gui.SpookyEnemyGuiSabotageStage
import tecrys.svc.shipsystems.spooky.gui.SpookyGuiShower

class SpookyEnemyImpl: SpookyActionAtADistance.SpookyImpl {
    private var stage = 0
    private val guiShower = SpookyGuiShower()
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        if(state != ShipSystemStatsScript.State.ACTIVE) return
        if(Global.getSector().memoryWithoutUpdate.contains(SymbioticCrisisIntelEvent.MEM_KEY_DISABLE_TELEPATHY)){
            return
        }
        if(guiShower.isRunning) return
        when(stage){
            0 -> {
                guiShower.gui = SpookyEnemyGuiIntroStage(guiShower)
                guiShower.start()
                stage = 1
            }
            1 -> {
                guiShower.gui = SpookyEnemyGuiSabotageStage(guiShower, false)
                guiShower.start()
                stage = 2
            }
            2 -> {
                guiShower.gui = SpookyEnemyGuiNegotiationStage(guiShower)
                guiShower.start()
                stage = 3
            }
            3 -> {
                guiShower.gui = SpookyEnemyGuiSabotageStage(guiShower, true)
                guiShower.start()
            }
            else -> TODO("The Mastermind system reached a state that should not be reachable. This is a bug.")
        }
    }
}