package tecrys.svc.world.fleets

data class FleetSpawnParameterSettings (
    val maxSpawnPower: Float, // spawn power is a combination of time elapsed and player fleet strength. it's the basis for all other values
    val spawnPowerScalingByPlayerStrength: Float, // How quickly spawn power scales with player fleet strength
    val spawnPowerScalingByCycles: Float, // how quickly spawn power scales with time elapsed
    val fleetSizeScaling: Float, // How quickly voidling fleet size scales with spawn power
    val minFleetSize: Float,
    val maxFleetSize: Float,
    val flatSpawnPower: Float, // will be added on top of scaling spawn power
    val baseMaxFleetCount: Int, // number of fleets at 0 spawn power
    val finalMaxFleetCount: Int, // number of fleets at max spawn power
    // weights when spawn power is 0 (negative weights are the same as 0, but make things appear later)
    val combatRoleBaseWeights: Map<String, Float>,
    // weights when spawn power is max
    val combatRoleFinalWeights: Map<String, Float>
)