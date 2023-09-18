package tecrys.svc.plugins.substanceabuse;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.itemPlugins.RecipeItemPlugin;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.GenericSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class svc_RecipeItemPlugin extends GenericSpecialItemPlugin {

    GenericSpecialItemPlugin plugin = null;

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);
        if (Global.getSettings().getModManager().isModEnabled("alcoholism")) {
            plugin = new RecipeItemPlugin();
            plugin.init(stack);
            plugin.setId(itemId);
        }
    }

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public void performRightClickAction() {
        if (plugin != null) {
            plugin.performRightClickAction();
        }
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return true;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {
        if (plugin != null) {
            plugin.createTooltip(tooltip, expanded, transferHandler, stackSource);
        }

    }

}