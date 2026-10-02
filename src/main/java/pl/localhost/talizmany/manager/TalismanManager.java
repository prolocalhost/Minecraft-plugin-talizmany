package pl.localhost.talizmany.manager;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pl.localhost.talizmany.Main;
import pl.localhost.talizmany.model.TalismanType;

import java.util.List;
import java.util.Map;

public class TalismanManager {

    private final Main plugin;
    private final NamespacedKey talismanKey;

    public TalismanManager(Main plugin) {
        this.plugin = plugin;
        this.talismanKey = new NamespacedKey(plugin, "talisman_type");
    }

    public ItemStack createTalisman(TalismanType type, int amount) {
        Material defaultMat;
        switch (type) {
            case VAMPIRISM:
                defaultMat = Material.REDSTONE;
                break;
            case PHOENIX:
                defaultMat = Material.TOTEM_OF_UNDYING;
                break;
            case THUNDER:
                defaultMat = Material.AMETHYST_SHARD;
                break;
            case WIND_DASH:
                defaultMat = Material.FEATHER;
                break;
            case TITAN:
            default:
                defaultMat = Material.IRON_INGOT;
                break;
        }

        Material mat = plugin.getConfigManager().getTalismanMaterial(type, defaultMat);
        ItemStack item = new ItemStack(mat, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(plugin.getConfigManager().getTalismanDisplayName(type));

            List<String> lore = plugin.getConfigManager().getTalismanLore(type);
            meta.setLore(lore);

            int cmd = plugin.getConfigManager().getTalismanCustomModelData(type);
            if (cmd > 0) {
                meta.setCustomModelData(cmd);
            }

            meta.getPersistentDataContainer().set(talismanKey, PersistentDataType.STRING, type.getKey());
            item.setItemMeta(meta);
        }

        return item;
    }

    public TalismanType getTalismanType(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(talismanKey, PersistentDataType.STRING)) {
            String key = pdc.get(talismanKey, PersistentDataType.STRING);
            return TalismanType.fromKey(key);
        }

        if (meta.hasCustomModelData()) {
            int cmd = meta.getCustomModelData();
            for (TalismanType type : TalismanType.values()) {
                if (plugin.getConfigManager().getTalismanCustomModelData(type) == cmd) {
                    return type;
                }
            }
        }

        if (meta.hasDisplayName()) {
            String displayName = meta.getDisplayName();
            for (TalismanType type : TalismanType.values()) {
                if (displayName.equals(plugin.getConfigManager().getTalismanDisplayName(type))) {
                    return type;
                }
            }
        }

        return null;
    }

    public boolean hasTalisman(Player player, TalismanType type) {
        if (!plugin.getConfigManager().isTalismanEnabled(type)) {
            return false;
        }

        String mode = plugin.getConfigManager().getActivationMode();

        if ("HANDS".equalsIgnoreCase(mode)) {
            return getTalismanType(player.getInventory().getItemInMainHand()) == type ||
                    getTalismanType(player.getInventory().getItemInOffHand()) == type;
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                if (getTalismanType(item) == type) {
                    return true;
                }
            }
        }

        return getTalismanType(player.getInventory().getItemInOffHand()) == type;
    }

    public boolean giveTalisman(Player player, TalismanType type, int amount) {
        ItemStack item = createTalisman(type, amount);
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item);

        if (!leftovers.isEmpty()) {
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
            player.sendMessage(plugin.getConfigManager().getMessage("inventory-full"));
            return false;
        }
        return true;
    }

    public NamespacedKey getTalismanKey() {
        return talismanKey;
    }
}
