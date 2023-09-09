package tecrys.svc.world.fleets

private val whaleCombatRoleBaseWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 0.5f,
    "combatLarge" to 0.25f,
)
private val whaleCombatRoleFinalWeights = mapOf(
    "combatSmall" to 1f,
    "combatMedium" to 1f,
    "combatLarge" to 1f,
)
val whaleSettings = FleetSpawnParameterSettings(
    250f,
    0.6f,
    10f,
    1f,
    10f,
    300f,
    5f,
    1,
    1,
    whaleCombatRoleBaseWeights,
    whaleCombatRoleFinalWeights
)
