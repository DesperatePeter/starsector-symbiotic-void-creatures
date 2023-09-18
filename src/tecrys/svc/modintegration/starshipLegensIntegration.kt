package tecrys.svc.modintegration

import com.fs.starfarer.api.Global
import tecrys.svc.BIOLOGICAL_HULL_TAGS
import tecrys.svc.SVC_VARIANT_TAG
import tecrys.svc.WHALE_VARIANT_TAG


fun flagHullsAsBiologicalForLegends(hulls: List<String>) {
    hulls.forEach {
        starship_legends.Integration.registerBiologicalShip(it)
    }
}

fun getAllBiologicalHullIds(): List<String> {
    return Global.getSettings().allShipHullSpecs.filter {hull ->
        BIOLOGICAL_HULL_TAGS.any { bio ->  hull?.tags?.contains(bio) == true }
    }.filterNotNull().map { it.hullId }
}