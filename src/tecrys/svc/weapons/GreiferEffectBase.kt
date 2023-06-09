package tecrys.svc.weapons

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.VectorUtils
import org.lwjgl.util.vector.Vector2f

abstract class GreiferEffectBase : BeamEffectPlugin {

    private val interval = IntervalUtil(0.1f, 0.2f)
    abstract fun shouldAffectFighters(): Boolean
    abstract fun shouldAffectShips(): Boolean

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
        if(!(b.brightness > 1f)) return
        if(target.velocity == null) return
        val dvVec = VectorUtils.getDirectionalVector(beamLoc, targetLoc)
        (target as? ShipAPI)?.let {
            if(it.isFighter && !shouldAffectFighters()) return
            if(!it.isFighter && !shouldAffectShips()) return
            if(it.phaseCloak?.isActive == true) return
            if(it == b.source) return // why would the ship target itself? Oo
            // val dv = max(4000f / (it.mass + 0.000001f), 0.01f)
            dvVec.scale(computeForceAgainstShip(it, source))
            Vector2f.add(it.velocity, dvVec, it.velocity)
            return
        }
        if(shouldAffectObjects()){
            dvVec.scale(computeForceAgainstObject(target))
            Vector2f.add(target.velocity, dvVec, target.velocity)
        }
    }
}