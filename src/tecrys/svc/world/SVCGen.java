/*    */ package tecrys.svc.world;
/*    */ 
/*    */ import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
/*    */ import com.fs.starfarer.api.campaign.SectorAPI;
/*    */ import com.fs.starfarer.api.campaign.SectorGeneratorPlugin;
import org.apache.log4j.Level;
import tecrys.svc.ConstantsKt;

/*    */
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SVCGen
/*    */   implements SectorGeneratorPlugin
/*    */ {
/*    */   public void generate(SectorAPI sector)
/*    */   {
/* 15 */     initFactionRelationships(sector);
/*    */   }
/*    */   
/*    */ 
/*    */   public static void initFactionRelationships(SectorAPI sector)
/*    */   {
/* 21 */     FactionAPI bge = sector.getFaction(ConstantsKt.SVC_FACTION_ID);
             FactionAPI uvc = sector.getFaction(ConstantsKt.UVC_FACTION_ID);
             if(uvc == null || bge == null){
                 Global.getLogger(SVCGen.class).log(Level.ERROR, "Unable to get UVC and SVC factions from sector");
                 return;
             }
/* 22 */     FactionAPI player = sector.getFaction("player");
/* 23 */     FactionAPI hegemony = sector.getFaction("hegemony");
/* 24 */     FactionAPI tritachyon = sector.getFaction("tritachyon");
/* 25 */     FactionAPI pirates = sector.getFaction("pirates");
/* 26 */     FactionAPI independent = sector.getFaction("independent");
/* 27 */     FactionAPI church = sector.getFaction("luddic_church");
/* 28 */     FactionAPI path = sector.getFaction("luddic_path");
/* 29 */     FactionAPI kol = sector.getFaction("knights_of_ludd");
/* 30 */     FactionAPI diktat = sector.getFaction("sindrian_diktat");
/* 31 */     FactionAPI persean = sector.getFaction("persean");
/*    */     
/* 33 */     bge.setRelationship(player.getId(), -0.5F);
/* 34 */     bge.setRelationship(hegemony.getId(), -0.5F);
/* 35 */     bge.setRelationship(tritachyon.getId(), -0.5F);
/* 36 */     bge.setRelationship(pirates.getId(), -0.5F);
/* 37 */     bge.setRelationship(independent.getId(), -0.5F);
/* 38 */     bge.setRelationship(persean.getId(), -0.5F);
/* 39 */     bge.setRelationship(church.getId(), -0.5F);
/* 40 */     bge.setRelationship(path.getId(), -0.5F);
/* 41 */     bge.setRelationship(kol.getId(), -0.5F);
/* 42 */     bge.setRelationship(diktat.getId(), -0.5F);
/* 43 */     bge.setRelationship("exigency", -0.5F);
/* 44 */     bge.setRelationship("shadow_industry", -0.5F);
/* 45 */     bge.setRelationship("mayorate", -0.5F);
/* 46 */     bge.setRelationship("blackrock", -0.5F);
/* 47 */     bge.setRelationship("tiandong", -0.5F);
/* 48 */     bge.setRelationship("SCY", -0.5F);
/* 49 */     bge.setRelationship("neutrinocorp", -0.5F);
/* 50 */     bge.setRelationship("interstellarimperium", -0.5F);
/* 51 */     bge.setRelationship("diableavionics", -0.5F);
             bge.setRelationship(ConstantsKt.UVC_FACTION_ID, -0.5f);
/*    */   }
/*    */ }


/* Location:              C:\Games\Starsector08a\mods\BGE\jars\BGEMeleeAI.jar!\bge\data\scripts\world\BGEGen.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */