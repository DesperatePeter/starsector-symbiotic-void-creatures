package tecrys.svc.world.fleets

import com.fs.starfarer.api.campaign.econ.MarketAPI


private val hatcheryCombatRoleBaseWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 0.5f,
    "combatLarge" to 0.25f,
    "combatEliteSmall" to 0.00f,
    "combatEliteMedium" to 0.00f,
    "combatEliteLarge" to 0.00f,
    "combatEliteSmallEvolved" to 0.00f,
    "combatEliteMediumEvolved" to 0.00f,
    "combatEliteLargeEvolved" to 0.00f,
)
private val hatcheryCombatRoleFinalWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 1f,
    "combatLarge" to 0.5f,
    "combatEliteSmall" to 0.25f,
    "combatEliteMedium" to 0.25f,
    "combatEliteLarge" to 0.25f,
    "combatEliteSmallEvolved" to 0.25f,
    "combatEliteMediumEvolved" to 0.2f,
    "combatEliteLargeEvolved" to 0.05f,
)

// Note: spawnPowerScalings get overridden by custom spawn power
fun voidlingHatcherySettings(market: MarketAPI): FleetSpawnParameterSettings = FleetSpawnParameterSettings(
    7f,
    0.6f,
    10f,
    20f,
    0f,
    150f,
    0f,
    1,
    1,
    hatcheryCombatRoleBaseWeights,
    hatcheryCombatRoleFinalWeights
) { market.size.toFloat() } // set spawn power to market size of colony
