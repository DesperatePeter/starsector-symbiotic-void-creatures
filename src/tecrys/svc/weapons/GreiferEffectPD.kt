package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI
import kotlin.math.max

class GreiferEffectPD : GreiferEffectBase() {
    override fun shouldAffectFighters(): Boolean = true

    override fun shouldAffectShips(): Boolean = false

    /**
     * false, to prevent crashing the game against dooms
     */
    override fun shouldAffectObjects(): Boolean = false

    // Note: these values are taken from the original implementation.
    override fun computeForceAgainstShip(ship: ShipAPI): Float = max(4000f / (ship.mass + 0.000001f), 0.01f)
    override fun computeForceAgainstObject(entity: CombatEntityAPI): Float = max(5000f / (entity.mass + 0.000001f), 0.5f)
}