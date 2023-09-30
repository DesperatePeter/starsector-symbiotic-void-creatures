package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import org.dark.graphics.plugins.ShipDestructionEffects
import org.lazywizard.lazylib.ext.campaign.contains
import tecrys.svc.WHALE_REPUTATION_MIN
import tecrys.svc.internalWhaleReputation
import tecrys.svc.utils.removeDMods
import java.awt.Color


class StjarwhalController: BaseHullMod() {
    companion object{
        private const val ENGINE_DAMAGE_TAKEN = 0.5f
        private const val DMODS_ALLOWED_TAG = "allow_dmods"
    }

    override fun advanceInCombat(ship: ShipAPI, amount: Float) {
        val decos = ship.allWeapons
        for (deco in decos) {
            if (deco.slot.id == "whalefins") {
                if (ship.originalOwner == -1) {
                    deco.animation.frame = 0
                } else {
                    deco.animation.frame = 1
                }
            }
        }
    }
    override fun advanceInCampaign(member: FleetMemberAPI?, amount: Float) {

        Global.getSector() ?: return
        Global.getSector().playerFleet ?: return

        member?.id?.let {
            if(!Global.getSector().playerFleet.contains(it)) return
        } ?: return
        member.stats?.suppliesPerMonth?.modifyMult(this.javaClass.name, computeMaintenanceFactor())
    }

    private fun computeMaintenanceFactor(): Float{
        if(internalWhaleReputation > WHALE_REPUTATION_MIN) return 1f
        return 1f + (100f - internalWhaleReputation) / 100f
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return when(index){
            0 -> "${(computeMaintenanceFactor() * 100f).toInt()}%"
            1 -> "${internalWhaleReputation.toInt()}"
            else -> null
        }
    }
    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats?.run {
            zeroFluxSpeedBoost.modifyMult(id, 0f)
            dynamic.getStat(Stats.EXPLOSION_DAMAGE_MULT).modifyMult(id, 0f)
            dynamic.getStat(Stats.EXPLOSION_RADIUS_MULT).modifyMult(id, 0f)
            minCrewMod.modifyMult(id, 0f)
            maxCrewMod.modifyMult(id, 0f)
            engineDamageTakenMult.modifyMult(id, ENGINE_DAMAGE_TAKEN)
        }
        if(stats?.variant?.tags?.contains(DMODS_ALLOWED_TAG) == false){
            stats.variant?.removeDMods()
        }

    }
    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        ShipDestructionEffects.suppressEffects(ship, true, false)
        ship?.explosionFlashColorOverride = Color.BLUE
    }
}