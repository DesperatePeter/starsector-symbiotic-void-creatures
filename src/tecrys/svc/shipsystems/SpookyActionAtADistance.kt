package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.spooky.SpookyEnemyImpl

class SpookyActionAtADistance : BaseShipSystemScript() {

    interface SpookyImpl {
        fun apply(
            stats: MutableShipStatsAPI?,
            id: String?,
            state: ShipSystemStatsScript.State?,
            effectLevel: Float
        )
    }

    enum class SpookyMode {
        UNINITIALIZED, ENEMY, PLAYER, ALLY
    }

    private var mode = SpookyMode.UNINITIALIZED
    private var impl: SpookyImpl? = null

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val thisShip = stats?.entity as? ShipAPI ?: return
        initMode(thisShip)
        impl?.apply(stats, id, state, effectLevel)
    }

    private fun initMode(ship: ShipAPI) {
        if(mode != SpookyMode.UNINITIALIZED) return
        mode = when {
            ship.originalOwner == 1 -> SpookyMode.ENEMY
            ship == Global.getCombatEngine()?.playerShip -> SpookyMode.PLAYER
            else -> SpookyMode.ALLY
        }
        when(mode){
            SpookyMode.ENEMY -> {
                impl = SpookyEnemyImpl()
            }
            else -> TODO("YOUCANNOTCONTROLME YOUSHOULDNOTHAVETHISSHIP")
        }
    }


}