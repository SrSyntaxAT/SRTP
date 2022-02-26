package at.srsyntax.rtp.impl;

import at.srsyntax.rtp.SyntaxRTP;
import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.countdown.Callback;
import at.srsyntax.rtp.api.countdown.Countdown;
import at.srsyntax.rtp.api.event.PlayerRandomTeleportEvent;
import at.srsyntax.rtp.api.exception.LocationNotFound;
import at.srsyntax.rtp.api.exception.TeleportException;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.api.teleport.Cooldown;
import at.srsyntax.rtp.api.teleport.Radius;
import at.srsyntax.rtp.api.teleport.TeleportLocation;
import at.srsyntax.rtp.config.LocationConfig;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PluginConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/*
 * MIT License
 *
 * Copyright (c) 2022 Marcel Haberl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class APIImpl implements API {

  public static final String METADATA_COOLDOWN_KEY = "srtpc:#";
  private final SyntaxRTP plugin;
  private boolean usePrefix = true;
  private Economy economy = null;

  public APIImpl(SyntaxRTP plugin) {
    this.plugin = plugin;
  }

  @Override
  public void reload() throws Exception {
    plugin.setPluginConfig(plugin.loadConfig());
    setUsingPrefix(plugin.getPluginConfig().isUsePrefix());
    plugin.registerCommand();

    if (plugin.getPluginConfig().isPaperAsync() && !Bukkit.getName().equalsIgnoreCase("Paper"))
      plugin.getLogger().warning("Paper async teleport was enabled, but does not run on a paper server.");
  }

  public void setupEconomy() {
    if (Bukkit.getPluginManager().getPlugin("Vault") == null) return;

    final RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
    if (rsp == null) return;

    economy = rsp.getProvider();
  }

  @Override
  public boolean vaultSupported() {
    return economy != null;
  }

  private void registerMessagesForCountdown(Countdown countdown, String[] messages, String prefix) {
    for (String line : messages) {
      final String[] splited = line.split(";"), timestamps = splited[0].split(",");
      final Message message = new Message(splited).prefix(prefix);
      try {
        for (String timestamp : timestamps)
          countdown.addMessage(Integer.parseInt(timestamp), message);
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  private void checkCooldown(Player target, Cooldown cooldown, MessageConfig messageConfig) {
    if (cooldown != null) {
      final long cooldownTime = TimeUnit.MILLISECONDS.toSeconds(hasCooldown(target, cooldown));
      if (cooldown.getTime() > 0 && cooldownTime > 0) {
        final String unit = cooldownTime > 1 ? messageConfig.getSeconds() : messageConfig.getSecond();
        final Map<String, String> replaces = Collections.singletonMap("<time>", cooldownTime + " " + unit);
        throw new TeleportException(Message.replace(messageConfig.getTeleportCooldownError(), replaces));
      }
    }
  }

  private Callback createCallback(Player target, Location location, String key) {
    return new Callback() {
      @Override
      public void done(Player player, Location location) {
        Bukkit.getScheduler().runTask(plugin, () -> {
          final Location oldLocation = player.getLocation();
          Bukkit.getPluginManager().callEvent(new PlayerRandomTeleportEvent(player, oldLocation, location, PlayerTeleportEvent.TeleportCause.PLUGIN));

          if (plugin.getPluginConfig().isPaperAsync()) {
            try {
              player.teleportAsync(location).get();
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          }
          else
            player.teleport(location);
          player.setMetadata(METADATA_COOLDOWN_KEY.replace("#", key), new FixedMetadataValue(plugin, System.currentTimeMillis()));
        });
      }

      @Override
      public Player getPlayer() {
        return target;
      }

      @Override
      public Location getLocation() {
        return location;
      }
    };
  }

  @Override
  public boolean teleport(@NotNull Player target, @NotNull Location location, double costs, @NotNull Radius radius) throws TeleportException {
    return teleport(target, location, costs, radius, 0);
  }

  @Override
  public boolean teleport(@NotNull Player target, @NotNull Location location, double costs, @NotNull Radius radius, int countdown) throws TeleportException {
    return teleport(target, location, costs, radius, null, countdown);
  }

  @Override
  public boolean teleport(@NotNull Player target, @NotNull Location location, double costs, @NotNull Radius radius, Cooldown cooldown, int countdown) throws TeleportException {
    final TeleportLocation teleportLocation = constructLocation(
        cooldown == null ? "default" : cooldown.getKey(),
        location, costs, radius,
        cooldown == null ? (short) 0 : cooldown.getTime(), (short) 0
    );

    return teleport(target, teleportLocation);
  }

  @Override
  public boolean teleport(@NotNull Player target, @NotNull TeleportLocation location) throws TeleportException {
    final MessageConfig messageConfig = plugin.getPluginConfig().getMessages();

    checkCooldown(target, location.getCooldown(), messageConfig);
    if (vaultSupported())
      buyTeleport(target, location.getPrice());
    final Location newLocation = randomLocation(location);
    final Callback callback = createCallback(target, newLocation, location.getName());

    if (hasCountdown(target, location)) {
      final Countdown countdown = createCountdown(location.getCountdown(), callback);
      registerMessagesForCountdown(countdown, messageConfig.getTeleportCountdown(), messageConfig.getPrefix());
      countdown.start();
      return true;
    } else {
      callback.done(target, newLocation);
      return false;
    }
  }

  private void buyTeleport(Player target, double price) throws TeleportException {
    if (price == 0) return;

    final MessageConfig messageConfig = plugin.getPluginConfig().getMessages();

    if (!target.isOnline())
      throw new TeleportException(messageConfig.getPlayerNotFound());

    if (price > economy.getBalance(target))
      throw new TeleportException(messageConfig.getNotEnoughtMoney());

    economy.withdrawPlayer(target, price);
  }

  private boolean hasCountdown(Player player, TeleportLocation location) {
    final String permission = plugin.getPluginConfig().getPermissions().getTeleportCountdownBypass();
    return
        location.getCountdown() > 0 &&
            (!player.hasPermission(permission)
                || !player.hasPermission(permission.replace("*", location.getName())));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull Location location, double costs, @NotNull Radius radius) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, costs, radius));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull Location location, double costs, @NotNull Radius radius, int countdown) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, costs, radius, countdown));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull Location location, double costs, @NotNull Radius radius, Cooldown cooldown, int countdown) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location, costs, radius, cooldown, countdown));
  }

  @Override
  public CompletableFuture<Boolean> teleportAsync(@NotNull Player target, @NotNull TeleportLocation location) throws TeleportException {
    return CompletableFuture.supplyAsync(() -> teleport(target, location));
  }

  @Override
  public Countdown createCountdown(int time, @NotNull Callback callback) {
    return new CountdownImpl(time, callback, plugin);
  }

  @Override
  public TeleportLocation constructLocation(@NotNull String name, @NotNull Location location, double costs, @NotNull Radius radius, short cooldown, short countdown) {
    return constructLocation(name, location.getWorld().getName(), location.getX(), location.getZ(), costs, radius, cooldown, countdown);
  }

  @Override
  public TeleportLocation constructLocation(@NotNull String name, @NotNull String world, double x, double z, double costs, @NotNull Radius radius, short cooldown, short countdown) {
    return new LocationConfig(name, world, x, z, costs, radius, cooldown, countdown);
  }

  @Override
  public TeleportLocation getLocation(@NotNull String name) throws LocationNotFound {
    for (LocationConfig config : plugin.getPluginConfig().getLocations()) {
      if (config.getName().equalsIgnoreCase(name))
        return config;
    }

    throw new LocationNotFound("Location " + name + " not found!");
  }

  @Override
  public TeleportLocation getCommandAliasLocation(@NotNull String command) throws LocationNotFound {
    final String name = filterAliases(command);

    if (name == null)
      throw new LocationNotFound("Location not found!");

    return getLocation(name);
  }

  private String filterAliases(String command) {
    final PluginConfig config = plugin.getPluginConfig();

    if (command.equalsIgnoreCase("srtp"))
      return config.getDefaultLocation();

    for (String alias : config.getAliases()) {
      if (alias.toLowerCase().startsWith(command.toLowerCase())) {
        final String[] splited = alias.split(" ");

        if (splited.length >= 2)
          return splited[1];
        else
          return config.getDefaultLocation();
      }
    }
    return null;
  }

  private boolean hasBypassPermission(Player player, String key) {
    final String permission = plugin.getPluginConfig().getPermissions().getTeleportCooldownBypass();
    return player.hasPermission(permission)
        || player.hasPermission(permission.replace("*", key));
  }

  @Override
  public long hasCooldown(@NotNull Player player, @NotNull Cooldown cooldown) {
    if (cooldown.getTime() > 0 && !hasBypassPermission(player, cooldown.getKey())) {
      final String key = METADATA_COOLDOWN_KEY.replace("#", cooldown.getKey());
      if (!player.hasMetadata(key)) return 0;

      final List<MetadataValue> list = player.getMetadata(key);
      if (list.isEmpty()) return 0;

      final long value = list.get(0).asLong(), result;
      result = value + TimeUnit.SECONDS.toMillis(cooldown.getTime()) - System.currentTimeMillis();
      return result < 0 ? 0 : result;
    }
    return 0;
  }

  @Override
  public long hasCooldown(@NotNull Player player, @NotNull TeleportLocation location) {
    return hasCooldown(player, location.getCooldown());
  }

  @Override
  public Location randomLocation(@NotNull Location center, @NotNull Radius radius) {
    boolean nether = center.getWorld().getEnvironment() == World.Environment.NETHER;
    int x, y, z;

    do {
      x = random(center.getBlockX(), radius.getMin(), radius.getMax());
      z = random(center.getBlockZ(), radius.getMin(), radius.getMax());
      y = center.getWorld().getHighestBlockYAt(x, z);

      if (nether) {

        while (y != 0) {
          y = findBlockAtY(center, x, y, z, true);

          y--;
          final Block block = getBlock(center, x, y, z);
          if (!block.getType().isAir())
            continue;

          y = findBlockAtY(center, x, y, z, false) + 1;
          break;
        }
      } else if (y != 0) {
        y++;
      }
    } while (y == 0);

    return new Location(center.getWorld(), x, y, z, center.getYaw(), center.getPitch());
  }

  @Override
  public Location randomLocation(@NotNull TeleportLocation location) {
    return randomLocation(location.toLocation(), location.getRadius());
  }

  @Override
  public boolean isUsingPrefix() {
    return plugin.getPluginConfig().isUsePrefix();
  }

  @Override
  public void setUsingPrefix(boolean using) {
    this.usePrefix = using;
  }

  private Block getBlock(Location location, int x, int y, int z) {
    return location.getWorld().getBlockAt(x, y, z);
  }

  private int findBlockAtY(Location location, int x, int y, int z, boolean air) {
    Block block;
    do {
      block = getBlock(location, x, y, z);
      if (block.getType().isAir() == air) break;
      y--;
    } while (y > 0);
    return y;
  }


  private int random(int current, int min, int radius) {
    boolean finished = false;
    int result;

    do {
      result = ThreadLocalRandom.current().nextInt(current - radius, current + radius);
      if (allowedResult(result, current, min, radius))
        finished = true;
    } while (!finished);

    return result;
  }

  private boolean allowedResult(int result, int current, int min, int radius) {
    return isHigherAllowed(result, current, min, radius) || isLowerAllowed(result,current, min, radius);
  }

  private boolean isHigherAllowed(int result, int current, int min, int radius) {
    return (result > current && result > current + min) && result <= radius;
  }

  private boolean isLowerAllowed(int result, int current, int min, int radius) {
    return (result < current && result < current - min) && result >= radius;
  }

}
