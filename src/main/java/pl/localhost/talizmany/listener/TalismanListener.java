package pl.localhost.talizmany.listener;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import pl.localhost.talizmany.Main;
import pl.localhost.talizmany.model.TalismanType;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class TalismanListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();
    private final Set<UUID> fallImmunePlayers = new HashSet<>();

    public TalismanListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();

        if (plugin.getTalismanManager().hasTalisman(attacker, TalismanType.VAMPIRISM)) {
            double chance = plugin.getConfigManager().getTalismanDouble(TalismanType.VAMPIRISM, "chance", 35.0);
            if (random.nextDouble() * 100.0 <= chance) {
                double healPercent = plugin.getConfigManager().getTalismanDouble(TalismanType.VAMPIRISM, "heal-percent", 25.0);
                double damageDealt = event.getFinalDamage();
                double healAmount = Math.max(0.5, damageDealt * (healPercent / 100.0));

                AttributeInstance maxHealthAttr = attacker.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                double maxHealth = (maxHealthAttr != null) ? maxHealthAttr.getValue() : 20.0;
                double newHealth = Math.min(maxHealth, attacker.getHealth() + healAmount);
                attacker.setHealth(newHealth);

                Particle particle = plugin.getConfigManager().getTalismanParticle(TalismanType.VAMPIRISM, Particle.DAMAGE_INDICATOR);
                Sound sound = plugin.getConfigManager().getTalismanSound(TalismanType.VAMPIRISM, Sound.ENTITY_PLAYER_BURP);

                attacker.getWorld().spawnParticle(particle, event.getEntity().getLocation().add(0, 1.0, 0), 8, 0.3, 0.5, 0.3, 0.05);
                attacker.playSound(attacker.getLocation(), sound, 0.6f, 1.2f);
            }
        }

        if (plugin.getTalismanManager().hasTalisman(attacker, TalismanType.THUNDER)) {
            double chance = plugin.getConfigManager().getTalismanDouble(TalismanType.THUNDER, "chance", 20.0);
            if (random.nextDouble() * 100.0 <= chance) {
                double extraDamage = plugin.getConfigManager().getTalismanDouble(TalismanType.THUNDER, "extra-damage", 3.0);
                event.setDamage(event.getDamage() + extraDamage);

                event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());

                Particle particle = plugin.getConfigManager().getTalismanParticle(TalismanType.THUNDER, Particle.ELECTRIC_SPARK);
                Sound sound = plugin.getConfigManager().getTalismanSound(TalismanType.THUNDER, Sound.ENTITY_LIGHTNING_BOLT_THUNDER);

                event.getEntity().getWorld().spawnParticle(particle, event.getEntity().getLocation().add(0, 1.0, 0), 20, 0.4, 0.6, 0.4, 0.1);
                attacker.playSound(attacker.getLocation(), sound, 0.7f, 1.0f);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && fallImmunePlayers.contains(victim.getUniqueId())) {
            event.setCancelled(true);
            fallImmunePlayers.remove(victim.getUniqueId());
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (victim.getHealth() - event.getFinalDamage() <= 0.0) {
            if (plugin.getTalismanManager().hasTalisman(victim, TalismanType.PHOENIX)) {
                if (!plugin.getCooldownManager().hasCooldown(victim.getUniqueId(), TalismanType.PHOENIX)) {
                    event.setCancelled(true);

                    int cooldown = plugin.getConfigManager().getTalismanInt(TalismanType.PHOENIX, "cooldown", 180);
                    plugin.getCooldownManager().setCooldown(victim.getUniqueId(), TalismanType.PHOENIX, cooldown);

                    victim.setHealth(4.0);

                    victim.removePotionEffect(PotionEffectType.POISON);
                    victim.removePotionEffect(PotionEffectType.WITHER);
                    victim.removePotionEffect(PotionEffectType.BLINDNESS);
                    victim.setFireTicks(0);

                    int regenSec = plugin.getConfigManager().getTalismanInt(TalismanType.PHOENIX, "regen-duration", 5);
                    int absSec = plugin.getConfigManager().getTalismanInt(TalismanType.PHOENIX, "absorption-duration", 10);

                    victim.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenSec * 20, 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, absSec * 20, 1));
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 15, 0));

                    Sound sound = plugin.getConfigManager().getTalismanSound(TalismanType.PHOENIX, Sound.ITEM_TOTEM_USE);
                    Particle particle = plugin.getConfigManager().getTalismanParticle(TalismanType.PHOENIX, Particle.FLAME);

                    victim.getWorld().playSound(victim.getLocation(), sound, 1.0f, 1.0f);
                    victim.getWorld().spawnParticle(particle, victim.getLocation().add(0, 1.0, 0), 40, 0.5, 1.0, 0.5, 0.08);

                    victim.sendMessage(plugin.getConfigManager().getMessage("phoenix-saved"));
                    return;
                }
            }
        }

        double futureHealth = victim.getHealth() - event.getFinalDamage();
        if (futureHealth > 0.0) {
            AttributeInstance maxAttr = victim.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            double maxHealth = (maxAttr != null) ? maxAttr.getValue() : 20.0;
            double healthPercent = (futureHealth / maxHealth) * 100.0;

            double threshold = plugin.getConfigManager().getTalismanDouble(TalismanType.TITAN, "hp-threshold", 30.0);
            if (healthPercent <= threshold) {
                if (plugin.getTalismanManager().hasTalisman(victim, TalismanType.TITAN)) {
                    if (!plugin.getCooldownManager().hasCooldown(victim.getUniqueId(), TalismanType.TITAN)) {
                        int cooldown = plugin.getConfigManager().getTalismanInt(TalismanType.TITAN, "cooldown", 60);
                        int duration = plugin.getConfigManager().getTalismanInt(TalismanType.TITAN, "duration", 6);

                        plugin.getCooldownManager().setCooldown(victim.getUniqueId(), TalismanType.TITAN, cooldown);

                        victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration * 20, 1));

                        Sound sound = plugin.getConfigManager().getTalismanSound(TalismanType.TITAN, Sound.BLOCK_ANVIL_LAND);
                        Particle particle = plugin.getConfigManager().getTalismanParticle(TalismanType.TITAN, Particle.ENCHANTMENT_TABLE);

                        victim.getWorld().playSound(victim.getLocation(), sound, 0.8f, 1.0f);
                        victim.getWorld().spawnParticle(particle, victim.getLocation().add(0, 1.0, 0), 30, 0.5, 0.8, 0.5, 0.1);

                        victim.sendMessage(plugin.getConfigManager().getMessage("titan-proc"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        TalismanType type = plugin.getTalismanManager().getTalismanType(event.getItem());

        if (type == TalismanType.WIND_DASH) {
            event.setCancelled(true);

            if (plugin.getCooldownManager().hasCooldown(player.getUniqueId(), TalismanType.WIND_DASH)) {
                long remaining = plugin.getCooldownManager().getRemainingSeconds(player.getUniqueId(), TalismanType.WIND_DASH);
                String msg = plugin.getConfigManager().getMessage("cooldown-active")
                        .replace("%time%", String.valueOf(remaining));
                player.sendMessage(msg);
                return;
            }

            int cooldown = plugin.getConfigManager().getTalismanInt(TalismanType.WIND_DASH, "cooldown", 15);
            plugin.getCooldownManager().setCooldown(player.getUniqueId(), TalismanType.WIND_DASH, cooldown);

            double power = plugin.getConfigManager().getTalismanDouble(TalismanType.WIND_DASH, "power", 1.6);
            double upPower = plugin.getConfigManager().getTalismanDouble(TalismanType.WIND_DASH, "up-power", 0.35);

            Vector dir = player.getLocation().getDirection().normalize();
            Vector velocity = dir.multiply(power);
            velocity.setY(Math.max(velocity.getY(), upPower));
            player.setVelocity(velocity);

            fallImmunePlayers.add(player.getUniqueId());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> fallImmunePlayers.remove(player.getUniqueId()), 200L);

            Sound sound = plugin.getConfigManager().getTalismanSound(TalismanType.WIND_DASH, Sound.ENTITY_PHANTOM_FLAP);
            Particle particle = plugin.getConfigManager().getTalismanParticle(TalismanType.WIND_DASH, Particle.CLOUD);

            player.getWorld().playSound(player.getLocation(), sound, 0.8f, 1.2f);
            player.getWorld().spawnParticle(particle, player.getLocation().add(0, 0.5, 0), 25, 0.4, 0.3, 0.4, 0.05);

            player.sendMessage(plugin.getConfigManager().getMessage("wind-dash"));
        }
    }
}
