package tecrys.svc.shipsystems

import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
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
        private const val SPEED_BUFF = 1.25f
        private const val ROF_BUFF = 1.25f
        private val params = listOf(SPEED_BUFF, ROF_BUFF)
        private val paramNames = listOf("Speed", "Rate of Fire")
    }

    private var affectedShips = listOf<WeakReference<ShipAPI>>()
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
                removeBuffsFromOutOfRangeShips(entity.location, id)
            }
            ShipSystemStatsScript.State.OUT -> {
                renderAura(entity, effectLevel)
                removeBuffsFromOutOfRangeShips(entity.location, id)
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

    private fun applyBuffs(entity: CombatEntityAPI, id: String?){
        affectedShips = entity.getNearbyAllies(SYSTEM_RANGE).filter { s ->
            s.variant.hasTag(SVC_VARIANT_TAG)
        }.map { WeakReference(it) }
        affectedShips.forEach {
            it.get()?.mutableStats?.run {
                listOf(ballisticRoFMult, energyRoFMult, missileRoFMult).forEach { b ->
                    b.modifyMult(id, ROF_BUFF)
                }
                listOf(maxSpeed, maxTurnRate, acceleration, turnAcceleration, deceleration).forEach { b ->
                    b.modifyMult(id, SPEED_BUFF)
                }
            }
        }
    }

    private fun removeBuffsFromOutOfRangeShips(loc: Vector2f, id: String?){
        val ships = affectedShips.map { it.get() }.filterNotNull().filter {
            (it.location - loc).length() > SYSTEM_RANGE
        }
        ships.forEach { removeBuffsFromShip(it, id) }
    }

    private fun removeBuffsFromShip(ship: ShipAPI, id: String?){
        ship.mutableStats?.run {
            listOf(ballisticRoFMult, energyRoFMult, missileRoFMult,
                maxSpeed, maxTurnRate, acceleration, turnAcceleration, deceleration).forEach { stat ->
                stat.unmodify(id)
            }
        }
        affectedShips = affectedShips.filterNot { it == ship}
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?) {
        rampUpAlphaMult = 0.2f
        affectedShips.forEach {
            it.get()?.let { sh ->
                removeBuffsFromShip(sh, id)
            }
        }
        affectedShips = listOf()
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