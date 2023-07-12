package tecrys.svc.hullmods
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags

import org.dark.graphics.plugins.ShipDestructionEffects
import org.lazywizard.lazylib.ext.campaign.contains
import org.magiclib.util.MagicIncompatibleHullmods
import tecrys.svc.DMOD_TAG
import tecrys.svc.SVC_BASE_HULLMOD_ID
import java.awt.Color
class BGECarapace : BaseHullMod() {
    companion object{
        private const val ENGINE_DAMAGE_TAKEN = 0.5f
        private const val EMP_RESISTANCE = 50f
        private const val HULL_RESISTANCE = 30f
        private const val POWER_SCALING_MIN_HULL = 0.3f
        private const val POWER_SCALING_MAX_HULL = 0.9f
        private const val POWER_SCALING_MIN_POWER = 0.6f
        private const val CONTROL_COLLAR_ID = "svc_controlcollar"
        private const val CONTROL_COLLAR_HULLMOD_ID = "svc_controlcollar_hm"
        private const val POWER_SCALING_MULT_KEY = "SVC_CARAPACE_POWER_SCALING"
        private val ALLOWED_HULLMODS = setOf("BGECarapace", "enhanced_reflexes", "muscleflexibility", "muscleendurance",
            "acceleratedmetabolism", "thickenedshell", "antimatterfatlayer", "svc_alpha_voidling", "svc_controlcollar_hm",
            "do_not_back_off", "ML_incompatibleHullmodWarning", "neural_interface")
    }
    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats?.variant?.let {
            removeIncompatibleHullmods(it)
        }
        stats?.run {
            empDamageTakenMult.modifyMult(id, 1f + EMP_RESISTANCE * 0.01f)
            hullDamageTakenMult.modifyMult(id, 1f - HULL_RESISTANCE * 0.01f)
            combatWeaponRepairTimeMult.modifyMult(id,0.3f)
            hullCombatRepairRatePercentPerSecond.modifyFlat(id, 0.3f)
            maxCombatHullRepairFraction.modifyFlat(id, 1f)
            zeroFluxSpeedBoost.modifyMult(id, 0f)
            dynamic.getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 0f)
            dynamic.getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 0f)
            minCrewMod.modifyMult(id, 0f)
            maxCrewMod.modifyMult(id, 0f)
            engineDamageTakenMult.modifyMult(id, ENGINE_DAMAGE_TAKEN)
        }
    }
    override fun advanceInCampaign(member: FleetMemberAPI?, amount: Float) {
        member?.let { fm ->
            // removeIncompatibleHullmods(fm.variant)
            addControlCollarIfPlayer(fm)
        }
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return ship?.variant?.hullMods?.all { ALLOWED_HULLMODS.contains(it) } ?: false
    }

    private fun hideControlCollarIfNotPlayer(ship: ShipAPI){
        if(ship.originalOwner == 0 || ship.originalOwner == -1) return
        ship.allWeapons.filter {
                w -> w.isDecorative && w.slot.id == CONTROL_COLLAR_ID
        }.forEach {
                w -> w.sprite.color = Color(0, 0, 0, 0)
        }
    }
    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        modifyPowerLevel(ship)
        hideControlCollarIfNotPlayer(ship)
        ship.captain?.setPersonality("reckless")
    }
    private fun modifyPowerLevel(ship: ShipAPI) {
        ship.mutableStats?.run {
            val mult = getPowerLevelBasedOnHullLevel(ship.hullLevel)
            listOf(maxSpeed, damageToCapital, energyRoFMult, ballisticRoFMult, missileRoFMult).forEach {
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
        ship.addListener(ReduceExplosionListener())
        ship.setCustomData("AGC_ApplyCustomShipModes", listOf("Charge"))
        ship.setCustomData("AGC_ApplyCustomOptions", mapOf("svc_inksac" to listOf("BlockBeams", "PrioMissile")))
    }
    private fun removeIncompatibleHullmods(variant: ShipVariantAPI){
        val hullMods = variant.hullMods.toList()
        hullMods.filterNot {
            ALLOWED_HULLMODS.contains(it)
        }.forEach {
            MagicIncompatibleHullmods.removeHullmodWithWarning(variant, it, SVC_BASE_HULLMOD_ID)
        }
        hullMods.forEach {
            if(DModManager.getMod(it).hasTag(Tags.HULLMOD_DMOD)){
                DModManager.removeDMod(variant, it)
            }
        }
    }

    private fun addControlCollarIfPlayer(member: FleetMemberAPI){
        member.id?.let {
            if(!Global.getSector().playerFleet.contains(it)) return
        } ?: return

        val hullMods = member.variant.hullMods.toList()
        if(hullMods.contains(CONTROL_COLLAR_HULLMOD_ID)) return
        member.variant.addMod(CONTROL_COLLAR_HULLMOD_ID)
    }
    override fun getDescriptionParam(index: Int, hullSize: HullSize?): String? {
        return when(index){
            0 -> "${EMP_RESISTANCE.toInt()}%"
            1 -> "${HULL_RESISTANCE.toInt()}%"
            else -> null
        }
    }
}