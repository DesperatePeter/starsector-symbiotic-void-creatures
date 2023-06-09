package tecrys.svc.weapons

import com.fs.starfarer.api.combat.ShipAPI
import kotlin.math.max

class GreiferEffect : GreiferEffectBase() {
    override fun shouldAffectFighters(): Boolean = true

    override fun shouldAffectShips(): Boolean = true

    override fun shouldAffectObjects(): Boolean = false

    override fun computeForceAgainstShip(ship: ShipAPI): Float = max(4000f / (ship.mass + 0.000001f), 0.01f)
}