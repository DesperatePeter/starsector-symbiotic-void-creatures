package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.combat.CombatUtils
import java.awt.Color

class BoltzmannScript(private val missile: MissileAPI): BaseEveryFrameCombatPlugin() {
    companion object{
        const val ARC_RANGE = 500f
        const val ARC_DMG = 30f
        const val ARC_EMP = 500f
        const val ARC_FREQUENCY_HZ = 1f
        const val ARC_THICKNESS = 15f
        val EMP_COLOR = Color(0, 255, 201, 150)
        const val EMP_SOUND_ID = "tachyon_lance_emp_impact"
    }
    private val engine = Global.getCombatEngine()
    private var timer = 0f
    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        removeScriptIfExpired()
        val loc = missile.location ?: return
        engine ?: return
        timer += amount
        if(timer < 1f / ARC_FREQUENCY_HZ) return
        CombatUtils.getShipsWithinRange(loc, ARC_RANGE).filter {
            it.originalOwner != missile.owner && it.originalOwner != 100
        }.randomOrNull()?.let { tgt ->
            engine.spawnEmpArc(missile.source, missile.location, missile, tgt, DamageType.ENERGY, ARC_DMG, ARC_EMP, ARC_RANGE, EMP_SOUND_ID, ARC_THICKNESS, EMP_COLOR.brighter(), EMP_COLOR)
        }
    }

    private fun removeScriptIfExpired(){
        if(missile.isExpired || missile.hitpoints <= 0f || missile.isFizzling || missile.didDamage()){
            engine.removePlugin(this)
        }
    }
}