package tecrys.svc.plugins.exotica

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType
import com.fs.starfarer.api.combat.ShipHullSpecAPI
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
        private var removeFromMembers: MutableSet<String> = mutableSetOf()
        private var backupSpecsByMember = mutableMapOf<String, ShipHullSpecAPI>()

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
            val spec = backupSpecsByMember[id] ?: member.hullSpec?.baseHull ?: Global.getSettings().getHullSpec(member.hullId)
            member.variant.setHullSpecAPI(spec)
        }

        private fun installIfApplicable(member: FleetMemberAPI){
            if(member.hullSpec?.shipDefenseId == "parry") return
            member.id ?: return
            backupSpecsByMember[member.id] = member.hullSpec
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