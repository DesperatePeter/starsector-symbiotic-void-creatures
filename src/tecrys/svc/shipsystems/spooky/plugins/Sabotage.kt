package tecrys.svc.shipsystems.spooky.plugins

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import java.awt.Color

abstract class Sabotage(protected val targetShip: ShipAPI,
                        protected val intensity: Float = 1.0f, // scales the intensity of any effect, e.g. chance for weapons to get disabled
                        durationMultiplier: Float = 1.0f
): BaseEveryFrameCombatPlugin() {
    companion object{
        private fun floatyText(txt: String, targetShip: ShipAPI?){
            Global.getCombatEngine()?.addFloatingText(targetShip?.location, txt,
                28f, Color.RED, targetShip, 2f, 4.0f)
        }

        @JvmStatic
        public fun applyMindControl(targetShip: ShipAPI?, durationMultiplier: Float = 1.0f) {
            targetShip ?: return
            Global.getCombatEngine()?.addPlugin(SpookyMindControl(targetShip, durationMultiplier))
            floatyText("Insane Captain", targetShip)
        }

        @JvmStatic
        public fun applyWeaponSabotage(targetShip: ShipAPI?, intensity: Float = 0.4f) {
            targetShip ?: return
            Global.getCombatEngine()?.addPlugin(SabotageWeapons(targetShip, intensity))
            floatyText("Weapons Crew Compromised", targetShip)
        }

        @JvmStatic
        public fun applyDriveSabotage(targetShip: ShipAPI?, intensity: Float = 0.7f) {
            targetShip ?: return
            Global.getCombatEngine()?.addPlugin(SabotageDrive(targetShip, intensity))
            floatyText("Rogue Engineer", targetShip)
        }

        @JvmStatic
        public fun applyCrewSabotage(targetShip: ShipAPI?, intensity: Float = 1.0f){
            targetShip ?: return
            Global.getCombatEngine()?.addPlugin(SabotageCrew(targetShip, intensity))
            floatyText("Violent Mutiny", targetShip)
        }
        const val TICK_PERIOD = 0.25f
    }
    protected val initialDuration: Float = durationFor10DpShip * (20f / ((targetShip.fleetMember?.baseDeploymentCostSupplies ?: 10f) + 10f)) * durationMultiplier
    protected var durationRemaining = initialDuration
    protected val progress: Float
        get() = 1f - (durationRemaining/initialDuration)
    private var nextPeriodicTick = initialDuration - TICK_PERIOD
    private var wasFirstActivation = false
    override fun advance(
        amount: Float,
        events: List<InputEventAPI?>?
    ) {
        if(Global.getCombatEngine().isPaused) return
        durationRemaining -= amount
        if(!wasFirstActivation) {
            wasFirstActivation = true
            onStart()
        }
        if(durationRemaining <= nextPeriodicTick) {
            periodicTick()
            nextPeriodicTick -= TICK_PERIOD
        }
        if(durationRemaining <= 0f) {
            onFinish()
            Global.getCombatEngine().removePlugin(this)
        }
    }

    /**
     * twice as long for 0 DP ship
     * 33% longer for 5 DP ship
     * half as long for 30 DP ship
     * 1/3rd as long for 50 DP ship
     */
    abstract val durationFor10DpShip: Float
    abstract fun onStart()
    abstract fun onFinish()
    abstract fun periodicTick()
}