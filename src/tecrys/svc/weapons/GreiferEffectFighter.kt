package tecrys.svc.weapons

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShipAPI

class GreiferEffectFighter : GreiferEffectBase() {
    override fun shouldAffectFighters(): Boolean = true

    override fun shouldAffectShips(): Boolean = true

    override fun shouldAffectObjects(): Boolean = false

    override fun computeForceAgainstShip(ship: ShipAPI): Float = 60f - ship.mass / 125f
    override fun computeForceAgainstObject(entity: CombatEntityAPI): Float = 60f - entity.mass / 125f
}