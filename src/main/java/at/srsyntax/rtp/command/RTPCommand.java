package at.srsyntax.rtp.command;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.API;
import at.srsyntax.rtp.api.Message;
import at.srsyntax.rtp.api.handler.cooldown.CooldownHandler;
import at.srsyntax.rtp.api.handler.countdown.CountdownCallback;
import at.srsyntax.rtp.api.handler.countdown.CountdownHandler;
import at.srsyntax.rtp.api.handler.economy.EconomyHandler;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PluginConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
public class RTPCommand implements CommandExecutor, TabCompleter {

  private final API api;
  private final String defaultLocation;
  private final PluginConfig pluginConfig;
  private final MessageConfig messageConfig;

  public RTPCommand(API api, PluginConfig pluginConfig) {
    this.api = api;
    this.defaultLocation = pluginConfig.getDefaultLocation();
    this.pluginConfig = pluginConfig;
    this.messageConfig = pluginConfig.getMessage();
  }

  @Override
  public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    try {
      checkAllowedToUse(commandSender);
      final Player player = (Player) commandSender;

      final TeleportLocation location = api.getLocation(strings.length > 0 ? strings[0] : defaultLocation);
      if (location == null) throw new Exception(messageConfig.getLocationNotFound());

      final CooldownHandler cooldownHandler = api.newCooldownHandler(location, player);
      final EconomyHandler economyHandler = api.newEconomyHandler(location, player);

      cooldownHandler.handle();
      economyHandler.handle();
      api.newCountdownHandler(location, player, newCountdownCallback(location, player, cooldownHandler, economyHandler)).handle();

      return true;
    } catch (Exception e) {
      new Message(e.getMessage(), pluginConfig).send(commandSender);
    }
    return false;
  }

  @Nullable
  @Override
  public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    try {
      checkAllowedToUse(commandSender);

      if (strings.length == 1) {
        final String arg = strings[0];
        final List<String> result = new ArrayList<>();

        for (TeleportLocation location : api.getLocationsCopy()) {
          if (location.hasPermission(commandSender) && location.getName().toLowerCase().startsWith(arg.toLowerCase()))
            result.add(location.getName());
        }

        return result;
      }
    } catch (Exception ignored) {}
    return new ArrayList<>();
  }

  private void checkAllowedToUse(CommandSender sender) throws Exception {
    if (!(sender instanceof Player)) throw new Exception("&cOnly a player can execute this command.");
    if (sender.hasPermission(RTPPlugin.ADMIN_PERMISSION) || sender.hasPermission("syntaxrtp.use")) return;
    throw new Exception(messageConfig.getNoPermission());
  }

  private CountdownCallback newCountdownCallback(TeleportLocation teleportLocation, Player player, CooldownHandler cooldownHandler, EconomyHandler economyHandler) {
    return new CountdownCallback() {
      @Override
      public void done() {
        teleportLocation.teleport(player);
        new Message(messageConfig.getTeleported(), pluginConfig)
            .add("location", teleportLocation.getName())
            .add("player", player.getName())
            .send(player);
      }

      @Override
      public void canceled() {
        cooldownHandler.removeCooldown();
        economyHandler.refund();
      }
    };
  }
}
