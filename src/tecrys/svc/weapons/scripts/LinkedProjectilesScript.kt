package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class LinkedProjectilesScript(private val firstProj: DamagingProjectileAPI, private val secondProj: DamagingProjectileAPI):
    BaseEveryFrameCombatPlugin() {
        companion object{
            const val VEL_MOD_BY_DIST = 1.8f // higher means a stronger rubber-band, i.e. faster movement
            const val LINK_TARGET_RANGE = 60f // the range around which the projectiles oscillate
            const val LINK_MAX_RANGE = 400f // range when link breaks
            const val AVG_ROTATIONAL_SPEED = 150f // how fast they rotate
            const val ROTATION_SPEED_RNG = 0.75f // value between 0.0f and 1.0f
            val CORE_LINK_COLOR: Color = Color.GREEN
        }

    private val engine = Global.getCombatEngine()
    private val sprite = Global.getSettings().getSprite("beams", "svc_parasumbilical_beam")
    private val fToS: Vector2f
        get() = secondProj.location - firstProj.location
    private val rotationSpeed = AVG_ROTATIONAL_SPEED * (1f - ROTATION_SPEED_RNG + 2f * ROTATION_SPEED_RNG * Math.random().toFloat())


    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(engine?.isPaused != false) return
        if(isDone()){
            engine.removePlugin(this)
            return
        }
        modifyProjectileVelocities(amount)
    }

    private fun modifyProjectileVelocities(amount: Float){
        val deltaV = fToS
        val dist = deltaV.length()
        if(dist <= 1f) return

        // rubber band effect
        deltaV.normalise()
        deltaV.scale(amount * VEL_MOD_BY_DIST * (dist - LINK_TARGET_RANGE))
        Vector2f.add(firstProj.velocity, deltaV, firstProj.velocity)
        deltaV.scale(-1f)
        Vector2f.add(secondProj.velocity, deltaV, secondProj.velocity)

        // rotational effect
        val rotV = Vector2f(fToS.y, -fToS.x)
        rotV.normalise()
        rotV.scale(rotationSpeed * amount)
        Vector2f.add(firstProj.velocity, rotV, firstProj.velocity)
        rotV.scale(-1f)
        Vector2f.add(secondProj.velocity, rotV, secondProj.velocity)

        // set correct facing based on velocity direction
        listOf(firstProj, secondProj).forEach {
            it.facing = Misc.getAngleInDegrees(it.velocity)
        }
    }

    private fun isDone(): Boolean =
        (firstProj.location - secondProj.location).length() > LINK_MAX_RANGE
                || listOf(firstProj, secondProj).any {
                    it.isFading || it.didDamage() || it.isExpired
        }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {
        if(engine?.isPaused != false) return
        val delta = (secondProj.location - firstProj.location)
        val dist = delta.length()
        delta.scale(0.5f)
        val center = firstProj.location + delta
        sprite?.run {
            alphaMult = CORE_LINK_COLOR.alpha.toFloat() / 255f
            color = CORE_LINK_COLOR
            setSize(5f, dist)
            setAdditiveBlend()
            angle = Misc.getAngleInDegrees(firstProj.location, secondProj.location) - 90f
            renderAtCenter(center.x, center.y)
        }
//        renderTimer.advance(engine.elapsedInLastFrame)
//        if(!renderTimer.intervalElapsed()) return
//        Global.getCombatEngine()?.run {
//            spawnEmpArc(null, firstProj.location, firstProj,
//                DummyCombatEntity(secondProj.location, secondProj.owner),
//                DamageType.ENERGY, 0f, 0f, LINK_MAX_RANGE, null, 1f,
//                FRINGE_LINK_COLOR, CORE_LINK_COLOR)
//        }
    }
}