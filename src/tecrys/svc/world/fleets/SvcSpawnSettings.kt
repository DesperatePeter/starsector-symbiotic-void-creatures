package tecrys.svc.world.fleets

private val svcCombatRoleBaseWeights = mapOf(
    "combatSmall" to 0.5f,
    "combatMedium" to 0f,
    "combatLarge" to -1f,
    "combatEliteSmall" to 0f,
    "combatEliteMedium" to -1.5f,
    "combatEliteLarge" to -2f,
    "combatEliteSmallEvolved" to 0f,
    "combatEliteMediumEvolved" to -1.5f,
    "combatEliteLargeEvolved" to -2f,
)
private val svcCombatRoleFinalWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 0.6f,
    "combatLarge" to 0.3f,
    "combatEliteSmall" to 0.5f,
    "combatEliteMedium" to 0.4f,
    "combatEliteLarge" to 0.2f,
    "combatEliteSmallEvolved" to 0.25f,
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
    20,
    50,
    svcCombatRoleBaseWeights,
    svcCombatRoleFinalWeights
)
