package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.utils.LifeStealListener

class BerserkerFrenzy : BaseShipSystemScript() {

    companion object{
        private const val ROF_BUFF = 1.75f
        private const val LIFE_STEAL = 1.0f
        private const val MOVEMENT_BUFF = 1.1f
        private const val MANEUVER_BUFF = 1.25f
        private const val HULL_DAMAGE_TAKEN = 0.75f
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
        ship.mutableStats?.run {
            listOf(energyRoFMult, ballisticRoFMult, missileRoFMult).forEach {
                it.modifyMult(id, ROF_BUFF)
            }
            maxSpeed.modifyMult(id, MOVEMENT_BUFF)
            listOf(acceleration, maxTurnRate).forEach {
                it.modifyMult(id, MANEUVER_BUFF)
            }
            hullDamageTakenMult.modifyMult(id, HULL_DAMAGE_TAKEN)
        }
        ship.addListener(LifeStealListener(ship, LIFE_STEAL))
    }

    private fun removeBuffs(id: String?, ship: ShipAPI){
        ship.mutableStats?.run {
            listOf(energyRoFMult, ballisticRoFMult, missileRoFMult).forEach {
                it.unmodify(id)
            }
            maxSpeed.unmodify(id)
            listOf(acceleration, maxTurnRate).forEach {
                it.unmodify(id)
            }
            hullDamageTakenMult.unmodify(id)
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
}