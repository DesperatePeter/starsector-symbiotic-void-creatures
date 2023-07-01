package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.GuidedMissileAI
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipSystemAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.DroneStrikeStatsAIInfoProvider
import com.fs.starfarer.api.plugins.ShipSystemStatsScript

import org.lwjgl.util.vector.Vector2f
import tecrys.svc.shipsystems.utils.VolatileDroneHandler
import tecrys.svc.utils.getEffectiveShipTarget
import java.awt.Color

class VolatileVoidDrones: BaseShipSystemScript(), DroneStrikeStatsAIInfoProvider {

    companion object{
        const val FAKE_WEAPON_ID = "svc_volatile_drone"
        const val VOLATILE_WING_ID = "svc_volatile_drones" // FIXME!!
        val EXPLOSION_FLASH_COLOR: Color = Color.RED
    }

    private var fakeWeapon: WeaponAPI? = null
    private val engine = Global.getCombatEngine()
    private var ship: ShipAPI? = null
    private var nextTarget: ShipAPI? = null
    private val initialTarget: ShipAPI?
        get(){
            if(ship == engine.playerShip) return ship?.shipTarget
            return ship?.getEffectiveShipTarget(getMaxRange(ship))
        }

    private fun getOrInitFakeWeapon(): WeaponAPI{
        if(fakeWeapon == null) fakeWeapon = engine.createFakeWeapon(ship, FAKE_WEAPON_ID)
        return fakeWeapon!!
    }

    private fun init(ship: ShipAPI?){
        ship?.let {
            this.ship = it
            getOrInitFakeWeapon()
        }
    }

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        if(state == ShipSystemStatsScript.State.IDLE) return
        init(stats?.entity as? ShipAPI)
        ship?.let {sh ->
            getDrones(sh).forEach {
                it.explosionScale = 0.67f
                it.explosionVelocityOverride = Vector2f()
                it.explosionFlashColorOverride = EXPLOSION_FLASH_COLOR
            }
            initialTarget?.let {
                fireDrone(it)
                sh.system?.forceState(ShipSystemAPI.SystemState.OUT, 0f)
            }
        }
    }

    private fun fireDrone(target: ShipAPI){
        setForceNextTarget(null)
        getDrones(ship).firstOrNull()?.let { drone ->
            val missile = engine.spawnProjectile(ship, getOrInitFakeWeapon(), FAKE_WEAPON_ID,
                Vector2f(drone.location), drone.facing, drone.velocity
            ) as? MissileAPI ?: return
            (missile.ai as? GuidedMissileAI)?.target = target
            drone.wing?.removeMember(drone)
            drone.wing = null
            drone.explosionFlashColorOverride = EXPLOSION_FLASH_COLOR
            engine.addLayeredRenderingPlugin(VolatileDroneHandler(drone, missile, engine))
        }
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String? {
        init(ship)
        if(system != null && ship != null){
            if(system.state != ShipSystemAPI.SystemState.IDLE) return "AGITATING DRONES"
            if(getDrones(ship).isEmpty()) return "BREEDING DRONES"
            if(initialTarget == null) return "NO TARGET"
            return "HATCHED & VOLATILE"
        }
        return null
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        init(ship)
        if(ship != null && system != null){
            if(system.state != ShipSystemAPI.SystemState.IDLE) return false
            return getDrones(ship).isNotEmpty()
        }
        return false
    }

    // ### DroneStrikeStatsAIInfoProvider method implementations ###
    override fun getMaxRange(ship: ShipAPI?): Float {
        init(ship)
        return ship?.let{
            it.mutableStats?.systemRangeBonus?.computeEffective(getOrInitFakeWeapon().range)
        } ?: 0f
    }

    override fun dronesUsefulAsPD(): Boolean = true // FIXME?

    override fun droneStrikeUsefulVsFighters(): Boolean = false //FIXME?

    override fun getDrones(ship: ShipAPI?): MutableList<ShipAPI> {
        init(ship)
        return ship?.run {
            allWings?.asSequence()?.filter { it.wingId == VOLATILE_WING_ID }?.mapNotNull {
                it.wingMembers
            }?.flatten()?.filterNotNull()?.toMutableList()
        } ?: mutableListOf()
    }

    override fun getMaxDrones(): Int = 8 //FIXME?

    override fun setForceNextTarget(forceNextTarget: ShipAPI?) {
        nextTarget = forceNextTarget
    }

    override fun getMissileSpeed(): Float = getOrInitFakeWeapon().projectileSpeed
}