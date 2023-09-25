package tecrys.svc.modintegration

import com.fs.starfarer.api.Global

const val STARSHIP_LEGENDS_ID = "sun_starship_legends"
const val SUBSTANCE_ABUSE_ID = "alcoholism"
const val EXOTICA_MOD_ID = "exoticatechnologies"

fun isSubstanceAbuseEnabled(): Boolean{
    return isModEnabled(SUBSTANCE_ABUSE_ID)
}

fun isStarshipLegendsEnabled(): Boolean{
    return isModEnabled(STARSHIP_LEGENDS_ID)
}

fun isExoticaEnabled(): Boolean{
    return isModEnabled(EXOTICA_MOD_ID)
}

private fun isModEnabled(modId: String): Boolean{
    return Global.getSettings().modManager.isModEnabled(modId)
}

