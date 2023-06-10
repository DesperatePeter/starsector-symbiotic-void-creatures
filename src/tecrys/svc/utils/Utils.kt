package tecrys.svc.utils

import org.lwjgl.util.vector.Vector2f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val degToRad: Float = PI.toFloat() / 180f
fun vectorFromAngleDeg(angle: Float): Vector2f {
    return Vector2f(cos(angle * degToRad), sin(angle * degToRad))
}