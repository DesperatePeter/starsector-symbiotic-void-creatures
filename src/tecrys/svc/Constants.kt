package tecrys.svc

import tecrys.svc.utils.CampaignSettingDelegate

const val SVC_MOD_ID = "symbiotic_void_creatures"
const val SVC_FACTION_ID = "svc"
const val VWL_FACTION_ID = "vwl"
const val SVC_BASE_HULLMOD_ID = "BGECarapace"
const val SVC_VARIANT_TAG = "svc"
const val SVC_ALPHA_TAG = "svc_alpha"
const val WHALE_VARIANT_TAG = "krill"
const val SVC_FLEET_DEFEATED_MEM_KEY = "\$svc_was_defeated"
const val SVC_NOTIFICATIONS_CATEGORY_TEXT_KEY = "svc_notification_strings"
const val SVC_ALPHA_HULLMOD_ID = "svc_alpha_voidling"
const val MAGIC_BOUNTY_DEFEATED_KEY = "\$svc_alpha_large_succeeded"
const val MAGIC_SETTINGS_RELATIONS_KEY = "doNotAdjustRelationsWith"
const val MAGIC_SETTINGS_MOD_KEY = "SymbioticVoidCreatures"
const val WHALES_ENCOUNTER_MEM_KEY = "\$svc_whales_encounter"
const val FLEET_ORIGINAL_STRENGTH_KEY = "\$svc_fleet_original_strength"
const val WHALE_OIL_ITEM_ID = "svc_whale_oil"
const val WHALE_REPUTATION_MIN = 50f
const val WHALE_HULLMOD_ID = "svc_stjarwhal_hm"
const val VOID_CHITIN_ID = "svc_void_chitin"
const val VOID_CHITIN_PLATING_EXOTICA_CHIP_ID = "et_voidchitinplating"
val specialVoidlingWeaponIds = listOf(
    "svc_fangcluster",
    "svc_acidthrower",
    "svc_inksac",
    "svc_worm_colony_large",
    "svc_worm_colony_small",
    "svc_boltzmann_brain"
)

val BIOLOGICAL_HULL_TAGS = listOf(SVC_VARIANT_TAG, WHALE_VARIANT_TAG)

var internalWhaleReputation by CampaignSettingDelegate("$" + SVC_MOD_ID + "internal_whale_reputation", 100f)
var canRecoverVoidlings by CampaignSettingDelegate("$" + SVC_MOD_ID + "canRecoverVoidlings", false)
var canRecoverAlphas by CampaignSettingDelegate("$" + SVC_MOD_ID + "canRecoverAlphas", false)