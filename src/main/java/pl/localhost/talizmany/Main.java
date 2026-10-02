package pl.localhost.talizmany;

import org.bukkit.plugin.java.JavaPlugin;
import pl.localhost.talizmany.command.TalismanCommand;
import pl.localhost.talizmany.gui.TalismanGUI;
import pl.localhost.talizmany.listener.TalismanListener;
import pl.localhost.talizmany.manager.ConfigManager;
import pl.localhost.talizmany.manager.CooldownManager;
import pl.localhost.talizmany.manager.TalismanManager;

public final class Main extends JavaPlugin {

    private ConfigManager configManager;
    private CooldownManager cooldownManager;
    private TalismanManager talismanManager;
    private TalismanGUI talismanGUI;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.talismanManager = new TalismanManager(this);
        this.talismanGUI = new TalismanGUI(this);

        TalismanCommand talismanCommand = new TalismanCommand(this);
        if (getCommand("talizman") != null) {
            getCommand("talizman").setExecutor(talismanCommand);
            getCommand("talizman").setTabCompleter(talismanCommand);
        }

        getServer().getPluginManager().registerEvents(new TalismanListener(this), this);
        getServer().getPluginManager().registerEvents(talismanGUI, this);

        this.cooldownManager.startActionBarTask();

        getLogger().info("Plugin localhost-talizmany zostal pomyslnie wlaczony!");
    }

    @Override
    public void onDisable() {
        if (this.cooldownManager != null) {
            this.cooldownManager.stopActionBarTask();
        }

        getLogger().info("Plugin localhost-talizmany zostal pomyslnie wylaczony!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public TalismanManager getTalismanManager() {
        return talismanManager;
    }

    public TalismanGUI getTalismanGUI() {
        return talismanGUI;
    }

    public String color(String text) {
        return configManager.color(text);
    }
}
