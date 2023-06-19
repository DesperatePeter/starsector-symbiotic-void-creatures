package tecrys.svc.weapons.scripts


import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI

import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.CollisionUtils

import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha

import tecrys.svc.weapons.InkSprayEffect
import java.awt.Color
import java.lang.ref.WeakReference

class InkSprayScript(

    private val ship: ShipAPI, engine: CombatEngineAPI, location: Vector2f, velocity: Vector2f, effectColor: Color,
    effectRadius: Float, duration: Float
) : CloudEffectScript(engine, location, velocity, effectColor, effectRadius, duration, 0f, false, 50f) {

    companion object {
        private const val EFFECT_ID = "InkDampening"
        private const val PROJECTILE_DAMAGE_MULT = 0.8f
        private const val BEAM_DAMAGE_MULT = 0.1f
    }
    private var affectedBeams: MutableMap<WeakReference<BeamAPI>, Boolean> = mutableMapOf()

    override fun executeOnAdvance(amount: Float) {
        disableMissiles()
        weakenProjectiles()
        dampenIntersectingBeams()
        unDampenBeams()
    }

    override fun executeOnRemoval() {
        unDampenBeams(false)
    }

    private fun disableMissiles() {
        CombatUtils.getMissilesWithinRange(location, InkSprayEffect.EFFECT_RADIUS).filterNotNull().filter {
            it.owner != ship.owner && it.isArmed
        }.forEach {
            it.flameOut()
        }
    }

    private fun weakenProjectiles() {
        CombatUtils.getProjectilesWithinRange(location, InkSprayEffect.EFFECT_RADIUS).filterNotNull().filter {
            it.owner != ship.owner
        }.forEach {
            it.damage.modifier.modifyMult(EFFECT_ID, PROJECTILE_DAMAGE_MULT)
        }
    }

    private fun dampenIntersectingBeams() {
        affectedBeams = affectedBeams.mapValues { false }.toMutableMap()
        CombatUtils.getShipsWithinRange(location, 2000f).filterNotNull().filter { it.owner != ship.owner }.map {
            it.allWeapons
        }.flatten().filter { it.isBeam && it.isFiring }.forEach {
            it.beams?.forEach { b ->
                if (CollisionUtils.getCollides(b.from, b.to, location, InkSprayEffect.EFFECT_RADIUS)) {
                    b.damage.modifier.modifyMult(EFFECT_ID, BEAM_DAMAGE_MULT)
                    b.coreColor = Color(b.coreColor.red, b.coreColor.green, b.coreColor.blue, 50)
                    affectedBeams = affectedBeams.filterKeys { reference -> reference.get() != b }.toMutableMap()
                    affectedBeams[WeakReference(b)] = true
                }
            }
        }
    }

    private fun unDampenBeams(onlyUnaffected: Boolean = true) {
        if (!onlyUnaffected) affectedBeams = affectedBeams.mapValues { false }.toMutableMap()
        val keysToRemove = mutableListOf<WeakReference<BeamAPI>>()
        affectedBeams.filterValues { !it }.keys.forEach {
            it.get()?.damage?.modifier?.unmodify(EFFECT_ID)
            it.get()?.run {
                coreColor = Color(coreColor.red, coreColor.green, coreColor.blue, 250)
            }
            keysToRemove.add(it)
        }
        keysToRemove.forEach {
            affectedBeams.remove(it)
        }
    }

}