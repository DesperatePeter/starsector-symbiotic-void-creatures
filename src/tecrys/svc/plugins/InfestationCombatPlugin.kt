package tecrys.svc.plugins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import tecrys.svc.colonycrisis.SymbioticCrisisIntelEvent
import tecrys.svc.hullmods.listeners.InfestationListener

class InfestationCombatPlugin: BaseEveryFrameCombatPlugin(){
    companion object{
        val isActive get() = SymbioticCrisisIntelEvent.Companion.isInfestationActive && !Global.getCombatEngine().isSimulation
        const val ALREADY_HAS_LISTENER_MEM_KEY = "svc_crisis_infestation_already_listener"
    }
    private val interval = IntervalUtil(10f, 10f)

    override fun advance(
        amount: Float,
        events: List<InputEventAPI?>?
    ) {
        interval.advance(amount)
        if(interval.intervalElapsed()){
            if(!isActive){
                Global.getCombatEngine().removePlugin(this)
                return
            }
            Global.getCombatEngine().ships.filterNot {
                ship -> ship.customData.contains(ALREADY_HAS_LISTENER_MEM_KEY)
            }.filterNot {
                it.isFighter
            }.filterNotNull().forEach { ship ->
                ship.addListener(InfestationListener(ship))
                ship.setCustomData(ALREADY_HAS_LISTENER_MEM_KEY, true)
            }
        }
    }
}