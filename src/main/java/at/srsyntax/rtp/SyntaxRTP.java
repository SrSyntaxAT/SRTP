package at.srsyntax.rtp;

import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.countdown.Callback;
import at.srsyntax.rtp.api.countdown.Countdown;
import at.srsyntax.rtp.api.event.PlayerRandomTeleportEvent;
import at.srsyntax.rtp.api.exception.LocationNotFound;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.api.exception.TeleportException;
import at.srsyntax.rtp.api.message.MessageType;
import at.srsyntax.rtp.api.teleport.Cooldown;
import at.srsyntax.rtp.api.teleport.Radius;
import at.srsyntax.rtp.api.teleport.TeleportLocation;
import at.srsyntax.rtp.command.RTPCommand;
import at.srsyntax.rtp.command.ReloadCommand;
import at.srsyntax.rtp.config.LocationConfig;
import at.srsyntax.rtp.config.exception.ConfigLoadException;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PermissionConfig;
import at.srsyntax.rtp.config.PluginConfig;
import at.srsyntax.rtp.impl.CountdownImpl;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
 * Copyright (c) 2021 Marcel Haberl
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
public class SyntaxRTP extends JavaPlugin implements API {
  
  public static final String METADATA_COOLDOWN_KEY = "srtpc:#";
  public static final int RESOURCE_ID = 99428;

  private static SyntaxRTP instance;

  private Metrics metrics;
  private Economy economy = null;

  private PluginConfig pluginConfig;
  private boolean usePrefix = true;

  @Override
  public void onEnable() {
    try {
      instance = this;
      
      reload();
      metrics = new Metrics(this, 13408);
      setupEconomy();

      if (!SpigotVersionCheck.check(getDescription().getVersion(), RESOURCE_ID))
        getLogger().warning("A newer version is available!");
    } catch (Exception exception) {
      exception.printStackTrace();
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }
  
  @Override
  public void reload() throws Exception {
    pluginConfig = loadConfig();
    setUsingPrefix(pluginConfig.isUsePrefix());
    registerCommand();

    if (pluginConfig.isPaperAsync() && !getServer().getName().equalsIgnoreCase("Paper"))
      getLogger().warning("Paper async teleport was enabled, but does not run on a paper server.");
  }

  private void setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) return;

    final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
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
        Bukkit.getScheduler().runTask(SyntaxRTP.instance, () -> {
          final Location oldLocation = player.getLocation();
          Bukkit.getPluginManager().callEvent(new PlayerRandomTeleportEvent(player, oldLocation, location, PlayerTeleportEvent.TeleportCause.PLUGIN));
          
          if (pluginConfig.isPaperAsync()) {
            try {
              player.teleportAsync(location).get();
            } catch (InterruptedException | ExecutionException e) {
              e.printStackTrace();
            }
          }
          else
            player.teleport(location);
          player.setMetadata(METADATA_COOLDOWN_KEY.replace("#", key), new FixedMetadataValue(SyntaxRTP.instance, System.currentTimeMillis()));
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
    final MessageConfig messageConfig = pluginConfig.getMessages();
  
    checkCooldown(target, location.getCooldown(), messageConfig);
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
    if (!target.isOnline())
      throw new TeleportException(pluginConfig.getMessages().getPlayerNotFound());

    if (price > economy.getBalance(target))
      throw new TeleportException(pluginConfig.getMessages().getNotEnoughtMoney());

    economy.withdrawPlayer(target, price);
  }
  
  private boolean hasCountdown(Player player, TeleportLocation location) {
    return
      location.getCountdown() > 0 &&
        (!player.hasPermission(pluginConfig.getPermissions().getTeleportCountdownBypass())
      || !player.hasPermission(pluginConfig.getPermissions().getTeleportCountdownBypass().replace("*", location.getName())));
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
    return new CountdownImpl(time, callback, this);
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
    for (LocationConfig config : pluginConfig.getLocations()) {
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
    if (command.equalsIgnoreCase("srtp"))
      return pluginConfig.getDefaultLocation();
    
    for (String alias : pluginConfig.getAliases()) {
      if (alias.toLowerCase().startsWith(command.toLowerCase())) {
        final String[] splited = alias.split(" ");
      
        if (splited.length >= 2)
          return splited[1];
        else
          return pluginConfig.getDefaultLocation();
      }
    }
    return null;
  }
  
  private boolean hasBypassPermission(Player player, String key) {
    return player.hasPermission(pluginConfig.getPermissions().getTeleportCooldownBypass())
      || player.hasPermission(pluginConfig.getPermissions().getTeleportCooldownBypass().replace("*", key));
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
    return usePrefix;
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

  private void registerCommand() throws NoSuchFieldException, IllegalAccessException {
    final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
    field.setAccessible(true);
    final SimpleCommandMap commandMap = (SimpleCommandMap) field.get(Bukkit.getPluginManager());
    
    final Command command = commandMap.getCommand(getName());
    if (command != null)
      commandMap.getCommands().remove(command);
    
    commandMap.register(getName(), new RTPCommand(this, pluginConfig, getCommandAliases()));
    commandMap.register(getName(), new ReloadCommand(this));
    
    field.setAccessible(false);
  }
  
  private List<String> getCommandAliases() {
    final List<String> list = new ArrayList<>(pluginConfig.getAliases().length);
    
    for (String alias : pluginConfig.getAliases()) {
      final String[] splited = alias.split(" ");
      if (splited.length > 0)
        list.add(splited[0]);
    }
    
    return list;
  }
  
  public void sync(Runnable runnable) {
    Bukkit.getScheduler().runTask(this, runnable);
  }

  private PluginConfig loadConfig() throws ConfigLoadException {
    if (!getDataFolder().exists()) getDataFolder().mkdirs();
    return PluginConfig.load(
        new PluginConfig(
            false,
            true,
            new String[]{"randomteleport", "rtp", "farming farming_world_2"},
          "farming_world",
          new LocationConfig[]{
              new LocationConfig(
                "farming_world",
                "world", 0D, 0D,
                  700D, new Radius(150, 3000),
                (short) 120, (short) 5
              ),
              new LocationConfig(
                "farming_world_2",
                "world", 6000D, 6000D,
                  1000D, new Radius(150, 3000),
                (short) 120, (short) 5
              )
            },
            new PermissionConfig(
                "srtp.command.teleport",
                "srtp.command.teleport.other",
                "srtp.command.teleport.countdown.bypass.*",
                "srtp.command.teleport.cooldown.bypass.*",
              "srtp.command.reload"
            ),
            new MessageConfig(
                new String[]{
                    "5,3,1;" + MessageType.TITLE.name() + ";&eYou will teleport in &6<time>&e.",
                    "0;" + MessageType.TITLE.name() + ";&aYou have been teleported!"
                },
                "&7[&bSyntaxRTP&7] &r",
                MessageType.ACTIONBAR.name() + ";&aYou have been teleported!",
                MessageType.CHAT.name() + ";&aYou have teleported &e<player>&a.",
                MessageType.ACTIONBAR.name() + ";&cYou can use the command again only in &e<time>&c!",
                MessageType.CHAT.name() + ";&cYou are not authorized to do that! :/",
                MessageType.CHAT.name() + ";&cAn error occurred while teleporting! :c",
                MessageType.CHAT.name() + ";&cPlayer was not found!",
              MessageType.CHAT.name() + ";&cSyntaxRTP will be reloaded!",
              MessageType.CHAT.name() + ";&4An error occurred during reload!!",
                MessageType.ACTIONBAR.name() + ";&4You do not have enough money!",
                "second", "seconds"
            )
        ),
        new File(getDataFolder(), "config.json")
    );
  }

  public static API getAPI() {
    return instance;
  }

  public PluginConfig getPluginConfig() {
    return pluginConfig;
  }
}
