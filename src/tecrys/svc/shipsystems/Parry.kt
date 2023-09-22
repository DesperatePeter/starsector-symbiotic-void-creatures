package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import tecrys.svc.utils.degToRad
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Parry: BaseShipSystemScript() {
    companion object{
        const val RANGE = 150f
    }

    private val affectedProjectiles = mutableSetOf<DamagingProjectileAPI>()
    private var afterImageShown = false

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val ship = stats?.entity as? ShipAPI ?: return
        if(!afterImageShown){

            val sys = Global.getSettings().getShipSystemSpec("parry")
            val duration = sys?.active ?: 0.3f
            val buildup = sys?.`in` ?: 0.1f
            val down = sys?.out ?: 0.1f
            val col = Color(70, 10, 200, 200)
            for(i in 0..3){
                val angle = (ship.facing + 150f + Math.random().toFloat() * 60f) * degToRad
                val aaLoc = Vector2f(cos(angle) * 15f * i.toFloat(),  sin(angle) * 15f * i.toFloat())
                col.setAlpha(col.alpha - 40)
                ship.addAfterimage(col, aaLoc.x, aaLoc.y, ship.velocity.x * 0.05f * i.toFloat(), ship.velocity.y * 0.05f * i.toFloat(), 0.2f, buildup, duration, down, false, true, false)
            }

            afterImageShown = true
        }
        if(state != ShipSystemStatsScript.State.ACTIVE) return
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
            Global.getCombatEngine().addHitParticle(proj.location, Vector2f(), 20f, 1f, 0.8f, Color.WHITE)
            Global.getCombatEngine().addHitParticle(proj.location, Vector2f(), 30f, 0.6f, 1f, Color.WHITE)
            Global.getCombatEngine().addHitParticle(proj.location, Vector2f(), 50f, 0.2f, 1.2f, Color.WHITE)
            Global.getSoundPlayer().playSound("svc_system_parry", 1f, 1f, proj.location, Vector2f())
            affectedProjectiles.add(proj)
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        super.unapply(stats, id)
        affectedProjectiles.clear()
        afterImageShown = false
    }
}