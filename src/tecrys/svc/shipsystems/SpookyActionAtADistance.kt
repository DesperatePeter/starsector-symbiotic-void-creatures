package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import tecrys.svc.shipsystems.spooky.SpookyAllyImpl
import tecrys.svc.shipsystems.spooky.SpookyEnemyImpl
import tecrys.svc.shipsystems.spooky.SpookyPlaceholderImpl
import tecrys.svc.shipsystems.spooky.SpookyPlayerImpl
import tecrys.svc.utils.getEffectiveShipTarget

class SpookyActionAtADistance : BaseShipSystemScript() {

    companion object {
        const val SYSTEM_RANGE = 1500f
    }

    interface SpookyImpl {
        fun apply(
            stats: MutableShipStatsAPI?,
            id: String?,
            state: ShipSystemStatsScript.State?,
            effectLevel: Float
        )
        fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean
    }

    enum class SpookyMode {
        UNINITIALIZED, ENEMY, PLAYER, ALLY
    }

    private var mode = SpookyMode.UNINITIALIZED
    private var impl: SpookyImpl? = null
    private var shouldActivate = false

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val thisShip = stats?.entity as? ShipAPI ?: return
        when(state){
            ShipSystemStatsScript.State.IN -> {
                initMode(thisShip)
                shouldActivate = true
            }
            ShipSystemStatsScript.State.ACTIVE -> {
                if(shouldActivate){
                    shouldActivate = false
                    impl?.apply(stats, id, state, effectLevel)
                }
            }
            else -> return
        }
    }

    private fun initMode(ship: ShipAPI) {
        val newMode = when {
            ship.originalOwner == 1 -> SpookyMode.ENEMY
            ship == Global.getCombatEngine()?.playerShip -> SpookyMode.PLAYER
            else -> SpookyMode.ALLY
        }
        if (newMode == mode) return
        mode = newMode
        impl = when(mode){
            SpookyMode.ENEMY -> SpookyEnemyImpl(ship)
            SpookyMode.PLAYER -> SpookyPlayerImpl(ship)
            SpookyMode.ALLY -> SpookyAllyImpl(ship)
            else -> SpookyPlaceholderImpl("System has not been initialized properly. This is an internal error.")
        }
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        ship?.let { initMode(it) }
        return impl?.isUsable(system, ship) == true
    }

    override fun getInfoText(
        system: ShipSystemAPI?,
        ship: ShipAPI?
    ): String? {
        return when(system?.state){
            ShipSystemAPI.SystemState.IDLE -> if(ship?.getEffectiveShipTarget() == null) "waves unbound" else if(isUsable(system, ship)) "wave collapse" else "wave dispersion"
            ShipSystemAPI.SystemState.COOLDOWN -> "waves latent"
            null -> "null"
            else -> "gamma waves"
        }
    }
}