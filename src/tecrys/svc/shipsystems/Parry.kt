package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.dark.shaders.distortion.DistortionShader
import org.dark.shaders.distortion.RippleDistortion
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import tecrys.svc.hullmods.ShellVulcanization
import tecrys.svc.utils.degToRad
import tecrys.svc.utils.toFloat
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class Parry: BaseShipSystemScript() {
    companion object{
        const val RANGE = 150f
        const val SYSTEM_ID = "parry"
        const val MAX_PARRYABLE_DMG_PER_OP = 200f
    }

    private val affectedProjectiles = mutableSetOf<DamagingProjectileAPI>()
    private var afterImageShown = false
    private var wasDurationExtended = false
    private var mustReactivate = false
    private var initialActivationTimestamp = 0f
    private var lastReactivation = 0f
    private var totalDuration = 0f
    private var activeDuration = 0f
    private val currentTime
        get() = Global.getCombatEngine().getTotalElapsedTime(false)

    private val durationSinceInitialActivation
        get() = currentTime - initialActivationTimestamp

    private val durationUntilWouldEnd = lastReactivation - currentTime + activeDuration

    private val remainingTotalDuration = totalDuration - durationSinceInitialActivation

    private var parryableDamage = 0f

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val ship = stats?.entity as? ShipAPI ?: return
        if(!afterImageShown){
            showAfterImage(ship)
            createDistortion(ship)
            afterImageShown = true
        }
        if(state == ShipSystemStatsScript.State.IN){
            initialActivationTimestamp = currentTime
            lastReactivation = currentTime
            parryableDamage = ship.variant.hullSpec.fleetPoints.toFloat() * MAX_PARRYABLE_DMG_PER_OP
            if(isImproved(ship)){
                parryableDamage *= ShellVulcanization.PARRYABLE_DAMAGE_MULT
            }
            return
        }
        if(state != ShipSystemStatsScript.State.ACTIVE) return
        val pc = ship.phaseCloak ?: return
        activeDuration = pc.chargeActiveDur
        totalDuration = activeDuration * ShellVulcanization.PARRY_DURATION_BUFF_MULT
        kotlin.run {
            if(!isImproved(ship)) return@run
            if(durationSinceInitialActivation >= totalDuration) {
                // abort
                pc.forceState(ShipSystemAPI.SystemState.COOLDOWN, 0f)
            }
            if(durationUntilWouldEnd + durationSinceInitialActivation >= totalDuration) return@run
            if(durationUntilWouldEnd > 0.1f) return@run
            // reactivate
            mustReactivate = true
            createDistortion(ship)
            pc.forceState(ShipSystemAPI.SystemState.COOLDOWN, 0f)
        }
        val projectiles = CombatUtils.getProjectilesWithinRange(ship.location, ship.collisionRadius + RANGE)
        val missiles = CombatUtils.getMissilesWithinRange(ship.location, ship.collisionRadius + RANGE).filter {
            !it.isGuided
        }
        (projectiles + missiles).filter {
            !affectedProjectiles.contains(it)
        }.filter {
            it.owner != 100 && it.owner != ship.owner
        }.forEach { proj ->
            val dmg = proj.damageAmount * if(proj.damageType == DamageType.FRAGMENTATION) 0.25f else 1f
            val deltaV = Vector2f(proj.velocity.x * -2f, proj.velocity.y * -2f)
            if(parryableDamage - dmg >= 0f){
                parryableDamage -= dmg
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
            }else{
                proj.velocity.set(0f, 0f)
            }
            Global.getCombatEngine().addHitParticle(proj.location, Vector2f(), 25f, 1f, 0.8f, Color.WHITE)
            Global.getCombatEngine().addHitParticle(proj.location, Vector2f(), 35f, 0.6f, 1f, Color.WHITE)
            Global.getCombatEngine().addHitParticle(proj.location, Vector2f(), 60f, 0.2f, 1.4f, Color.WHITE)
            Global.getSoundPlayer().playSound("svc_system_parry", 1f, 1f, proj.location, Vector2f())
            affectedProjectiles.add(proj)
        }
    }

    private fun showAfterImage(ship: ShipAPI){
        val sys = Global.getSettings().getShipSystemSpec(SYSTEM_ID)
        val duration = sys?.active ?: 0.3f
        val buildup = sys?.`in` ?: 0.1f
        val down = sys?.out ?: 0.1f
        var col = Color(70, 10, 200, 150)
        for(i in 0..3){
            val angle = (ship.facing + 150f + Math.random().toFloat() * 60f) * degToRad
            val aaLoc = Vector2f(cos(angle) * 15f * i.toFloat(),  sin(angle) * 15f * i.toFloat())
            col = col.setAlpha(col.alpha - 30)
            ship.addAfterimage(col, aaLoc.x, aaLoc.y, ship.velocity.x * 0.05f * i.toFloat(), ship.velocity.y * 0.05f * i.toFloat(), 0.2f, buildup, duration, down, false, true, true)
        }
    }

    private fun createDistortion(ship: ShipAPI){
        DistortionShader.addDistortion(RippleDistortion(ship.location, ship.velocity).apply {
              size = ship.shieldRadiusEvenIfNoShield * 1.5f
            //  intensity = ship.shieldRadiusEvenIfNoShield * 2f
            // arcAttenuationWidth = 450f
            fadeInSize(0.15f)
            fadeOutIntensity(0.7f)
        })
    }

    private fun isImproved(ship: ShipAPI): Boolean{
        return ship.variant?.hasHullMod(ShellVulcanization.HULLMOD_ID) == true
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        super.unapply(stats, id)
        if(mustReactivate){
            (stats?.entity as? ShipAPI)?.phaseCloak?.forceState(ShipSystemAPI.SystemState.ACTIVE, 0f)
            mustReactivate = false
            lastReactivation = currentTime
            return
        }
        affectedProjectiles.clear()
        afterImageShown = false
        wasDurationExtended = false
        mustReactivate = false
    }
}