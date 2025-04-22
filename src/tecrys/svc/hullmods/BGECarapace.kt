package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.campaign.fleet.FleetMember
import org.dark.graphics.plugins.ShipDestructionEffects
import org.lazywizard.lazylib.ext.campaign.contains
import org.magiclib.kotlin.createDefaultShipAI
import org.magiclib.util.MagicIncompatibleHullmods
import tecrys.svc.SVC_BASE_HULLMOD_ID
import tecrys.svc.hullmods.listeners.ReduceExplosionListener
import tecrys.svc.shipsystems.utils.VoidlingShroud
import tecrys.svc.utils.removeDMods
import java.awt.Color


class BGECarapace : BaseHullMod() {
    companion object{
        private const val ENGINE_DAMAGE_TAKEN = 0.2f
        private const val HULL_RESISTANCE = 30f
        private const val POWER_SCALING_MIN_HULL = 0.1f
        private const val POWER_SCALING_MAX_HULL = 0.5f
        private const val POWER_SCALING_MIN_POWER = 0.7f
        private const val CONTROL_COLLAR_ID = "svc_controlcollar"
        private const val CONTROL_COLLAR_HULLMOD_ID = "svc_controlcollar_hm"
        private const val POWER_SCALING_MULT_KEY = "SVC_CARAPACE_POWER_SCALING"
        private val ALLOWED_HULLMODS = setOf("BGECarapace", "svc_alpha_voidling", "svc_controlcollar_hm",
            "do_not_back_off", "ML_incompatibleHullmodWarning", "neural_interface", "svc_stjarwhal_hm",  "carrier_regroup",
            "never_detaches", "shared_flux_sink", "svc_scoliac_tail_turner", "always_detaches", "svc_acid_blood_hm", "svc_infestation_hm",
            "svc_no_fuel_hm", "svc_more_alphas_hm", "svc_overdamage_res_hm", "svc_shell_vulcanization", "svc_rdm_stats", "svc_swarmHM")
        private val ALLOWED_HULLMODS_BY_PREFIX = setOf("automated", "sun", "ehm","sms" )
        private var DestroAI: FleetMemberAPI? = null
    }

    private val erraticPropulsion = ErraticPropulsion()

    override fun applyEffectsBeforeShipCreation(hullSize: HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats?.variant?.let {
            removeIncompatibleHullmods(it)
        }
        stats?.run {
            hullDamageTakenMult.modifyMult(id, 1f - HULL_RESISTANCE * 0.01f)
            combatWeaponRepairTimeMult.modifyMult(id,0.3f)
            combatEngineRepairTimeMult.modifyMult(id,0.3f)
            hullCombatRepairRatePercentPerSecond.modifyFlat(id, 0.6f)
            maxCombatHullRepairFraction.modifyFlat(id, 1f)
            zeroFluxSpeedBoost.modifyMult(id, 0f)
//            dynamic.getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 0f)
//            dynamic.getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 0f)
            minCrewMod.modifyMult(id, 0f)
            maxCrewMod.modifyMult(id, 0f)
            engineDamageTakenMult.modifyMult(id, ENGINE_DAMAGE_TAKEN)
        }
    }
    override fun advanceInCampaign(member: FleetMemberAPI?, amount: Float) {
        member?.let { fm ->
            addControlCollarIfPlayer(fm)
        }
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return ship?.variant?.hullMods?.all {
            ALLOWED_HULLMODS_BY_PREFIX.any { prefix:String -> it.startsWith(prefix) }
                    || ALLOWED_HULLMODS.contains(it)
        } ?: false
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
        //                        WeaponGroupAPI Group = FIGHTER.getWeaponGroupFor(weapon);
        val player = Global.getCombatEngine().playerShip
        modifyPowerLevel(ship)
        hideControlCollarIfNotPlayer(ship)
        ship.setExplosionScale(0.001f)
        ship.setNoDamagedExplosions(true);
        ship.setShipCollisionSoundOverride("dweller_collision_ships");
        ship.setAsteroidCollisionSoundOverride("dweller_collision_asteroid_ship");
/*        ship.captain?.setPersonality("reckless")*/
        erraticPropulsion.advanceInCombat(ship, amount)
//        if ( ship.areSignificantEnemiesInRange() && (ship != player || !Global.getCombatEngine().isUIAutopilotOn()))
//        {
//            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS)
//            ship.blockCommandForOneFrame(ShipCommand.DECELERATE)
//        }
        if (ship.shipAI != null && ship.shipAI.config != null) {
            val config = ship.shipAI.config

                config.personalityOverride = Personalities.RECKLESS
                config.alwaysStrafeOffensively = true
                config.backingOffWhileNotVentingAllowed = false
                config.turnToFaceWithUndamagedArmor = false
                config.burnDriveIgnoreEnemies = true
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF)
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF_EVEN_WHILE_VENTING)
            if (ship.hullSize == HullSize.FRIGATE)
            {
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.HARASS_MOVE_IN)
            }

            if (ship.fleetMember == null)
    return

            if (ship.fleetMember.isPhaseShip) {
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.PHASE_BRAWLER_DUMPING_FLUX)
                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.ESCORT_OTHER_SHIP)
//                ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.STAY_PHASED)
            }

        }

    }
    private fun modifyPowerLevel(ship: ShipAPI) {

        ship.mutableStats?.run {
            val mult = getPowerLevelBasedOnHullLevel(ship.hullLevel)
            listOf(maxCombatHullRepairFraction, ballisticWeaponDamageMult, energyWeaponDamageMult, missileWeaponDamageMult).forEach {
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


//		stats.getHullDamageTakenMult().unmodify(id);
//		stats.getArmorDamageTakenMult().unmodify(id);
//		stats.getEmpDamageTakenMult().unmodify(id);
        if (ship.hullSpec.baseHullId.startsWith("svc_tox") || ship.hullSpec.baseHullId.startsWith("svc_mas")) {
            var shroud = VoidlingShroud.getShroudFor(ship)
            if (shroud == null) shroud = VoidlingShroud(ship)
        }

    }
    private fun removeIncompatibleHullmods(variant: ShipVariantAPI){
        variant.removeDMods()
        val hullMods = variant.hullMods.toList()
        hullMods.filterNot {
            ALLOWED_HULLMODS.contains(it)
                    || ALLOWED_HULLMODS_BY_PREFIX.any { prefix -> it.startsWith(prefix) }
        }.forEach {
            MagicIncompatibleHullmods.removeHullmodWithWarning(variant, it, SVC_BASE_HULLMOD_ID)
        }
    }

    private fun addControlCollarIfPlayer(member: FleetMemberAPI){

        Global.getSector() ?: return
        Global.getSector().playerFleet ?: return

        member.id?.let {
            if(!Global.getSector().playerFleet.contains(it)) return
        } ?: return

        val hullMods = member.variant.hullMods.toList()
        if(hullMods.contains(CONTROL_COLLAR_HULLMOD_ID)) return
        member.variant.addMod(CONTROL_COLLAR_HULLMOD_ID)
    }
    override fun getDescriptionParam(index: Int, hullSize: HullSize?): String? {
        return when(index){
            0 -> "Every Voidling is unique and thus their statistics differ positively or negatively by up to 15%."
            1 -> "Void Creatures cannot carry and do not need antimatter fuel to travel through Hyperspace."
            2 -> "${HULL_RESISTANCE.toInt()}%"
            3 -> "Neural Interface"
            else -> null
        }
    }
}