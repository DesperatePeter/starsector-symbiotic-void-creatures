package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class LinkedProjectilesScript(private val firstProj: DamagingProjectileAPI, private val secondProj: DamagingProjectileAPI): BaseEveryFrameCombatPlugin() {
    companion object{
        const val VEL_MOD_BY_DIST = 20f
        const val LINK_TARGET_RANGE = 50f
        const val LINK_MAX_RANGE = 280f
        const val AVG_ROTATIONAL_SPEED = 200f
        const val ROTATION_SPEED_RNG = 0.7f
        val CORE_LINK_COLOR: Color = Color.GREEN
        const val LINK_MAX_RANGE_SQ = LINK_MAX_RANGE * LINK_MAX_RANGE
    }

    private val engine = Global.getCombatEngine()
    private val sprite = Global.getSettings().getSprite("fx", "base_trail_zapWithCore")
    private val rotationSpeed = AVG_ROTATIONAL_SPEED * (1f - ROTATION_SPEED_RNG + 2f * ROTATION_SPEED_RNG * Math.random().toFloat())

    // Cache scratch vectors
    private val scratchDelta = Vector2f()
    private val rotV = Vector2f()

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(engine?.isPaused != false) return
        if(isDone()){
            engine.removePlugin(this)
            return
        }
        modifyProjectileVelocities(amount)
    }

    private fun modifyProjectileVelocities(amount: Float){
        Vector2f.sub(secondProj.location, firstProj.location, scratchDelta)
        val dist = scratchDelta.length()
        if(dist <= 1f) return

        // rubber band effect
        scratchDelta.normalise()
        scratchDelta.scale(amount * VEL_MOD_BY_DIST * (dist - LINK_TARGET_RANGE))
        Vector2f.add(firstProj.velocity, scratchDelta, firstProj.velocity)

        scratchDelta.scale(-1f)
        Vector2f.add(secondProj.velocity, scratchDelta, secondProj.velocity)

        // rotational effect
        Vector2f.sub(secondProj.location, firstProj.location, scratchDelta) // re-calc after modification
        rotV.set(scratchDelta.y, -scratchDelta.x)
        if (rotV.lengthSquared() > 0) rotV.normalise()
        rotV.scale(rotationSpeed * amount)

        Vector2f.add(firstProj.velocity, rotV, firstProj.velocity)
        rotV.scale(-1f)
        Vector2f.add(secondProj.velocity, rotV, secondProj.velocity)

        firstProj.facing = Misc.getAngleInDegrees(firstProj.velocity)
        secondProj.facing = Misc.getAngleInDegrees(secondProj.velocity)
    }

    private fun isDone(): Boolean {
        if (firstProj.isFading || firstProj.isExpired || secondProj.isFading || secondProj.isExpired) return true
        return MathUtils.getDistanceSquared(firstProj.location, secondProj.location) > LINK_MAX_RANGE_SQ
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {
        if(engine?.isPaused != false) return
        Vector2f.sub(secondProj.location, firstProj.location, scratchDelta)
        val dist = scratchDelta.length()
        scratchDelta.scale(0.5f)

        // Mutate for center
        Vector2f.add(firstProj.location, scratchDelta, scratchDelta)

        sprite?.run {
            alphaMult = CORE_LINK_COLOR.alpha.toFloat() / 100f
            color = CORE_LINK_COLOR
            setSize(19f, dist)
            setAdditiveBlend()
            angle = Misc.getAngleInDegrees(firstProj.location, secondProj.location) - 90f
            renderAtCenter(scratchDelta.x, scratchDelta.y)
        }
    }
}