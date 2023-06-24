package tecrys.svc.world;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import org.apache.log4j.Level;
import tecrys.svc.ConstantsKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SVCGen implements SectorGeneratorPlugin {
    public void generate(SectorAPI sector) {
        initFactionRelationships(sector);
    }


    public static void initFactionRelationships(SectorAPI sector) {
        FactionAPI svc = sector.getFaction(ConstantsKt.SVC_FACTION_ID);
        // FactionAPI uvc = sector.getFaction(ConstantsKt.UVC_FACTION_ID);
        if (svc == null) {
            Global.getLogger(SVCGen.class).log(Level.ERROR, "Unable to get SVC faction from sector." +
                    " Aborting faction initialization.");
            return;
        }
        FactionAPI player = sector.getFaction("player");
        FactionAPI hegemony = sector.getFaction("hegemony");
        FactionAPI tritachyon = sector.getFaction("tritachyon");
        FactionAPI pirates = sector.getFaction("pirates");
        FactionAPI independent = sector.getFaction("independent");
        FactionAPI church = sector.getFaction("luddic_church");
        FactionAPI path = sector.getFaction("luddic_path");
        FactionAPI kol = sector.getFaction("knights_of_ludd");
        FactionAPI diktat = sector.getFaction("sindrian_diktat");
        FactionAPI persean = sector.getFaction("persean");

        List<FactionAPI> hostileFactions = Arrays.asList(player, hegemony, tritachyon, pirates, independent, church, path, kol, diktat, persean);
        List<String> hostileFactionStrings = Arrays.asList("exigency", "shadow_industry", "mayorate", "blackrock", "tiandong", "SCY", "neutrinocorp", "interstellarimperium", "diableavionics");

        for(String fs : hostileFactionStrings){
            svc.setRelationship(fs, -0.5F);
        }
        for(FactionAPI f : hostileFactions){
            svc.setRelationship(f.getId(), -0.5F);
        }
        svc.setRelationship(ConstantsKt.UVC_FACTION_ID, -0.5f);


    }
}
