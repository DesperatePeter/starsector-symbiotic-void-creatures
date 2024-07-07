package tecrys.svc.shipsystems.spooky.gui

import com.fs.starfarer.api.Global
import org.magiclib.combatgui.MagicCombatGuiLayout
import java.awt.Color

val spookyGuiLayout = MagicCombatGuiLayout(
    0.3f,
    0.55f,
    170f,
    25f,
    1.0f,
    Global.getSettings().basePlayerColor,
    // Color(120, 10, 180, 200),
    5f,
    0.3f,
    0.2f,
    32f,
    "graphics/fonts/insignia21LTaa.fnt",
    0.3f,
    0.8f
)