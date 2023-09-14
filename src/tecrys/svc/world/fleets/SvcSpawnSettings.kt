package tecrys.svc.world.fleets

private val svcCombatRoleBaseWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 0.5f,
    "combatLarge" to 0.25f,
    "combatEliteSmall" to 0.05f,
    "combatEliteMedium" to 0.02f,
    "combatEliteLarge" to 0.01f,
    "combatEliteSmallEvolved" to 0.05f,
    "combatEliteMediumEvolved" to 0.02f,
    "combatEliteLargeEvolved" to 0.01f,
)
private val svcCombatRoleFinalWeights = mapOf(
    "combatSmall" to 0.3f,
    "combatMedium" to 0.4f,
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
