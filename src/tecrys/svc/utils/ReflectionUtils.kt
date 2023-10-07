package tecrys.svc.utils

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

fun cloneHullSpec(hullSpec: ShipHullSpecAPI): ShipHullSpecAPI {
    val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    val invokeMethod = MethodHandles.lookup().findVirtual(methodClass,
        "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

    var foundMethod: Any? = null

    for (method in hullSpec::class.java.declaredMethods as Array<Any>)
    {
        if (method.toString().contains("clone"))
        {
            foundMethod = method
            break
        }
    }
    return invokeMethod.invoke(foundMethod, hullSpec) as ShipHullSpecAPI
}

fun setShieldType(shieldSpec: ShipHullSpecAPI.ShieldSpecAPI, type: ShieldAPI.ShieldType){
    val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    val invokeMethod = MethodHandles.lookup().findVirtual(methodClass,
        "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

    var foundMethod: Any? = null

    for (method in shieldSpec::class.java.declaredMethods as Array<Any>)
    {
        if (method.toString().contains("setType"))
        {
            foundMethod = method
        }
    }
    invokeMethod.invoke(foundMethod, shieldSpec, type)
}