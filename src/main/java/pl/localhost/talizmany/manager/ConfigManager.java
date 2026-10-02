package pl.localhost.talizmany.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import pl.localhost.talizmany.Main;
import pl.localhost.talizmany.model.TalismanType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {

    private final Main plugin;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public String color(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public List<String> colorList(List<String> list) {
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::color).collect(Collectors.toList());
    }

    public String getPrefix() {
        return color(getConfig().getString("messages.prefix", "&8[&6Talizmany&8] &7"));
    }

    public String getMessage(String path) {
        String msg = getConfig().getString("messages." + path, "&cWiadomość nieznaleziona: " + path);
        return getPrefix() + color(msg);
    }

    public String getRawMessage(String path) {
        String msg = getConfig().getString("messages." + path, "");
        return color(msg);
    }

    public String getActivationMode() {
        return getConfig().getString("settings.activation-mode", "INVENTORY").toUpperCase();
    }

    public boolean isActionBarEnabled() {
        return getConfig().getBoolean("settings.actionbar-enabled", true);
    }

    public Sound getGuiSound() {
        String soundName = getConfig().getString("settings.gui-sound", "UI_BUTTON_CLICK");
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            return Sound.UI_BUTTON_CLICK;
        }
    }

    public boolean isTalismanEnabled(TalismanType type) {
        return getConfig().getBoolean("talizmany." + type.getKey() + ".enabled", true);
    }

    public Material getTalismanMaterial(TalismanType type, Material fallback) {
        String matName = getConfig().getString("talizmany." + type.getKey() + ".material", fallback.name());
        Material mat = Material.matchMaterial(matName);
        return mat != null ? mat : fallback;
    }

    public int getTalismanCustomModelData(TalismanType type) {
        return getConfig().getInt("talizmany." + type.getKey() + ".custom-model-data", 0);
    }

    public String getTalismanDisplayName(TalismanType type) {
        return color(getConfig().getString("talizmany." + type.getKey() + ".name", type.getDefaultName()));
    }

    public List<String> getTalismanLore(TalismanType type) {
        List<String> rawLore = getConfig().getStringList("talizmany." + type.getKey() + ".lore");
        String path = "talizmany." + type.getKey() + ".";

        double chance = getConfig().getDouble(path + "chance", 0.0);
        double heal = getConfig().getDouble(path + "heal-percent", 0.0);
        int cooldown = getConfig().getInt(path + "cooldown", 0);
        double damage = getConfig().getDouble(path + "extra-damage", 0.0);
        int duration = getConfig().getInt(path + "duration", 0);

        List<String> formatted = new ArrayList<>();
        for (String line : rawLore) {
            String processed = line
                    .replace("%chance%", String.valueOf(chance))
                    .replace("%heal%", String.valueOf(heal))
                    .replace("%cooldown%", String.valueOf(cooldown))
                    .replace("%damage%", String.valueOf(damage))
                    .replace("%duration%", String.valueOf(duration));
            formatted.add(color(processed));
        }
        return formatted;
    }

    public double getTalismanDouble(TalismanType type, String key, double def) {
        return getConfig().getDouble("talizmany." + type.getKey() + "." + key, def);
    }

    public int getTalismanInt(TalismanType type, String key, int def) {
        return getConfig().getInt("talizmany." + type.getKey() + "." + key, def);
    }

    public Sound getTalismanSound(TalismanType type, Sound def) {
        String name = getConfig().getString("talizmany." + type.getKey() + ".sound");
        if (name == null) return def;
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }

    public Particle getTalismanParticle(TalismanType type, Particle def) {
        String name = getConfig().getString("talizmany." + type.getKey() + ".particle");
        if (name == null) return def;
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return def;
        }
    }
}
