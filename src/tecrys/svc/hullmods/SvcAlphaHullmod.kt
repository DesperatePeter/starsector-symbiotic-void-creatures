package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.SVC_ALPHA_HULLMOD_ID

class SvcAlphaHullmod: BaseHullMod() {
    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        if(isInPlayerFleet(stats)){
            if((Global.getSector().playerFleet?.membersWithFightersCopy?.count {
                it.variant.hasHullMod(SVC_ALPHA_HULLMOD_ID)
                } ?: 0) >= 2){
                stats?.maxCombatReadiness?.modifyFlat(SVC_ALPHA_HULLMOD_ID, -1f)
            }
        }
    }
}