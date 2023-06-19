package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatNebulaAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

abstract class CloudEffectScript(
    protected val engine: CombatEngineAPI,
    protected var location: Vector2f,
    protected val velocity: Vector2f,
    protected val effectColor: Color,
    protected val initialEffectRadius: Float,
    private var duration: Float,
    private val effectRadiusGrowthPerSecond: Float = 0f,
    private val spawnMultipleNebulae: Boolean = false,
    private val colorVariation: Float = 0f
) : BaseEveryFrameCombatPlugin() {
    private var hasParticleBeenAdded = false
    protected var currentRadius = initialEffectRadius
    private val fullDuration = duration

    abstract fun executeOnRemoval()
    abstract fun executeOnAdvance(amount: Float)

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(Global.getCombatEngine().isPaused) return
        currentRadius += effectRadiusGrowthPerSecond * amount
        duration -= amount
        if (duration <= 0f) {
            executeOnRemoval()
            engine.removePlugin(this)
            return
        }
        // add visual effect
        if (!hasParticleBeenAdded) {
            fun d(c: Int) = MathUtils.clamp(c + ((Math.random() - 0.5) * colorVariation).toInt(), 0, 255)

            var tmpDuration = duration
            while (tmpDuration > duration / 2f){
                val color = Color( d(effectColor.red),  d(effectColor.green), d(effectColor.blue), effectColor.alpha)
                val endSizeMult = (initialEffectRadius + (effectRadiusGrowthPerSecond * tmpDuration)) / initialEffectRadius
                engine.addNebulaParticle(
                    location,
                    velocity,
                    2f * initialEffectRadius,
                    endSizeMult,
                    0.5f,
                    0.8f,
                    tmpDuration,
                    color
                )
                tmpDuration -= Math.random().toFloat() * (fullDuration * 0.2f)
                if(!spawnMultipleNebulae) tmpDuration = 0f
            }

            hasParticleBeenAdded = true
        }
        val dLoc = Vector2f(velocity.x, velocity.y) // make sure we actually copy
        dLoc.scale(amount)
        location += dLoc
        executeOnAdvance(amount)
    }
}