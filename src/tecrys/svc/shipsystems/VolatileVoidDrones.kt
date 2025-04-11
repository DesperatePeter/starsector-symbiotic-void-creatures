package tecrys.svc.shipsystems

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.impl.combat.DroneStrikeStatsAIInfoProvider
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import org.lwjgl.util.vector.Vector2f
import tecrys.svc.shipsystems.utils.VolatileDroneHandler
import java.awt.Color

class VolatileVoidDrones : BaseShipSystemScript(), DroneStrikeStatsAIInfoProvider {

    companion object {
        const val FAKE_WEAPON_ID = "svc_volatile_drone"
        const val VOLATILE_WING_ID = "svc_pox_whip_wing" // This might not be needed anymore, depending on how you identify drones
        val EXPLOSION_FLASH_COLOR: Color = Color.RED
    }

    private var fakeWeapon: WeaponAPI? = null
    private val engine = Global.getCombatEngine()
    private var fighter: ShipAPI? = null // Changed from ship to fighter
    private var nextTarget: ShipAPI? = null
    private val initialTarget: ShipAPI?
        get() {
            return fighter?.shipTarget // Target of the fighter.
        }

    private fun getOrInitFakeWeapon(): WeaponAPI {
        if (fakeWeapon == null) fakeWeapon = engine.createFakeWeapon(fighter, FAKE_WEAPON_ID)
        return fakeWeapon!!
    }

    private fun init(fighter: ShipAPI?) { // Changed parameter name
        fighter?.let {
            this.fighter = it // Changed assignment
            getOrInitFakeWeapon()
        }
    }

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    ) {
        if (state != ShipSystemStatsScript.State.ACTIVE) return
        init(stats?.entity as? ShipAPI) // Pass the fighter
        fighter?.let { fighter ->
            getDrones(fighter).forEach { drone ->
                drone.explosionScale = 0.67f
                drone.explosionVelocityOverride = Vector2f()
                drone.explosionFlashColorOverride = EXPLOSION_FLASH_COLOR
            }
            initialTarget?.let { target ->
                fireDrone(target)
                if (getDrones(fighter).isEmpty()) {
                    fighter.system?.forceState(ShipSystemAPI.SystemState.OUT, 0.1f)
                }
            }
        }
    }

    private fun fireDrone(target: ShipAPI) {
        setForceNextTarget(null)
        getDrones(fighter).firstOrNull()?.let { drone ->
            val missile = engine.spawnProjectile(
                fighter,
                getOrInitFakeWeapon(),
                FAKE_WEAPON_ID,
                Vector2f(drone.location),
                drone.facing,
                drone.velocity
            ) as? MissileAPI ?: return
            (missile.ai as? GuidedMissileAI)?.target = target
            //  No need to remove from wing.  Fighters ARE the wing.
            //drone.wing?.removeMember(drone)
            //drone.wing = null
            drone.explosionFlashColorOverride = EXPLOSION_FLASH_COLOR
            engine.addLayeredRenderingPlugin(VolatileDroneHandler(drone, missile, engine))
        }
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String? {
        init(ship) // Pass ship, which is the fighter.
        if (system != null && ship != null) {
            if (system.state != ShipSystemAPI.SystemState.IDLE) return "AGITATING DRONES"
            if (getDrones(ship).isEmpty()) return "NO DRONES" // Changed for clarity
            if (initialTarget == null) return "NO TARGET"
            return "HATCHED & VOLATILE" // Changed for clarity
        }
        return null
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        init(ship) // Pass ship
        if (ship != null && system != null) {
            if (system.state != ShipSystemAPI.SystemState.IDLE) return false
            return getDrones(ship).isNotEmpty()
        }
        return false
    }

    // ### DroneStrikeStatsAIInfoProvider method implementations ###
    override fun getMaxRange(ship: ShipAPI?): Float {
        init(ship)
        return ship?.let {
            it.mutableStats?.systemRangeBonus?.computeEffective(getOrInitFakeWeapon().range)
        } ?: 0f
    }

    override fun dronesUsefulAsPD(): Boolean = true

    override fun droneStrikeUsefulVsFighters(): Boolean = false

    override fun getDrones(ship: ShipAPI?): MutableList<ShipAPI> {
        init(ship)
        //  Modified logic.  A fighter's drones are its wingmates.
        return ship?.let { fighter ->
            fighter.wingMembers.toMutableList() // Get the fighter's wing members.
        } ?: mutableListOf()
    }

    override fun getMaxDrones(): Int = 5 //  Max wing size.

    override fun setForceNextTarget(forceNextTarget: ShipAPI?) {
        nextTarget = forceNextTarget
    }

    override fun getMissileSpeed(): Float = getOrInitFakeWeapon().projectileSpeed
}