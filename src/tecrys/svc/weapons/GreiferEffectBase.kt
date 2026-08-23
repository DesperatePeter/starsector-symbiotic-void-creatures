package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

abstract class GreiferEffectBase : BeamEffectPlugin {

    private val interval = IntervalUtil(0.1f, 0.2f)
    // Cache a single vector to prevent per-interval object allocation
    private val scratchVec = Vector2f()

    abstract fun shouldAffectFighters(): Boolean
    abstract fun shouldAffectShips(): Boolean
    open fun useRubberBandForce(): Boolean = true
    open fun pullEnemyShips(): Boolean = false
    abstract fun shouldAffectObjects(): Boolean
    abstract fun computeForceAgainstShip(target: ShipAPI, source: ShipAPI): Float
    open fun computeForceAgainstObject(entity: CombatEntityAPI): Float = 0f

    private fun computeRubberBandAdjustmentFactor(sourceLoc: Vector2f, targetLoc: Vector2f, range: Float): Float {
        // Use MathUtils to avoid instantiating a new vector through subtraction
        val distance = MathUtils.getDistance(sourceLoc, targetLoc)
        return 0.5f + 1f * (distance / range)
    }

    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI?) {
        interval.advance(amount)
        if (!interval.intervalElapsed()) return

        val b = beam ?: return
        val source = b.source ?: return
        val beamLoc = b.source?.location ?: return
        val target = b.damageTarget ?: return
        val targetLoc = target.location ?: return

        if (b.brightness < 0.9f || target.velocity == null) return

        // Mutate our scratch vector instead of instantiating directional vectors
        Vector2f.sub(targetLoc, beamLoc, scratchVec)
        if (scratchVec.lengthSquared() > 0) scratchVec.normalise()

        val targetShip = target as? ShipAPI
        if (targetShip != null) {
            if (targetShip.isFighter && !shouldAffectFighters()) return
            if (!targetShip.isFighter && !shouldAffectShips()) return
            if (targetShip.phaseCloak?.isActive == true) return
            if (targetShip === source) return

            var force = computeForceAgainstShip(targetShip, source)
            if (useRubberBandForce()) {
                force *= computeRubberBandAdjustmentFactor(beamLoc, targetLoc, b.weapon.range)
            }

            if (pullEnemyShips()) {
                scratchVec.scale(-force)
                Vector2f.add(targetShip.velocity, scratchVec, targetShip.velocity)
            } else {
                scratchVec.scale(force)
                Vector2f.add(source.velocity, scratchVec, source.velocity)
            }
            return
        }

        if (shouldAffectObjects()) {
            scratchVec.scale(computeForceAgainstObject(target))
            Vector2f.add(target.velocity, scratchVec, target.velocity)
        }
    }
}