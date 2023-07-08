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
import tecrys.svc.utils.getEffectiveShipTarget
import java.awt.Color

class ParasumbilicalCord: BaseShipSystemScript() {
    companion object{
        const val SYSTEM_SUSTAIN_RANGE = 700f
        const val SYSTEM_ACTIVATION_RANGE = SYSTEM_SUSTAIN_RANGE -100f
        const val INNER_BEAM_WIDTH = 30f
    }

    private val innerSprite = Global.getSettings().getSprite("beams", "svc_parasumbilical_beam")
    // private val outerSprite = Global.getSettings().getSprite("beams", "fakeBeamFringe")

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
            thisShip.getEffectiveShipTarget()?.let {
                applyEffect(thisShip, it)
            }
        }
        if(!isActiveTargetInSustainRange(thisShip)){
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
            innerSprite, alphaMult, INNER_BEAM_WIDTH, line.length(), VectorUtils.getAngle(origin, target), origin
        ))
    }

    private fun isActiveTargetInSustainRange(thisShip: ShipAPI): Boolean{
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
            val tgt = getEffectiveShipTarget() ?: return false
            return (location - tgt.location).length() <= SYSTEM_ACTIVATION_RANGE
        }
        return false
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String {
        return when(system?.state){
            ShipSystemAPI.SystemState.ACTIVE -> "Attached"
            ShipSystemAPI.SystemState.IDLE -> if(ship?.getEffectiveShipTarget() == null) "No target" else if(isUsable(system, ship)) "In Range" else "Out of Range"
            ShipSystemAPI.SystemState.COOLDOWN -> "Recharging"
            ShipSystemAPI.SystemState.IN -> "Attaching"
            ShipSystemAPI.SystemState.OUT -> "Fading"
            null -> "Null"
        }
    }
}