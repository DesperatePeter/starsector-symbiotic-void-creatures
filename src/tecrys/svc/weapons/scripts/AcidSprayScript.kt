package tecrys.svc.weapons.scripts

import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.ShipAPI
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.max

class AcidSprayScript(
    private val ship: ShipAPI,
    engine: CombatEngineAPI,
    location: Vector2f,
    velocity: Vector2f,
    effectColor: Color,
    effectRadius: Float,
    duration: Float,
    effectRadiusGrowth: Float
) : CloudEffectScript(engine, location, velocity, effectColor, effectRadius, duration, effectRadiusGrowth, true, 200f) {

    companion object{
        const val ARMOR_DAMAGE_PER_CELL_PER_SECOND = 10f
    }
    override fun executeOnRemoval() { }

    override fun executeOnAdvance(amount: Float) {
        val dmg = ARMOR_DAMAGE_PER_CELL_PER_SECOND * amount
        var totalDamage = 0f
        CombatUtils.getShipsWithinRange(location, currentRadius).filterNotNull().filter {
            ship.owner != it.owner && it.phaseCloak?.isActive != true
        }.forEach {
            val grid = it.armorGrid.grid
            for(i in grid.indices){
                val row = grid[i]
                for(j in row.indices){
                    val loc = it.armorGrid.getLocation(i, j)
                    if((loc - location).length() <= currentRadius){
                        val previousValue = grid[i][j]
                        grid[i][j] = max(0f, grid[i][j] - dmg)
                        totalDamage += previousValue - grid[i][j]
                    }
                }
            }
            engine.addFloatingDamageText(it.location, totalDamage, Color.YELLOW, it, ship)
        }
    }
}