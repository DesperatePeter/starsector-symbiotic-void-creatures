package tecrys.svc.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAIConfig
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import org.lazywizard.lazylib.ext.plus
import org.magiclib.kotlin.createDefaultShipAI
import tecrys.svc.hullmods.listeners.InfestationListener
import tecrys.svc.hullmods.listeners.KillSwitch
import tecrys.svc.utils.vectorFromAngleDeg
import java.awt.Color

class VoidlingInfestation: BaseHullMod() {
    companion object{
        const val TRIGGER_HULL_LEVEL = 0.5f
        const val FIGHTER_ID = "svc_mios_mandibles_single_wing"
        val NUMBER_OF_FIGHTERS = mapOf(
            ShipAPI.HullSize.FIGHTER to 1,
            ShipAPI.HullSize.FRIGATE to 2,
            ShipAPI.HullSize.DESTROYER to 4,
            ShipAPI.HullSize.CRUISER to 7,
            ShipAPI.HullSize.CAPITAL_SHIP to 11
        )
        val CLOUD_COLOR: Color = Color.RED
        const val CLOUD_RADIUS = 200f
        const val CLOUD_DURATION = 2f
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        ship?.run {
            addListener(InfestationListener(this))
        }
    }

    override fun getDescriptionParam(index: Int, hullSize: ShipAPI.HullSize?): String? {
        return NUMBER_OF_FIGHTERS.values.toList().getOrNull(index)?.toString()
    }
}