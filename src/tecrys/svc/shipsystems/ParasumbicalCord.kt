package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lazywizard.lazylib.CollisionUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.ext.minus
import org.magiclib.kotlin.setAlpha
import tecrys.svc.CombatPlugin
import tecrys.svc.shipsystems.utils.DamageSharingListener
import java.awt.Color

class ParasumbicalCord: BaseShipSystemScript() {
    companion object{
        const val SYSTEM_SUSTAIN_RANGE = 700f
        const val SYSTEM_ACTIVATION_RANGE = SYSTEM_SUSTAIN_RANGE -100f
        private val INNER_COLOR = Color.ORANGE
        private val OUTER_COLOR = Color.PINK
        const val INNER_BEAM_WIDTH = 10f
        const val OUTER_BEAM_WIDTH = 20f
    }

    private val innerSprite = Global.getSettings().getSprite("beams", "fakeBeamCore")
    private val outerSprite = Global.getSettings().getSprite("beams", "fakeBeamFringe")

    private var damageSharingListener: DamageSharingListener? = null
    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        val thisShip = stats?.entity as? ShipAPI ?: return
        when(state){
            ShipSystemStatsScript.State.ACTIVE -> {
                onActive(thisShip)
                renderCord(thisShip, 1f)
            }
            ShipSystemStatsScript.State.OUT -> {
                unApplyEffect(thisShip)
                renderCord(thisShip, effectLevel)
            }
            else -> {}
        }
    }

    private fun onActive(thisShip: ShipAPI){
        if(damageSharingListener == null){
            thisShip.shipTarget?.let {
                applyEffect(thisShip, it)
            }
        }
        if(!isActiveTargetInRange(thisShip)){
            unApplyEffect(thisShip)
            thisShip.system?.forceState(ShipSystemAPI.SystemState.OUT, 0.5f)
            return
        }

    }

    private fun renderCord(thisShip: ShipAPI, alphaMult: Float){
        val targetShip = damageSharingListener?.shipTarget ?: return
        val origin = CollisionUtils.getCollisionPoint(thisShip.location, targetShip.location, thisShip) ?: return
        val target = CollisionUtils.getCollisionPoint(thisShip.location, targetShip.location, targetShip) ?: return
        val line = target - origin
        CombatPlugin.spritesToRenderOneFrame.add(CombatPlugin.Companion.RenderableSprite(
            innerSprite, INNER_COLOR.setAlpha((INNER_COLOR.alpha * alphaMult).toInt()), INNER_BEAM_WIDTH, line.length(), VectorUtils.getAngle(origin, target), origin
        ))
        CombatPlugin.spritesToRenderOneFrame.add(CombatPlugin.Companion.RenderableSprite(
            outerSprite, OUTER_COLOR.setAlpha((OUTER_COLOR.alpha * alphaMult).toInt()), OUTER_BEAM_WIDTH, line.length(), VectorUtils.getAngle(origin, target), origin
        ))
    }

    private fun isActiveTargetInRange(thisShip: ShipAPI): Boolean{
        val tgt = damageSharingListener?.shipTarget?.location ?: return false
        return (thisShip.location - tgt).length() <= SYSTEM_SUSTAIN_RANGE
    }

    private fun applyEffect(thisShip: ShipAPI, target: ShipAPI){
        if(damageSharingListener != null) unApplyEffect(thisShip)
        damageSharingListener = DamageSharingListener(target)
        thisShip.addListener(damageSharingListener)
    }

    private fun unApplyEffect(thisShip: ShipAPI){
        damageSharingListener?.run {
            thisShip.removeListener(this)
        }
        damageSharingListener = null
    }


    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        ship?.run {
            val tgt = shipTarget ?: return false
            return (location - tgt.location).length() <= SYSTEM_ACTIVATION_RANGE
        }
        return false
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String {
        return when(system?.state){
            ShipSystemAPI.SystemState.ACTIVE -> "Attached"
            ShipSystemAPI.SystemState.IDLE -> if(ship?.shipTarget == null) "No target" else if(isUsable(system, ship)) "In Range" else "Out of Range"
            ShipSystemAPI.SystemState.COOLDOWN -> "Recharging"
            ShipSystemAPI.SystemState.IN -> "Attaching"
            ShipSystemAPI.SystemState.OUT -> "Fading"
            null -> "Null"
        }
    }
}