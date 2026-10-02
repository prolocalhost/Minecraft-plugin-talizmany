package pl.localhost.talizmany.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pl.localhost.talizmany.Main;
import pl.localhost.talizmany.model.TalismanType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final Main plugin;
    private final Map<UUID, Map<TalismanType, Long>> cooldowns = new HashMap<>();
    private BukkitTask actionBarTask;

    public CooldownManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean hasCooldown(UUID uuid, TalismanType type) {
        Map<TalismanType, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return false;
        Long expireTime = playerCooldowns.get(type);
        if (expireTime == null) return false;
        return expireTime > System.currentTimeMillis();
    }

    public long getRemainingSeconds(UUID uuid, TalismanType type) {
        Map<TalismanType, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0;
        Long expireTime = playerCooldowns.get(type);
        if (expireTime == null) return 0;
        long timeDiff = expireTime - System.currentTimeMillis();
        return Math.max(0, Math.round(timeDiff / 1000.0));
    }

    public void setCooldown(UUID uuid, TalismanType type, long seconds) {
        long expireTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>()).put(type, expireTime);
    }

    public void clearCooldown(UUID uuid, TalismanType type) {
        Map<TalismanType, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(type);
        }
    }

    public void startActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
        }

        actionBarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.getConfigManager().isActionBarEnabled()) {
                    return;
                }

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    TalismanType type = plugin.getTalismanManager().getTalismanType(mainHand);

                    if (type == null) {
                        ItemStack offHand = player.getInventory().getItemInOffHand();
                        type = plugin.getTalismanManager().getTalismanType(offHand);
                    }

                    if (type != null) {
                        String talismanName = plugin.getConfigManager().getTalismanDisplayName(type);

                        if (hasCooldown(player.getUniqueId(), type)) {
                            long remaining = getRemainingSeconds(player.getUniqueId(), type);
                            String rawMsg = plugin.getConfigManager().getRawMessage("actionbar-cooldown")
                                    .replace("%talisman%", talismanName)
                                    .replace("%time%", String.valueOf(remaining));
                            player.sendActionBar(rawMsg);
                        } else {
                            String rawMsg = plugin.getConfigManager().getRawMessage("actionbar-ready")
                                    .replace("%talisman%", talismanName);
                            player.sendActionBar(rawMsg);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void stopActionBarTask() {
        if (actionBarTask != null) {
            actionBarTask.cancel();
            actionBarTask = null;
        }
        cooldowns.clear();
    }
}
