package tecrys.svc.weapons

import com.fs.starfarer.api.combat.ShipAPI
import kotlin.math.max

class GreiferEffect : GreiferEffectBase() {
    override fun shouldAffectFighters(): Boolean = true

    override fun shouldAffectShips(): Boolean = true

    override fun shouldAffectObjects(): Boolean = false

    override fun computeForceAgainstShip(target: ShipAPI, source: ShipAPI): Float = max(37f - source.mass / 25f , 0.01f)
}