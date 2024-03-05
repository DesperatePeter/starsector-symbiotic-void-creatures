package tecrys.svc.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import tecrys.svc.BIOLOGICAL_HULL_TAGS

open class BiologicalBaseHullmod: BaseHullMod() {
    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return ship?.hullSpec?.tags?.any { it in BIOLOGICAL_HULL_TAGS } == true
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be applied to biological ships (voidlings and whales)."
    }
    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return when(index){
            0 -> "100%"
            1 -> "5%"
            else -> null
        }
    }
}