package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.ext.combat.getNearbyAllies
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import tecrys.svc.CombatPlugin
import tecrys.svc.SVC_VARIANT_TAG
import java.awt.Color
import java.lang.ref.WeakReference
import kotlin.math.min

class AggressivePheromones : BaseShipSystemScript() {

    companion object{
        private val auraLineColor = Color(150, 0, 200, 120)
        private const val SYSTEM_RANGE = 1200f
        private const val SPEED_BUFF = 1.5f
        private const val ROF_BUFF = 1.5f
        private val params = listOf(SPEED_BUFF, ROF_BUFF)
        private val paramNames = listOf("Speed", "Rate of Fire")
    }

    private var rampUpAlphaMult = 0.2f
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val entity = stats?.entity ?: return
        when(state){
            ShipSystemStatsScript.State.IN -> applyBuffs(entity, id)
            ShipSystemStatsScript.State.ACTIVE -> {
                rampUpAlphaMult = min(rampUpAlphaMult + 0.005f, 1f)
                renderAura(entity, rampUpAlphaMult)
                applyBuffs(entity, id)
            }
            ShipSystemStatsScript.State.OUT -> {
                renderAura(entity, effectLevel)
                applyBuffs(entity, id)
            }
            else -> return
        }
    }

    private fun renderAura(entity: CombatEntityAPI, alphaMult: Float = 1f){
        CombatPlugin.aurasToRenderOneFrame.add(
            CombatPlugin.Companion.AuraInfo(
                entity.location, SYSTEM_RANGE, auraLineColor.setAlpha((auraLineColor.alpha * alphaMult).toInt())
        ))
    }

    private fun applyBuffs(entity: CombatEntityAPI, id: String?, removeBuffs: Boolean = false){
        val alliedSVCShips = Global.getCombatEngine().ships.filter { s ->
           s != entity && s.owner == entity.owner && s.hullSpec.hasTag(SVC_VARIANT_TAG)
        }

        alliedSVCShips.forEach {
            val isInRange = Misc.getDistance(it.location, entity.location) <= SYSTEM_RANGE

            it.mutableStats?.run {
                listOf(ballisticRoFMult, energyRoFMult, missileRoFMult, fluxDissipation).forEach { b ->
                    if (isInRange && !removeBuffs) {
                        b.modifyMult(id, ROF_BUFF)
                    }else{
                        b.unmodifyMult(id)
                    }
                }
                listOf(maxSpeed, maxTurnRate, acceleration, turnAcceleration, deceleration).forEach { b ->
                    if (isInRange && !removeBuffs) {
                        b.modifyMult(id, SPEED_BUFF)
                    }else{
                        b.unmodifyMult(id)
                    }
                }
            }
        }
    }



    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        rampUpAlphaMult = 0.2f
        applyBuffs(stats?.entity ?: return, id, true)
    }

    override fun getStatusData(
        index: Int,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ): ShipSystemStatsScript.StatusData? {
        if(paramNames.getOrNull(index) == null) return null
        return ShipSystemStatsScript.StatusData("Allied ${paramNames.getOrNull(index)}: ${params.getOrNull(index)}", false)
    }
}