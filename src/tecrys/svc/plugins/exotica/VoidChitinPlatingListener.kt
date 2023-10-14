package tecrys.svc.plugins.exotica

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.cloneHullSpec
import tecrys.svc.utils.setShieldType
import java.awt.Color

class VoidChitinPlatingListener: EveryFrameScript {
    companion object{
        private var installedOnMembers: Set<Any> by CampaignSettingDelegate(
            "$" + SVC_MOD_ID + "voidPlatingIsInstalledOnMembers",
            setOf()
        )
        private var removeFromMembers: MutableSet<String> = mutableSetOf()
        private var backupSpecsByMember = mutableMapOf<String, ShipHullSpecAPI>()

        fun installOnMember(member: FleetMemberAPI){
            val id = member.id ?: return
            installedOnMembers = installedOnMembers + id
            removeFromMembers.remove(id)
            installIfApplicable(member)
        }

        fun uninstallFromMember(member: FleetMemberAPI){
            val id = member.id ?: return
            installedOnMembers = installedOnMembers - id
            removeFromMembers.add(id)
            uninstallIfApplicable(member)
        }

        private fun uninstallIfApplicable(member: FleetMemberAPI){
            if(member.hullSpec?.shipDefenseId != "parry") return
            val id = member.id ?: return
            val spec = backupSpecsByMember[id] ?: member.hullSpec?.baseHull ?: Global.getSettings().getHullSpec(member.hullId)
            member.variant.setHullSpecAPI(spec)
        }

        private fun installIfApplicable(member: FleetMemberAPI){
            Global.getSector().playerPerson.faction.knownWeapons.removeIf { w ->
                Global.getSettings().getWeaponSpec(w).tags.contains("base_bp")
            }
            fun logError(){
                Global.getSector()?.campaignUI?.addMessage(
                    "Error when trying to change hull-spec. Void Chitin Plating not properly applied." +
                            " Please remove to get rid of this error message!", Color.red)
            }
            if(member.hullSpec?.shipDefenseId == "parry") return
            member.id ?: return
            backupSpecsByMember[member.id] = member.hullSpec
            cloneHullSpec(member.hullSpec)?.let {clone ->
                if(!setShieldType(clone.shieldSpec, ShieldType.PHASE)){
                    logError()
                    return
                }
                clone.shipDefenseId = "parry"

                member.variant.setHullSpecAPI(clone)
            } ?: kotlin.run {
                logError()
            }
        }
    }

    override fun isDone(): Boolean = false

    override fun runWhilePaused(): Boolean = true

    override fun advance(amount: Float) {
        val members = Global.getSector()?.playerFleet?.membersWithFightersCopy ?: return
        members.filterNotNull().filter { member ->
            installedOnMembers.contains(member.id)
        }.forEach { member ->
            installIfApplicable(member)
        }
        members.filterNotNull().filter { member ->
            removeFromMembers.contains(member.id)
        }.forEach { member ->
            uninstallIfApplicable(member)
        }
    }
}