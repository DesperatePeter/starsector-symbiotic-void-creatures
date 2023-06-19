package tecrys.svc.weapons

import com.fs.starfarer.api.combat.ShipAPI
import kotlin.math.max

class GreiferEffect : GreiferEffectBase() {
    override fun shouldAffectFighters(): Boolean = true

    override fun shouldAffectShips(): Boolean = true

    override fun shouldAffectObjects(): Boolean = false

    override fun computeForceAgainstShip(target: ShipAPI, source: ShipAPI): Float = max(48f - source.mass / 17f , 0.01f)
}