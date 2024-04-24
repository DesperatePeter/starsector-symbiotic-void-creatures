package tecrys.svc.world.fleets

private val svcCombatRoleBaseWeights = mapOf(
    "combatSmall" to 0.2f,
    "combatMedium" to 0f,
    "combatLarge" to -0.3f,
    "combatEliteSmall" to -0.1f,
    "combatEliteMedium" to -0.1f,
    "combatEliteLarge" to -0.5f,
    "combatEliteSmallEvolved" to -0.1f,
    "combatEliteMediumEvolved" to -0.1f,
    "combatEliteLargeEvolved" to -0.5f,
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
    350f,
    0.6f,
    10f,
    1f,
    10f,
    400f,
    5f,
    12,
    20,
    svcCombatRoleBaseWeights,
    svcCombatRoleFinalWeights
)
