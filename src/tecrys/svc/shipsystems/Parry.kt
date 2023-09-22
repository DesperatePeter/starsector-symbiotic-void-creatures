package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class Parry: BaseShipSystemScript() {
    companion object{
        const val RANGE = 150f
    }

    private val affectedProjectiles = mutableSetOf<DamagingProjectileAPI>()

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        if(state != ShipSystemStatsScript.State.ACTIVE) return
        val ship = stats?.entity as? ShipAPI ?: return
        ship.setJitter(id, Color.green, 0.1f, 20, 100f)
        val projectiles = CombatUtils.getProjectilesWithinRange(ship.location, ship.collisionRadius + RANGE)
        val missiles = CombatUtils.getMissilesWithinRange(ship.location, ship.collisionRadius + RANGE).filter {
            !it.isGuided
        }
        (projectiles + missiles).filter {
            !affectedProjectiles.contains(it)
        }.filter {
            it.owner != 100 && it.owner != ship.owner
        }.forEach { proj ->
            val deltaV = Vector2f(proj.velocity.x * -2f, proj.velocity.y * -2f)
            Vector2f.add(proj.velocity, deltaV, proj.velocity)
            proj.facing += 180f
            if(proj.facing > 360f) proj.facing -= 360f
            when(proj.collisionClass){
                 CollisionClass.PROJECTILE_FF -> proj.collisionClass =  CollisionClass.PROJECTILE_NO_FF
                 CollisionClass.MISSILE_FF -> proj.collisionClass =  CollisionClass.MISSILE_NO_FF
                 CollisionClass.HITS_SHIPS_ONLY_FF -> proj.collisionClass = CollisionClass.HITS_SHIPS_ONLY_NO_FF
                else -> {}
            }
            proj.owner = ship.owner
            proj.source = ship
            affectedProjectiles.add(proj)
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        super.unapply(stats, id)
        affectedProjectiles.clear()
    }
}