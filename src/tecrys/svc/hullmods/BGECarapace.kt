package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.impl.campaign.ids.Stats
import org.dark.graphics.plugins.ShipDestructionEffects
import java.awt.Color

class BGECarapace : BaseHullMod() {

    companion object{
        private const val EMP_RESISTANCE = 50f
        private const val HULL_RESISTANCE = 30f
        private const val POWER_SCALING_MIN_HULL = 0.3f
        private const val POWER_SCALING_MAX_HULL = 0.9f
        private const val POWER_SCALING_MIN_POWER = 0.7f
        private const val POWER_SCALING_MULT_KEY = "SVC_CARAPACE_POWER_SCALING"
        private val BLOCKED_HULLMODS = setOf("turretgyros", "advancedoptics", "autorepair", "dedicated_targeting_core", "targetingunit", "augmentedengines",
            "blast_doors", "unstable_injector", "reinforcedhull", "heavyarmor", "fluxshunt", "auxiliarythrusters", "insulatedengine")
    }

    private val nearbyShips: MutableSet<ShipAPI> = HashSet()
    private var resisting = 0f

    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats?.run {
            empDamageTakenMult.modifyMult(id, 1f + EMP_RESISTANCE * 0.01f)
            hullDamageTakenMult.modifyMult(id, 1f - HULL_RESISTANCE * 0.01f)
            combatWeaponRepairTimeMult.modifyMult(id,0.3f)
            hullCombatRepairRatePercentPerSecond.modifyFlat(id, 2f)
            maxCombatHullRepairFraction.modifyFlat(id, 1f)
            zeroFluxSpeedBoost.modifyMult(id, 0f)
            dynamic.getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 0f)
            dynamic.getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 0f)
        }
    }

    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        modifyPowerLevel(ship)
    }

    private fun modifyPowerLevel(ship: ShipAPI) {
        ship.mutableStats?.run {
            val mult = getPowerLevelBasedOnHullLevel(ship.hullLevel)
            listOf(maxSpeed, fluxDissipation, damageToCapital, damageToCruisers, damageToDestroyers, damageToFrigates, damageToFighters, damageToMissiles).forEach {
                it.unmodify(POWER_SCALING_MULT_KEY)
                it.modifyMult(POWER_SCALING_MULT_KEY, mult)
            }
        }
    }

    private fun getPowerLevelBasedOnHullLevel(hull: Float): Float{
        return when{
            hull <= POWER_SCALING_MIN_HULL -> POWER_SCALING_MIN_POWER
            hull >= POWER_SCALING_MAX_HULL -> 1f
            else -> 1f - (POWER_SCALING_MAX_HULL - hull) / (POWER_SCALING_MAX_HULL - POWER_SCALING_MIN_HULL) * (1f - POWER_SCALING_MIN_POWER)
        }
        // e.g. with max = 0.6, min = 0.4, hull = 0.5, minPower = 0.5
        // 1 - (0.6 - 0.5) / (0.6 - 0.4) * (1 - 0.5) = 1 - 0.1 / 0.2 * 0.5 = 0.75
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {
        ShipDestructionEffects.suppressEffects(ship, true, false)
        ship.explosionFlashColorOverride = Color.RED
        BLOCKED_HULLMODS.forEach {
            if(ship.variant.hullMods.contains(it)) ship.variant.removeMod(it)
        }
        ship.addListener(ReduceExplosionListener())
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize?): String? {
        return when(index){
            0 -> "${EMP_RESISTANCE.toInt()}%"
            1 -> "${HULL_RESISTANCE.toInt()}%"
            else -> null
        }
    }
}