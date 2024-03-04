package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.BIOLOGICAL_HULL_TAGS

class VoidFins: BiologicalBaseHullmod() {
    companion object{
        const val MULT_SRC = "SVC_VOID_FINS"
    }
    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        stats?.fuelUseMod?.modifyMult(MULT_SRC, 0f)
    }
}