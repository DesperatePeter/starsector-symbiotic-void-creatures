package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.SpookyActionAtADistance
import tecrys.svc.shipsystems.spooky.gui.SpookyEnemyGuiIntroStage
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
        if(guiShower.isRunning) return
        when(stage){
            0 -> {
                guiShower.gui = SpookyEnemyGuiIntroStage{guiShower.exit()}
                guiShower.start()
                // stage = 1
            }
            else -> TODO("Not yet implemented")
        }
    }
}