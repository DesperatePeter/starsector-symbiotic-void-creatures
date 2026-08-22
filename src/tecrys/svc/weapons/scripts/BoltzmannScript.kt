package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.FastTrig
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.utils.DummyCombatEntity
import tecrys.svc.utils.randomlyVaried
import java.awt.Color
import kotlin.math.PI

class BoltzmannScript(private val missile: MissileAPI): BaseEveryFrameCombatPlugin() {
    companion object{
        const val ARC_RANGE = 500f
        const val ARC_RANGE_SQUARED = ARC_RANGE * ARC_RANGE
        const val VISUAL_ARC_MIN_RANGE = 250f
        const val ARC_DMG = 100f
        const val ARC_EMP = 500f
        const val ARC_FREQUENCY_HZ = 5f
        const val FIRE_INTERVAL = 1f / ARC_FREQUENCY_HZ
        const val ARC_THICKNESS = 5f
        const val MAX_CHARGES = 15
        val EMP_COLOR = Color(0, 255, 201, 150)
        val BLANK_COLOR = Color(0, 0, 0, 0)
        const val EMP_SOUND_ID = "svc_emp"
        const val COLOR_VARIATION = 100f
        const val NUM_VISUAL_ARCS = 6
    }

    private val engine = Global.getCombatEngine()
    private var timer = 0f
    private var charges = MAX_CHARGES

    // Cache a single dummy entity so we don't instantiate new ones every frame
    private val dummyTargetLoc = Vector2f(0f, 0f)
    private val dummyTarget = DummyCombatEntity(dummyTargetLoc, missile.owner)

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        if(engine?.isPaused == true) return

        removeIfExpired()
        if (charges <= 0 || !engine.isEntityInPlay(missile)) return
        val loc = missile.location ?: return

        // 1. Check timer FIRST to avoid scanning the grid every frame
        timer += amount
        if (timer < FIRE_INTERVAL) return

        // 2. Scan the grid manually to avoid Kotlin collection overhead
        var foundTarget = false
        val ships = engine.ships
        val numShips = ships.size

        for (i in 0 until numShips) {
            val tgt = ships[i]
            if (tgt.originalOwner == missile.owner || tgt.originalOwner == 100 || tgt.isHulk) continue

            // Fast distance check
            if (MathUtils.getDistanceSquared(loc, tgt.location) <= ARC_RANGE_SQUARED) {
                engine.spawnEmpArc(
                    missile.source,
                    loc,
                    missile,
                    tgt,
                    DamageType.ENERGY,
                    ARC_DMG,
                    ARC_EMP,
                    ARC_RANGE + 200f,
                    if (!foundTarget) EMP_SOUND_ID else null, // Only play the sound once per burst
                    0f,
                    BLANK_COLOR,
                    BLANK_COLOR
                )
                foundTarget = true
            }
        }

        if (!foundTarget) return // Keep timer maxed so it fires instantly when a ship enters range

        timer = 0f
        spawnVisualEmpArcs(loc)
        charges--
    }

    private fun spawnVisualEmpArcs(loc: Vector2f){
        for (i in 0 until NUM_VISUAL_ARCS) {
            val angle = (i.toFloat() + Math.random().toFloat()) * 2f * PI.toFloat() / NUM_VISUAL_ARCS.toFloat()
            val randomRange = MathUtils.getRandomNumberInRange(VISUAL_ARC_MIN_RANGE, ARC_RANGE)

// Update the existing vector's coordinates instead of instantiating a new one
            dummyTargetLoc.set(
                loc.x + (randomRange * FastTrig.cos(angle.toDouble())).toFloat(),
                loc.y + (randomRange * FastTrig.sin(angle.toDouble())).toFloat()
            )

            engine.spawnEmpArc(
                missile.source,
                loc,
                missile,
                dummyTarget,
                DamageType.ENERGY,
                0f,
                0f,
                ARC_RANGE,
                null,
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