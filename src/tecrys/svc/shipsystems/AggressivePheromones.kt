package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.setAlpha
import tecrys.svc.CombatPlugin
import tecrys.svc.SVC_VARIANT_TAG
import java.awt.Color
import kotlin.math.min

class AggressivePheromones : BaseShipSystemScript() {

    companion object {
        private val auraLineColor = Color(150, 0, 200, 120)
        private const val SYSTEM_RANGE = 1200f

        // Pre-calculate squared range to avoid expensive Math.sqrt calls every frame
        private const val SYSTEM_RANGE_SQUARED = SYSTEM_RANGE * SYSTEM_RANGE

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

    private fun renderAura(entity: CombatEntityAPI, alphaMult: Float = 1f) {
        CombatPlugin.aurasToRenderOneFrame.add(
            CombatPlugin.Companion.AuraInfo(
                entity.location, SYSTEM_RANGE, auraLineColor.setAlpha((auraLineColor.alpha * alphaMult).toInt())
            )
        )
    }

    private fun applyBuffs(entity: CombatEntityAPI, id: String?, removeBuffs: Boolean = false) {
        val engine = Global.getCombatEngine() ?: return
        val ships = engine.ships
        val numShips = ships.size

        // Standard indexed for-loop prevents Iterator object allocation
        for (i in 0 until numShips) {
            val target = ships[i]

            // Fast fail: check references and booleans before doing any math
            if (target === entity || target.owner != entity.owner || !target.hullSpec.hasTag(SVC_VARIANT_TAG)) {
                continue
            }

            // Use MathUtils to check distance squared (avoids Math.sqrt)
            val distSq = MathUtils.getDistanceSquared(target.location, entity.location)
            val isInRange = distSq <= SYSTEM_RANGE_SQUARED

            val mutStats = target.mutableStats ?: continue

            // Unroll the loops and modify stats directly to prevent creating Lists every frame
            if (isInRange && !removeBuffs) {
                mutStats.ballisticRoFMult.modifyMult(id, ROF_BUFF)
                mutStats.energyRoFMult.modifyMult(id, ROF_BUFF)
                mutStats.missileRoFMult.modifyMult(id, ROF_BUFF)
                mutStats.fluxDissipation.modifyMult(id, ROF_BUFF)

                mutStats.maxSpeed.modifyMult(id, SPEED_BUFF)
                mutStats.maxTurnRate.modifyMult(id, SPEED_BUFF)
                mutStats.acceleration.modifyMult(id, SPEED_BUFF)
                mutStats.turnAcceleration.modifyMult(id, SPEED_BUFF)
                mutStats.deceleration.modifyMult(id, SPEED_BUFF)
            } else {
                mutStats.ballisticRoFMult.unmodifyMult(id)
                mutStats.energyRoFMult.unmodifyMult(id)
                mutStats.missileRoFMult.unmodifyMult(id)
                mutStats.fluxDissipation.unmodifyMult(id)

                mutStats.maxSpeed.unmodifyMult(id)
                mutStats.maxTurnRate.unmodifyMult(id)
                mutStats.acceleration.unmodifyMult(id)
                mutStats.turnAcceleration.unmodifyMult(id)
                mutStats.deceleration.unmodifyMult(id)
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