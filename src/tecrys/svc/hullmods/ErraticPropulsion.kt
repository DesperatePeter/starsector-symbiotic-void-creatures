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
        val addedSpeedBuffByRoll = mapOf(
            0.2f to 0.25f, // when roll below 0.2f, add 0.25f
            0.1f to 0.15f, // when roll below 0.1f, add additional 0.15f for total of 0.4f
            0.05f to 0.35f,
            0.01f to 0.5f
        )
    }

    // In Starsector, hullmods are only instantiated once and each ship shares the same class instance
    private val timerByShip = mutableMapOf<String, IntervalUtil>()
    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)
        ship ?: return
        if(ship.engineController.isDisabled || !ship.isAlive) return
        if(!timerByShip.contains(ship.id)) timerByShip[ship.id] = IntervalUtil(INTERVAL_MIN, INTERVAL_MAX)
        val timer = timerByShip[ship.id] ?: return
        timer.advance(amount)
        if(timer.intervalElapsed()){
            val roll = Math.random()
            var speedBuff = 1f
            addedSpeedBuffByRoll.forEach {
                if(roll <= it.key) speedBuff += it.value
            }
            if(speedBuff >= 1.5f){
                val col = if(speedBuff >= 2f) Color.RED else Color.BLUE
                Global.getCombatEngine().addFloatingText(ship.location, "Burst of Speed!", 20f, col, ship, 1f, 3f)
            }
            ship.mutableStats?.run {
                listOf(maxSpeed, acceleration, turnAcceleration).forEach {
                    it?.modifyMult(BUFF_ID, speedBuff)
                }
            }
        }
    }
}