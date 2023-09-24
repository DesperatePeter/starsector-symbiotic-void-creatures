package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f

abstract class GreiferEffectBase : BeamEffectPlugin {

    private val interval = IntervalUtil(0.1f, 0.2f)
    abstract fun shouldAffectFighters(): Boolean
    abstract fun shouldAffectShips(): Boolean

    open fun useRubberBandForce(): Boolean = true

    /**
     * if true, enemies ships will be pulled towards this ship.
     * if false, will pull this ship towards the enemy.
     */
    open fun pullEnemyShips(): Boolean = false

    private fun computeRubberBandAdjustmentFactor(sourceLoc: Vector2f, targetLoc: Vector2f, range: Float): Float{
        val distance = (targetLoc - sourceLoc).length()
        return 0.5f + 0.75f * (distance/range)
    }

    /**
     * @note Setting this to true can cause the game zo freeze for some reason, mainly against Dooms
     */
    abstract fun shouldAffectObjects(): Boolean
    abstract fun computeForceAgainstShip(target: ShipAPI, source: ShipAPI): Float
    open fun computeForceAgainstObject(entity: CombatEntityAPI): Float = 0f

    override fun advance(amount: Float, engine: CombatEngineAPI?, beam: BeamAPI?) {
        interval.advance(amount)
        val b = beam ?: return
        val source = b.source ?: return
        val beamLoc = b.source?.location ?: return
        if(!interval.intervalElapsed()) return
        val target = b.damageTarget ?: return
        val targetLoc = target.location ?: return
        if(b.brightness < 0.9f) return
        if(target.velocity == null) return
        val dvVec = VectorUtils.getDirectionalVector(beamLoc, targetLoc)
        (target as? ShipAPI)?.let { targetShip ->
            if(targetShip.isFighter && !shouldAffectFighters()) return
            if(!targetShip.isFighter && !shouldAffectShips()) return
            if(targetShip.phaseCloak?.isActive == true) return
            if(targetShip == b.source) return // why would the ship target itself? Oo
            // val dv = max(4000f / (it.mass + 0.000001f), 0.01f)
            dvVec.scale(computeForceAgainstShip(targetShip, source))
            if(useRubberBandForce()){
                dvVec.scale(computeRubberBandAdjustmentFactor(beamLoc, targetLoc, b.weapon.range))
            }
            if(pullEnemyShips()){
                dvVec.scale(-1f)
                Vector2f.add(targetShip.velocity, dvVec, targetShip.velocity)
            }else{
                Vector2f.add(source.velocity, dvVec, source.velocity)
            }
            return
        }
        if(shouldAffectObjects()){
            dvVec.scale(computeForceAgainstObject(target))
            Vector2f.add(target.velocity, dvVec, target.velocity)
        }
    }
}