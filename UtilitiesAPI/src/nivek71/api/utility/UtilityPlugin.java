package nivek71.api.utility;

import nivek71.api.utility.functions.RunnableEx;
import nivek71.api.utility.input.InventoryGUI;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class UtilityPlugin extends JavaPlugin {
    private static List<RunnableEx> enableFunctions = new ArrayList<>();
    // cannot use isEnabled() from JavaPlugin because JavaPlugin instance is not available when this is needed
    private static boolean enabled = false;

    public static void registerOnEnableFunction(RunnableEx runnableEx) {
        if (!enabled)
            enableFunctions.add(runnableEx);
        else Logger.tryOrLog(runnableEx);
    }

    public static UtilityPlugin getPlugin() {
        return JavaPlugin.getPlugin(UtilityPlugin.class);
    }

    @Override
    public void onLoad() {
        saveResource("database.yml", false);
    }

    @Override
    public void onEnable() {
        enabled = true;
        Logger.tryOrLog(InventoryGUI::load);

        for (RunnableEx runnableEx : enableFunctions)
            Logger.tryOrLog(runnableEx);
        enableFunctions.clear();
    }

    @Override
    public void onDisable() {
        enabled = false;
    }
}
