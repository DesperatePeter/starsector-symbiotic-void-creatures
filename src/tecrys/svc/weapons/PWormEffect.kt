package tecrys.svc.weapons

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.CombatEntityAPI
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.OnHitEffectPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import org.lwjgl.util.vector.Vector2f
import kotlin.math.max

class PWormEffect: OnHitEffectPlugin {
    companion object{
        const val CREW_KILLED = 10f
    }
    override fun onHit(
        projectile: DamagingProjectileAPI?,
        target: CombatEntityAPI?,
        point: Vector2f?,
        shieldHit: Boolean,
        damageResult: ApplyDamageResultAPI?,
        engine: CombatEngineAPI?
    ) {
        if(shieldHit) return
        val ship = target as? ShipAPI ?: return
        val crew = ship.hullSpec.minCrew
        if (crew <= 0.01f) return
        var marinesOnShip = 0f
        if(ship.originalOwner == 0){
            val totalDeployedCrew = Global.getCombatEngine().getFleetManager(0).deployedCopy.map { it.hullSpec.minCrew }.sum()
            marinesOnShip = Global.getSector().playerFleet.cargo.marines.toFloat() * (crew / totalDeployedCrew)
        }
        val crewDamage = CREW_KILLED * crew / (crew + marinesOnShip)
        val crDamage = 0.002f

        if(ship.originalOwner == 0 && ship.currentCR > 0f){
            Global.getSector().playerFleet.cargo.removeCrew(crDamage.toInt())
        }
        ship.currentCR = max(0f, ship.currentCR - crDamage)
    }
}