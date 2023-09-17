package tecrys.svc.plugins.SubAb;
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeItemPlugin;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;

public class svc_makeRecipePlugin {

    public static GenericSpecialItemPlugin makePlugin(){
        return new RecipeItemPlugin();
    }
}
