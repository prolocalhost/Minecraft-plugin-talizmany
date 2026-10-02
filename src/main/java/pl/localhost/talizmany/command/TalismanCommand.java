package pl.localhost.talizmany.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import pl.localhost.talizmany.Main;
import pl.localhost.talizmany.model.TalismanType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TalismanCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public TalismanCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
                return true;
            }

            Player player = (Player) sender;
            if (!player.hasPermission("talizmany.use")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            plugin.getTalismanGUI().openGUI(player);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("lista") || sub.equals("list")) {
            if (!sender.hasPermission("talizmany.use")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            sender.sendMessage(plugin.getConfigManager().color("&8---------- &6Dostępne Talizmany &8----------"));
            for (TalismanType type : TalismanType.values()) {
                String name = plugin.getConfigManager().getTalismanDisplayName(type);
                boolean enabled = plugin.getConfigManager().isTalismanEnabled(type);
                String status = enabled ? "&a[Włączony]" : "&c[Wyłączony]";
                sender.sendMessage(plugin.getConfigManager().color(" &8» &f" + type.getKey() + " &7- " + name + " " + status));
            }
            sender.sendMessage(plugin.getConfigManager().color("&8------------------------------------------"));
            return true;
        }

        if (sub.equals("reload") || sub.equals("rl")) {
            if (!sender.hasPermission("talizmany.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            plugin.getConfigManager().reloadConfig();
            sender.sendMessage(plugin.getConfigManager().getMessage("reload"));
            return true;
        }

        if (sub.equals("daj") || sub.equals("give")) {
            if (!sender.hasPermission("talizmany.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(plugin.getConfigManager().getMessage("usage-give"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                String msg = plugin.getConfigManager().getMessage("player-not-found")
                        .replace("%player%", args[1]);
                sender.sendMessage(msg);
                return true;
            }

            TalismanType type = TalismanType.fromKey(args[2]);
            if (type == null) {
                String available = String.join(", ", TalismanType.getKeys());
                String msg = plugin.getConfigManager().getMessage("unknown-type")
                        .replace("%types%", available);
                sender.sendMessage(msg);
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount <= 0 || amount > 64) {
                        sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
                    return true;
                }
            }

            plugin.getTalismanManager().giveTalisman(target, type, amount);

            String talismanName = plugin.getConfigManager().getTalismanDisplayName(type);

            String senderMsg = plugin.getConfigManager().getMessage("given-sender")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%talisman%", talismanName)
                    .replace("%player%", target.getName());
            sender.sendMessage(senderMsg);

            String targetMsg = plugin.getConfigManager().getMessage("given-target")
                    .replace("%amount%", String.valueOf(amount))
                    .replace("%talisman%", talismanName);
            target.sendMessage(targetMsg);
            return true;
        }

        sender.sendMessage(plugin.getConfigManager().getMessage("usage"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("talizmany.use")) {
                list.add("lista");
            }
            if (sender.hasPermission("talizmany.admin")) {
                list.add("daj");
                list.add("reload");
            }
            return filter(list, args[0]);
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("daj") || args[0].equalsIgnoreCase("give"))) {
            if (!sender.hasPermission("talizmany.admin")) return list;
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("daj") || args[0].equalsIgnoreCase("give"))) {
            if (!sender.hasPermission("talizmany.admin")) return list;
            return filter(TalismanType.getKeys(), args[2]);
        }

        if (args.length == 4 && (args[0].equalsIgnoreCase("daj") || args[0].equalsIgnoreCase("give"))) {
            if (!sender.hasPermission("talizmany.admin")) return list;
            list.add("1");
            list.add("16");
            list.add("64");
            return filter(list, args[3]);
        }

        return list;
    }

    private List<String> filter(List<String> list, String query) {
        String lower = query.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }
}
