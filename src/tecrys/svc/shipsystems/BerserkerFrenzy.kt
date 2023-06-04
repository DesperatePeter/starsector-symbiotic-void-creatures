package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.utils.LifeStealListener

class BerserkerFrenzy : BaseShipSystemScript() {

    companion object{
        private const val ROF_BUFF = 1.25f
        private const val LIFE_STEAL = 0.2f
        private const val MOVEMENT_BUFF = 1.25f
        private const val MANEUVER_BUFF = 2.0f
        private const val HULL_DAMAGE_TAKEN = 0.9f
        private const val ENGINE_DAMAGE_TAKEN = 0.1f
        private const val WEAPON_DAMAGE_TAKEN = 0.5f
        private val params = listOf(ROF_BUFF, LIFE_STEAL, MOVEMENT_BUFF, MANEUVER_BUFF, HULL_DAMAGE_TAKEN)
        private val paramNames = listOf("Rate of fire", "life steal", "speed", "maneuverability", "hull damage taken")
    }
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val ship = stats?.entity as? ShipAPI ?: return
        when(state){
            ShipSystemStatsScript.State.IN -> applyBuffs(id, ship)
            ShipSystemStatsScript.State.OUT -> applyOverload(ship)
            else -> return
        }
    }

    private fun applyOverload(ship: ShipAPI){
        if(ship.fluxTracker.isOverloaded) return
        ship.fluxTracker.beginOverloadWithTotalBaseDuration(ship.system.chargeDownDur)
    }

    private fun applyBuffs(id: String?, ship: ShipAPI){
        ship.engineController?.shipEngines?.forEach {
            it.repair()
        }
        ship.allWeapons?.forEach {
            it.repair()
        }
        ship.mutableStats?.run {
            listOf(energyRoFMult, ballisticRoFMult, missileRoFMult).forEach {
                it.modifyMult(id, ROF_BUFF)
            }
            maxSpeed.modifyMult(id, MOVEMENT_BUFF)
            listOf(acceleration, maxTurnRate).forEach {
                it.modifyMult(id, MANEUVER_BUFF)
            }
            hullDamageTakenMult.modifyMult(id, HULL_DAMAGE_TAKEN)
            engineDamageTakenMult.modifyMult(id, ENGINE_DAMAGE_TAKEN)
            weaponDamageTakenMult.modifyMult(id, WEAPON_DAMAGE_TAKEN)
        }
        ship.addListener(LifeStealListener(ship, LIFE_STEAL))
    }

    private fun removeBuffs(id: String?, ship: ShipAPI){
        ship.mutableStats?.run {
            listOf(energyRoFMult, ballisticRoFMult, missileRoFMult, maxSpeed, acceleration,
                maxTurnRate, hullDamageTakenMult, engineDamageTakenMult, weaponDamageTakenMult).forEach {
                it.unmodify(id)
            }
        }
        ship.removeListenerOfClass(LifeStealListener::class.java)
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        val ship = stats?.entity as? ShipAPI ?: return
        removeBuffs(id, ship)
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float):
            ShipSystemStatsScript.StatusData {
        return ShipSystemStatsScript.StatusData("${paramNames.getOrNull(index)}: ${params.getOrNull(index)}", false)
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String = "Enter frenzy"
//        "The ${ship?.hullSpec?.hullName ?: "ship"} briefly enters a berserker rage, gaining improved stats and restoring" +
//                " hull points when dealing damage. Afterwards, the ship overloads for a brief moment."
}