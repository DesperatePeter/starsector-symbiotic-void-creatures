package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BeamAPI
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.weapons.InkSprayEffect
import java.awt.Color

class InkSprayScript(
    private val ship: ShipAPI,
    engine: CombatEngineAPI,
    location: Vector2f,
    velocity: Vector2f,
    effectColor: Color,
    effectRadius: Float,
    duration: Float
) : CloudEffectScript(engine, location, velocity, effectColor, effectRadius, duration, 0f, false, 50f) {

    companion object {
        private const val EFFECT_ID = "InkDampening"
        private const val PROJECTILE_DAMAGE_MULT = 0.8f
        private const val BEAM_DAMAGE_MULT = 0.1f
        // Used to cull distance checks. Generous 2500 max range for standard beams.
        private const val MAX_BEAM_RANGE_SQUARED = 2500f * 2500f
    }

    private val radiusSq = InkSprayEffect.EFFECT_RADIUS * InkSprayEffect.EFFECT_RADIUS

    // Track the beams and their exact original colors to restore them cleanly
    private val dampenedBeams = mutableMapOf<BeamAPI, Color>()
    private val beamsInCloudThisFrame = mutableSetOf<BeamAPI>()

    override fun executeOnAdvance(amount: Float) {
        val eng = Global.getCombatEngine() ?: return
        disableMissiles(eng)
        weakenProjectiles(eng)
        dampenIntersectingBeams(eng)
    }

    override fun executeOnRemoval() {
        dampenedBeams.forEach { (b, originalColor) ->
            b.damage.modifier.unmodify(EFFECT_ID)
            b.coreColor = originalColor
        }
        dampenedBeams.clear()
    }

    private fun disableMissiles(eng: CombatEngineAPI) {
        val missiles = eng.missiles
        val size = missiles.size
        for (i in 0 until size) {
            val m = missiles[i]
            if (m.owner != ship.owner && m.isArmed && MathUtils.getDistanceSquared(m.location, location) <= radiusSq) {
                m.flameOut()
            }
        }
    }

    private fun weakenProjectiles(eng: CombatEngineAPI) {
        val projs = eng.projectiles
        val size = projs.size
        for (i in 0 until size) {
            val p = projs[i]
            if (p.owner != ship.owner && MathUtils.getDistanceSquared(p.location, location) <= radiusSq) {
                p.damage.modifier.modifyMult(EFFECT_ID, PROJECTILE_DAMAGE_MULT)
            }
        }
    }

    private fun dampenIntersectingBeams(eng: CombatEngineAPI) {
        beamsInCloudThisFrame.clear()

        val ships = eng.ships
        val numShips = ships.size

        for (i in 0 until numShips) {
            val s = ships[i]
            if (s.owner == ship.owner || s.isHulk) continue

            // Fast distance check to see if ship is remotely capable of firing a beam through the cloud
            if (MathUtils.getDistanceSquared(s.location, location) > MAX_BEAM_RANGE_SQUARED) continue

            val weapons = s.allWeapons ?: continue
            val numWeapons = weapons.size
            for (j in 0 until numWeapons) {
                val w = weapons[j] ?: continue
                if (w.isBeam && w.isFiring) {
                    val beams = w.beams ?: continue
                    val numBeams = beams.size
                    for (k in 0 until numBeams) {
                        val b = beams[k] ?: continue

                        // Line-circle intersection is extremely lightweight math
                        if (CollisionUtils.getCollides(b.from, b.to, location, InkSprayEffect.EFFECT_RADIUS)) {
                            beamsInCloudThisFrame.add(b)

                            // Apply the debuff and save original color if not already dampened
                            if (!dampenedBeams.containsKey(b)) {
                                dampenedBeams[b] = b.coreColor
                                b.damage.modifier.modifyMult(EFFECT_ID, BEAM_DAMAGE_MULT)
                                b.coreColor = Color(b.coreColor.red, b.coreColor.green, b.coreColor.blue, 50)
                            }
                        }
                    }
                }
            }
        }

        // Remove the effect from beams that stopped colliding with the cloud or stopped firing
        val iterator = dampenedBeams.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val b = entry.key
            if (!beamsInCloudThisFrame.contains(b)) {
                b.damage.modifier.unmodify(EFFECT_ID)
                b.coreColor = entry.value // Safely restores the exact original color
                iterator.remove() // Cleans up the reference naturally
            }
        }
    }
}