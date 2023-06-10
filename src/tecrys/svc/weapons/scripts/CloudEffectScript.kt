package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

abstract class CloudEffectScript(
    protected val engine: CombatEngineAPI,
    protected var location: Vector2f,
    protected val velocity: Vector2f,
    protected val effectColor: Color,
    protected val effectRadius: Float,
    private var duration: Float
) : BaseEveryFrameCombatPlugin() {
    private var hasParticleBeenAdded = false

    abstract fun executeOnRemoval()
    abstract fun executeOnAdvance(amount: Float)

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        duration -= amount
        if (duration <= 0f) {
            executeOnRemoval()
            engine.removePlugin(this)
            return
        }
        // add visual effect
        if (!hasParticleBeenAdded) {
            engine.addNebulaParticle(
                location,
                velocity,
                2f * effectRadius,
                1f,
                1f,
                1f,
                duration,
                effectColor
            )
            hasParticleBeenAdded = true
        }
        val dLoc = Vector2f(velocity.x, velocity.y) // make sure we actually copy
        dLoc.scale(amount)
        location += dLoc
        executeOnAdvance(amount)

    }
}