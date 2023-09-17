package tecrys.svc.plugins.SubAb;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.alcoholism.itemPlugins.AlcoholItemPlugin;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI;
import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class svc_AlcoholItemPlugin extends BaseSpecialItemPlugin {

    BaseSpecialItemPlugin plugin = null;

    @Override
    public void init(CargoStackAPI stack) {
        super.init(stack);
        if (Global.getSettings().getModManager().isModEnabled("alcoholism")) {
            plugin = new AlcoholItemPlugin();
            plugin.init(stack);
            plugin.setId(itemId);
        }
    }


    @Override
    public String getDesignType() {
        if (plugin != null) {
            return plugin.getDesignType();
        }
        return null;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, CargoTransferHandlerAPI transferHandler, Object stackSource) {

        if (plugin != null) {
            plugin.createTooltip(tooltip, expanded, transferHandler, stackSource);
        }
    }

    @Override
    public void render(float x, float y, float w, float h, float alphaMult, float glowMult, SpecialItemRendererAPI renderer) {
        if (plugin != null) {
            plugin.render(x, y, w, h, alphaMult, glowMult, renderer);
        }
    }

    @Override
    public boolean hasRightClickAction() {
        return true;
    }

    @Override
    public boolean shouldRemoveOnRightClickAction() {
        return false;
    }

    @Override
    public void performRightClickAction() {
        if (plugin != null) {
            plugin.performRightClickAction();
        }
    }
}