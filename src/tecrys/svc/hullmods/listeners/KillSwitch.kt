package tecrys.svc.hullmods.listeners

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI

class KillSwitch(private val ship: ShipAPI, private var timeout: Float): BaseEveryFrameCombatPlugin() {
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(Global.getCombatEngine().isPaused) return
        timeout -= amount
        if(timeout <= 0f){
            Global.getCombatEngine().applyDamage(ship, ship.location, 999999f, DamageType.ENERGY, 0f, true, false, ship, false)
            Global.getCombatEngine().removePlugin(this)
        }
    }
}