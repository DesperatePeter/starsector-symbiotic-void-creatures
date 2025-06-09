package tecrys.svc.shipsystems.spooky

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.SpookyActionAtADistance
import java.awt.Color

class SpookyPlaceholderImpl(private val reason: String): SpookyActionAtADistance.SpookyImpl {
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        Global.getCombatEngine()?.addFloatingText(stats?.entity?.location,
            "This is a placeholder implementation of the Spooky Action at a distance ship system: " +
                    reason,
            20f, Color.RED, stats?.entity, 1f, 5f)
    }

}