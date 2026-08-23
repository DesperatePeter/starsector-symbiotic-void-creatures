package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.IntervalUtil
import java.awt.Color

class ErraticPropulsion: BaseHullMod() {
    companion object{
        const val INTERVAL_MIN = 1.5f
        const val INTERVAL_MAX = 4.5f
        const val BUFF_ID = "svc_erratic_propulsion"
        const val EXCLUSION_TAG = "no_burst_of_speed"
        const val HULLMOD_STATE_KEY = "svc_erratic_timer"
        val addedSpeedBuffByRoll = mapOf(
            0.2f to 0.25f,
            0.1f to 0.15f,
            0.05f to 0.35f,
            0.01f to 0.5f
        )
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)
        val currentShip = ship ?: return
        if(currentShip.engineController?.isDisabled == true || !currentShip.isAlive) return
        if(currentShip.hullSpec.hasTag(EXCLUSION_TAG)) return

        var timer = currentShip.customData[HULLMOD_STATE_KEY] as? IntervalUtil
        if (timer == null) {
            timer = IntervalUtil(INTERVAL_MIN, INTERVAL_MAX)
            currentShip.setCustomData(HULLMOD_STATE_KEY, timer)
        }

        timer.advance(amount)
        if(timer.intervalElapsed()){
            val roll = Math.random()
            var speedBuff = 1f
            addedSpeedBuffByRoll.forEach {
                if(roll <= it.key) speedBuff += it.value
            }
            if(speedBuff >= 1.5f){
                val col = if(speedBuff >= 2f) Color.RED else Color.BLUE
                Global.getCombatEngine().addFloatingText(currentShip.location, "Burst of Speed!", 20f, col, currentShip, 1f, 3f)
            }
            currentShip.mutableStats?.run {
                maxSpeed.modifyMult(BUFF_ID, speedBuff)
                acceleration.modifyMult(BUFF_ID, speedBuff)
                turnAcceleration.modifyMult(BUFF_ID, speedBuff)
            }
        }
    }
}