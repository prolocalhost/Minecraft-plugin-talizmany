package pl.localhost.talizmany.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.localhost.talizmany.Main;
import pl.localhost.talizmany.model.TalismanType;

import java.util.List;

public class TalismanGUI implements Listener {

    private final Main plugin;
    private static final String GUI_IDENTIFIER = "§0";

    public TalismanGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player) {
        String baseTitle = plugin.getConfigManager().color(plugin.getConfig().getString("gui.title", "&8Katalog Talizmanow"));
        int size = plugin.getConfig().getInt("gui.size", 36);

        Inventory inv = Bukkit.createInventory(null, size, baseTitle + GUI_IDENTIFIER);

        Material bgMat = Material.matchMaterial(plugin.getConfig().getString("gui.background.material", "GRAY_STAINED_GLASS_PANE"));
        if (bgMat == null) bgMat = Material.GRAY_STAINED_GLASS_PANE;

        ItemStack bgItem = new ItemStack(bgMat);
        ItemMeta bgMeta = bgItem.getItemMeta();
        if (bgMeta != null) {
            bgMeta.setDisplayName(plugin.getConfigManager().color(plugin.getConfig().getString("gui.background.name", " ")));
            bgItem.setItemMeta(bgMeta);
        }

        for (int i = 0; i < size; i++) {
            inv.setItem(i, bgItem);
        }

        for (TalismanType type : TalismanType.values()) {
            int slot = plugin.getConfig().getInt("gui.slots." + type.getKey(), -1);
            if (slot >= 0 && slot < size) {
                ItemStack talismanItem = plugin.getTalismanManager().createTalisman(type, 1);
                inv.setItem(slot, talismanItem);
            }
        }

        int infoSlot = plugin.getConfig().getInt("gui.info-slot", 31);
        if (infoSlot >= 0 && infoSlot < size) {
            Material infoMat = Material.matchMaterial(plugin.getConfig().getString("gui.info-item.material", "BOOK"));
            if (infoMat == null) infoMat = Material.BOOK;

            ItemStack infoItem = new ItemStack(infoMat);
            ItemMeta infoMeta = infoItem.getItemMeta();
            if (infoMeta != null) {
                infoMeta.setDisplayName(plugin.getConfigManager().color(plugin.getConfig().getString("gui.info-item.name", "&6&lInformacje")));
                List<String> rawLore = plugin.getConfig().getStringList("gui.info-item.lore");
                infoMeta.setLore(plugin.getConfigManager().colorList(rawLore));
                infoItem.setItemMeta(infoMeta);
            }
            inv.setItem(infoSlot, infoItem);
        }

        player.openInventory(inv);

        Sound sound = plugin.getConfigManager().getGuiSound();
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.7f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().endsWith(GUI_IDENTIFIER)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getInventory().getSize()) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        TalismanType type = plugin.getTalismanManager().getTalismanType(clicked);
        if (type != null) {
            if (player.hasPermission("talizmany.admin") && event.isShiftClick()) {
                plugin.getTalismanManager().giveTalisman(player, type, 1);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.7f, 1.2f);
                String msg = plugin.getConfigManager().getMessage("given-target")
                        .replace("%amount%", "1")
                        .replace("%talisman%", plugin.getConfigManager().getTalismanDisplayName(type));
                player.sendMessage(msg);
            } else {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.5f);
            }
        }
    }
}
