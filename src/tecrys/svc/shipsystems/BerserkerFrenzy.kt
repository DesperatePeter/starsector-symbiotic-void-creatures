package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import java.awt.Color
import tecrys.svc.shipsystems.utils.LifeStealListener

class BerserkerFrenzy : BaseShipSystemScript() {
    companion object {
        private const val ROF_BUFF = 1.25f
        private const val LIFE_STEAL = 0.1f
        private const val FLUX_BUFF = 2.0f
        private const val MOVEMENT_BUFF = 1.5f
        private const val MANEUVER_BUFF = 1.2f
        private const val HULL_DAMAGE_TAKEN = 0.9f
        private const val ENGINE_DAMAGE_TAKEN = 0.1f
        private val WEAPON_DAMAGE_TAKEN = 0.5f
        private val JITTER_COLOR_ACTIVE = Color(255, 50, 0, 80)
        private val JITTER_COLOR_INACTIVE = Color(50, 150, 0, 25)

        private val params = listOf(ROF_BUFF, LIFE_STEAL, MOVEMENT_BUFF, MANEUVER_BUFF, HULL_DAMAGE_TAKEN, ENGINE_DAMAGE_TAKEN, WEAPON_DAMAGE_TAKEN)
        private val paramNames = listOf("Rate of fire", "life steal", "speed", "maneuverability", "hull damage taken", "engine damage taken", "weapon damage taken")
    }

    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        val ship = stats?.entity as? ShipAPI ?: return
        when (state) {
            ShipSystemStatsScript.State.IN -> applyBuffs(id, ship)
            ShipSystemStatsScript.State.OUT -> {
                applyOverload(ship)
                jitterShip(ship, id, JITTER_COLOR_INACTIVE, 0.1f)
            }
            ShipSystemStatsScript.State.ACTIVE -> {
                jitterShip(ship, id, JITTER_COLOR_ACTIVE, 1.5f)
                ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
                ship.blockCommandForOneFrame(ShipCommand.DECELERATE)
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK)
            }
            else -> return
        }
    }

    private fun applyOverload(ship: ShipAPI) {
        if (!ship.fluxTracker.isOverloaded) {
            ship.fluxTracker.beginOverloadWithTotalBaseDuration(ship.system.chargeDownDur)
        }
    }

    private fun applyBuffs(id: String?, ship: ShipAPI) {
        val engines = ship.engineController?.shipEngines
        if (engines != null) {
            for (i in 0 until engines.size) engines[i].repair()
        }

        val weapons = ship.allWeapons
        if (weapons != null) {
            for (i in 0 until weapons.size) weapons[i].repair()
        }

        ship.mutableStats?.run {
            energyRoFMult.modifyMult(id, ROF_BUFF)
            ballisticRoFMult.modifyMult(id, ROF_BUFF)
            missileRoFMult.modifyMult(id, ROF_BUFF)
            maxSpeed.modifyMult(id, MOVEMENT_BUFF)
            acceleration.modifyMult(id, MANEUVER_BUFF)
            maxTurnRate.modifyMult(id, MANEUVER_BUFF)
            deceleration.modifyMult(id, MANEUVER_BUFF)
            turnAcceleration.modifyMult(id, MANEUVER_BUFF)
            hullDamageTakenMult.modifyMult(id, HULL_DAMAGE_TAKEN)
            engineDamageTakenMult.modifyMult(id, ENGINE_DAMAGE_TAKEN)
            weaponDamageTakenMult.modifyMult(id, WEAPON_DAMAGE_TAKEN)
            fluxDissipation.modifyMult(id, FLUX_BUFF)
            energyWeaponFluxCostMod.modifyMult(id, WEAPON_DAMAGE_TAKEN)
            ballisticWeaponFluxCostMod.modifyMult(id, WEAPON_DAMAGE_TAKEN)
        }

        // Prevent listener stacking!
        if (!ship.hasListenerOfClass(LifeStealListener::class.java)) {
            ship.addListener(LifeStealListener(ship, LIFE_STEAL))
        }
    }

    private fun jitterShip(ship: ShipAPI, id: String?, color: Color, intensity: Float) {
        ship.setJitter(id, color, intensity, 2, 10f)
        ship.setJitterUnder(id, color, intensity, 2, 10f)
    }

    private fun removeBuffs(id: String?, ship: ShipAPI) {
        ship.mutableStats?.run {
            energyRoFMult.unmodify(id)
            ballisticRoFMult.unmodify(id)
            missileRoFMult.unmodify(id)
            maxSpeed.unmodify(id)
            acceleration.unmodify(id)
            deceleration.unmodify(id)
            maxTurnRate.unmodify(id)
            turnAcceleration.unmodify(id)
            hullDamageTakenMult.unmodify(id)
            engineDamageTakenMult.unmodify(id)
            weaponDamageTakenMult.unmodify(id)
            fluxDissipation.unmodify(id)
        }
        ship.removeListenerOfClass(LifeStealListener::class.java)
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        val ship = stats?.entity as? ShipAPI ?: return
        stats.ballisticWeaponFluxCostMod.unmodify(id)
        stats.energyWeaponFluxCostMod.unmodify(id)
        removeBuffs(id, ship)
    }

    override fun getStatusData(index: Int, state: ShipSystemStatsScript.State?, effectLevel: Float): ShipSystemStatsScript.StatusData? {
        if (index >= paramNames.size) return null
        return ShipSystemStatsScript.StatusData("${paramNames[index]}: ${params[index]}", false)
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String = "Enter frenzy"
}