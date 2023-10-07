package tecrys.svc.plugins.exotica

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.fleet.FleetMemberAPI
import tecrys.svc.SVC_MOD_ID
import tecrys.svc.utils.CampaignSettingDelegate
import tecrys.svc.utils.cloneHullSpec
import tecrys.svc.utils.setShieldType

class VoidChitinPlatingListener: EveryFrameScript {
    companion object{
        private var installedOnMembers: MutableSet<String> by CampaignSettingDelegate(
            "$" + SVC_MOD_ID + "voidPlatingIsInstalledOnMembers",
            mutableSetOf()
        )
        private var removeFromMembers: MutableSet<String> by CampaignSettingDelegate(
            "$" + SVC_MOD_ID + "voidPlatingRemoveFromMembers",
            mutableSetOf()
        )
        private var backupShieldTypesByMember: MutableMap<String, ShieldType> by CampaignSettingDelegate(
            "$" + SVC_MOD_ID + "voidPlatingBackupShieldTypes",
            mutableMapOf()
        )
        private var backupDefenseIdsByMember: MutableMap<String, String> by CampaignSettingDelegate(
            "$" + SVC_MOD_ID + "voidPlatingBackupDefenseIds",
            mutableMapOf()
        )

        fun installOnMember(member: FleetMemberAPI){
            val id = member.id ?: return
            installedOnMembers.add(id)
            removeFromMembers.remove(id)
            installIfApplicable(member)
        }

        fun uninstallFromMember(member: FleetMemberAPI){
            val id = member.id ?: return
            installedOnMembers.remove(id)
            removeFromMembers.add(id)
            uninstallIfApplicable(member)
        }

        private fun uninstallIfApplicable(member: FleetMemberAPI){
            if(member.hullSpec?.shipDefenseId != "parry") return
            val id = member.id ?: return
            val defenseId = backupDefenseIdsByMember[id] ?: member.hullSpec?.baseHull?.shipDefenseId ?: ""
            val shieldType = backupShieldTypesByMember[id] ?: ShieldType.NONE
            backupDefenseIdsByMember.remove(id)
            backupShieldTypesByMember.remove(id)
            val clone = cloneHullSpec(member.hullSpec)

            setShieldType(clone.shieldSpec, shieldType)
            clone.shipDefenseId = defenseId
            member.variant.setHullSpecAPI(clone)
        }

        private fun installIfApplicable(member: FleetMemberAPI){
            if(member.hullSpec?.shipDefenseId == "parry") return
            member.id ?: return
            backupShieldTypesByMember[member.id] = member.hullSpec.shieldType
            backupDefenseIdsByMember[member.id] = member.hullSpec.shipDefenseId
            val clone = cloneHullSpec(member.hullSpec)
            setShieldType(clone.shieldSpec, ShieldAPI.ShieldType.PHASE)
            clone.shipDefenseId = "parry"
            member.variant.setHullSpecAPI(clone)
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