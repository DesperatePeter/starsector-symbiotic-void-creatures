package tecrys.svc.utils

import com.fs.starfarer.api.Global
import kotlin.reflect.KProperty

class CampaignSettingDelegate<T>(private val key: String, private val defaultValue: T)  {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T{
        return (Global.getSector().persistentData[key] as? T?) ?: defaultValue
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T){
        Global.getSector().persistentData[key] = value
    }
}