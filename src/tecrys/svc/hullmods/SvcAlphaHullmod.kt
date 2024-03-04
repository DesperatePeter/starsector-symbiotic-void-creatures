package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import tecrys.svc.SVC_ALPHA_HULLMOD_ID

class SvcAlphaHullmod: BaseHullMod() {
    companion object{
        private const val MORE_ALPHAS_HM_ID = "svc_more_alphas_hm"
        private const val CR_REDUCTION_NORMAL = -1f
        private const val CR_REDUCTION_PER_ALPHA_WITH_HM = -0.05f
        private fun countsAsAlpha(member: FleetMemberAPI): Boolean{
            return member.variant.hasHullMod(SVC_ALPHA_HULLMOD_ID) && !member.variant.sMods.contains(MORE_ALPHAS_HM_ID)
        }
    }
    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if(isInPlayerFleet(stats)){
            val numAlphas = (Global.getSector().playerFleet?.membersWithFightersCopy?.count {
                countsAsAlpha(it)
            } ?: 0)
            if(numAlphas >= 2){
                if(stats?.variant?.hasHullMod(MORE_ALPHAS_HM_ID) == true){
                    stats.maxCombatReadiness?.modifyFlat(SVC_ALPHA_HULLMOD_ID, numAlphas.toFloat() * CR_REDUCTION_PER_ALPHA_WITH_HM)
                }else{
                    stats?.maxCombatReadiness?.modifyFlat(SVC_ALPHA_HULLMOD_ID, CR_REDUCTION_NORMAL)
                }
            }
        }
    }
}