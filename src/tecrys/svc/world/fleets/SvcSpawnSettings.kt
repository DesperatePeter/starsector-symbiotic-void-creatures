package tecrys.svc.world.fleets

import com.fs.starfarer.api.Global

private val svcCombatRoleBaseWeights = mapOf(
    "combatSmall" to 0.2f,
    "combatMedium" to 0f,
    "combatLarge" to 0f,
    "combatEliteSmall" to 0f,
    "combatEliteMedium" to 0f,
    "combatEliteLarge" to 0f,
    "combatEliteSmallEvolved" to 0f,
    "combatEliteMediumEvolved" to 0f,
    "combatEliteLargeEvolved" to 0f,
)
private val svcCombatRoleFinalWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 0.6f,
    "combatLarge" to 0.3f,
    "combatEliteSmall" to 0.3f,
    "combatEliteMedium" to 0.2f,
    "combatEliteLarge" to 0.1f,
    "combatEliteSmallEvolved" to 0.3f,
    "combatEliteMediumEvolved" to 0.2f,
    "combatEliteLargeEvolved" to 0.1f,
)
val svcSettings = FleetSpawnParameterSettings(
    Global.getSettings().battleSize.toFloat(),
    0.6f,
    10f,
    1f,
    10f,
    Global.getSettings().battleSize.toFloat(),
    5f,
    12,
    20,
    svcCombatRoleBaseWeights,
    svcCombatRoleFinalWeights
)
