package tecrys.svc.utils

import com.fs.starfarer.api.combat.BoundsAPI
import com.fs.starfarer.api.combat.CollisionClass
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.ShieldAPI
import org.lwjgl.util.vector.Vector2f

class DummyCombatEntity(private val location: Vector2f, private var owner: Int): CombatEntityAPI {
    private var facing = 0f
    override fun getLocation(): Vector2f = location

    override fun getVelocity(): Vector2f = Vector2f()

    override fun getFacing(): Float = 0f

    override fun setFacing(facing: Float) {
        this.facing = facing
    }

    override fun getAngularVelocity(): Float = 0f

    override fun setAngularVelocity(angVel: Float) {
    }

    override fun getOwner(): Int = owner

    override fun setOwner(owner: Int) {
        this.owner = owner
    }

    override fun getCollisionRadius(): Float = 100f

    override fun getCollisionClass(): CollisionClass = CollisionClass.NONE

    override fun setCollisionClass(collisionClass: CollisionClass?) {
    }

    override fun getMass(): Float = 1f

    override fun setMass(mass: Float) {
    }

    override fun getExactBounds(): BoundsAPI? = null

    override fun getShield(): ShieldAPI? = null

    override fun getHullLevel(): Float = 1f

    override fun getHitpoints(): Float  = 1f

    override fun getMaxHitpoints(): Float = 1f

    override fun setCollisionRadius(radius: Float) {
    }

    override fun getAI(): Any? = null

    override fun isExpired(): Boolean = false

    override fun setCustomData(key: String?, data: Any?) {
    }

    override fun removeCustomData(key: String?) {
    }

    override fun getCustomData(): MutableMap<String, Any>? = null

    override fun setHitpoints(hitpoints: Float) {
    }
}