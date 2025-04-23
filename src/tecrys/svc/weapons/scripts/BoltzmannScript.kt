package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.DummyCombatEntity
import tecrys.svc.utils.randomlyVaried
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class BoltzmannScript(private val missile: MissileAPI): BaseEveryFrameCombatPlugin() {
    companion object{
        const val ARC_RANGE = 500f
        const val VISUAL_ARC_MIN_RANGE = 250f
        const val ARC_DMG = 100f
        const val ARC_EMP = 500f
        const val ARC_FREQUENCY_HZ = 5f
        const val ARC_THICKNESS = 5f
        const val MAX_CHARGES = 25
        val EMP_COLOR = Color(0, 255, 201, 150)
        const val EMP_SOUND_ID = "svc_emp"
        const val COLOR_VARIATION = 100f
        const val NUM_VISUAL_ARCS = 12
    }
    private val engine = Global.getCombatEngine()
    private var timer = 0f
    private var charges = MAX_CHARGES
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        removeIfExpired()
        val loc = missile.location ?: return
        engine ?: return
        if(engine.isPaused) return
        val targets = CombatUtils.getShipsWithinRange(loc, ARC_RANGE).filter {
            it.originalOwner != missile.owner && it.originalOwner != 100
        }.filterNotNull()
        if(targets.isEmpty()) return

        timer += amount
        if(timer < 1f / ARC_FREQUENCY_HZ) return
        timer = 0f

        spawnVisualEmpArcs()

        targets.forEach{ tgt ->
            engine.spawnEmpArc(missile.source, missile.location, missile, tgt, DamageType.ENERGY, ARC_DMG, ARC_EMP, ARC_RANGE + 200f, EMP_SOUND_ID, 0f,
                Color(0, 0, 0, 0), Color(0, 0, 0, 0))

        }

        charges--
    }

    private fun spawnVisualEmpArcs(){
        for (i in 0 until NUM_VISUAL_ARCS) {
            val angle = (i.toFloat() + Math.random()) * 2f * PI.toFloat() / NUM_VISUAL_ARCS.toFloat()
            val randomRange = MathUtils.getRandomNumberInRange(VISUAL_ARC_MIN_RANGE, ARC_RANGE)
            val loc = missile.location + Vector2f(randomRange * cos(angle).toFloat(), randomRange * sin(angle).toFloat())
            engine.spawnEmpArc(
                missile.source,
                missile.location,
                missile,
                DummyCombatEntity(loc, missile.owner),
                DamageType.ENERGY,
                ARC_DMG,
                ARC_EMP,
                ARC_RANGE,
                EMP_SOUND_ID,
                ARC_THICKNESS,
                EMP_COLOR.randomlyVaried(COLOR_VARIATION).brighter(),
                EMP_COLOR.randomlyVaried(COLOR_VARIATION)
            )
        }
    }

    private fun removeIfExpired(){
        if(missile.isExpired || missile.hitpoints <= 0f || missile.isFizzling || missile.didDamage() || charges <= 0){
            engine.removePlugin(this)
            if(!(missile.isExpired || missile.hitpoints <= 0f || missile.didDamage())){
                missile.explode()
                missile.fadeOutThenIn(0.5f)
            }
        }
    }
}