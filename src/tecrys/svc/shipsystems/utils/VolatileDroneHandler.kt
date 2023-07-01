package tecrys.svc.shipsystems.utils

import com.fs.starfarer.api.combat.*
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

// Note: The terminator script for some reason uses a rendering plugin to handle the drone,
//       so who am I to question Alex's wisdom? xD
class VolatileDroneHandler(private val drone: ShipAPI, private val missile: MissileAPI, private val engine: CombatEngineAPI):
    BaseCombatLayeredRenderingPlugin() {
    private var isDone = false
    override fun isExpired(): Boolean = isDone

    companion object{
        const val VELOCITY_RANDOMNESS = 50f
    }

    override fun advance(amount: Float) {
        super.advance(amount)
        if(isDone) return
        missile.eccmChanceOverride = 1f
        missile.owner = drone.originalOwner
        missile.velocity.set(randomlyOffsetVelocity(missile.velocity))
        drone.location.set(missile.location)
        drone.velocity.set(missile.velocity)
        drone.facing = missile.facing
        drone.collisionClass = CollisionClass.FIGHTER
        drone.engineController.fadeToOtherColor(this, Color(0, 0, 0, 0), Color(0,0,0,0), 1f, 1f)
        missile.spriteAlphaOverride = 0f

        val isDroneDestroyed = drone.isHulk || drone.hitpoints <= 0f
        if(isDroneDestroyed || missile.didDamage() || missile.isFizzling || missile.hitpoints <= 0f){
            drone.velocity.set(Vector2f(0f, 0f))
            missile.velocity.set(Vector2f(0f, 0f))
            if(!isDroneDestroyed){
                engine.applyDamage(drone, drone.location, 1000000f, DamageType.ENERGY, 0f, true, false, drone, false)
            }
            if(!missile.didDamage()){
                missile.explode()
            }
            missile.interruptContrail()
            engine.removeEntity(missile)
            engine.removeEntity(drone)
        }
    }

    private fun randomlyOffsetVelocity(vel: Vector2f): Vector2f{
        return Vector2f(vel.x + (Math.random().toFloat() - 0.5f)*2f* VELOCITY_RANDOMNESS,
            vel.y + (Math.random().toFloat() - 0.5f)*2f* VELOCITY_RANDOMNESS)
    }
}