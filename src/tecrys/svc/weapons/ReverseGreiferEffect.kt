package tecrys.svc.weapons

import com.fs.starfarer.api.combat.ShipAPI

class ReverseGreiferEffect: GreiferEffectBase() {
    companion object{
        const val FORCE = 35f
    }

    override fun shouldAffectFighters(): Boolean = true

    override fun shouldAffectShips(): Boolean = true

    override fun shouldAffectObjects(): Boolean = false

    override fun computeForceAgainstShip(target: ShipAPI, source: ShipAPI): Float = FORCE

    override fun pullEnemyShips(): Boolean = true

    override fun useRubberBandForce(): Boolean = true
}