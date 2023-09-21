package tecrys.svc.modintegration

import com.fs.starfarer.api.Global
import tecrys.svc.BIOLOGICAL_HULL_TAGS


fun flagHullsAsBiologicalForLegends(hulls: List<String>) {
    try {
        hulls.forEach {
            starship_legends.Integration.registerBiologicalShip(it)
        }
    }catch (e: NoSuchMethodError){
        Global.getSector().campaignUI?.addMessage("SVC: Incompatible version of Starship Legends! Please upgrade Starship Legends!")
    }
}

fun getAllBiologicalHullIds(): List<String> {
    return Global.getSettings().allShipHullSpecs.filter {hull ->
        BIOLOGICAL_HULL_TAGS.any { bio ->  hull?.tags?.contains(bio) == true }
    }.filterNotNull().map { it.hullId }
}