package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.shipsystems.Parry

class ShellVulcanization: BiologicalBaseHullmod() {

    companion object{
        const val PARRY_DURATION_BUFF_MULT = 2f
        const val ARMOR_DAMAGE_MULTIPLIER = 1.25f
        const val HULLMOD_ID = "svc_shell_vulcanization"
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats?.armorDamageTakenMult?.modifyMult(HULLMOD_ID, ARMOR_DAMAGE_MULTIPLIER)
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return super.isApplicableToShip(ship) && ship?.hullSpec?.shipDefenseId == Parry.SYSTEM_ID
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be applied to biological ships with the parry defense system."
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return when(index){
            0 -> "${(PARRY_DURATION_BUFF_MULT * 100f - 100f).toInt()}%" // increases parry duration by s%
            1 -> "${(ARMOR_DAMAGE_MULTIPLIER * 100f - 100f).toInt()}%" // increases armor damage taken by s%
            else -> null
        }
    }
}