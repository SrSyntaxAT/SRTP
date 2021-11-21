package at.srsyntax.rtp.command;

import at.srsyntax.rtp.SyntaxRTP;
import at.srsyntax.rtp.api.exception.LocationNotFound;
import at.srsyntax.rtp.api.exception.TeleportException;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.api.teleport.TeleportLocation;
import at.srsyntax.rtp.config.MessageConfig;
import at.srsyntax.rtp.config.PermissionConfig;
import at.srsyntax.rtp.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
public class RTPCommand extends Command {

  private final SyntaxRTP syntaxRTP;
  private final MessageConfig messageConfig;
  private final PermissionConfig permissionConfig;

  public RTPCommand(SyntaxRTP syntaxRTP, PluginConfig config, List<String> aliases) {
    super("srtp", "Teleport to a random location", "/srtp", aliases);
    this.syntaxRTP = syntaxRTP;
    this.messageConfig = config.getMessages();
    this.permissionConfig = config.getPermissions();
  }

  @Override
  public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
    final String prefix = messageConfig.getPrefix();

    if (strings.length == 0)
      return single(commandSender, s, prefix);
    else
      return other(commandSender, s, strings, prefix);
  }

  private boolean single(CommandSender commandSender, String label, String prefix) {
    if (!(commandSender instanceof Player)) {
      commandSender.sendMessage("§cuse srtp <player>");
      return false;
    }
    final Player target = (Player) commandSender;

    if (!target.hasPermission(permissionConfig.getTeleport())) {
      new Message(target, messageConfig.getNoPermission()).prefix(prefix).send();
      return false;
    }
  
    try {
      final TeleportLocation location = syntaxRTP.getCommandAliasLocation(label);
      syntaxRTP.teleportAsync(target, location).handle((aBoolean, throwable) -> {
        if (throwable == null) {
          if (!aBoolean)
            new Message(target, messageConfig.getTeleportedWithoutCountdown()).prefix(prefix).send();
        } else {
          new Message(target, throwable.getCause().getMessage()).prefix(prefix).send();
        }
        return true;
      });
    } catch (LocationNotFound e) {
      new Message(target, messageConfig.getTeleportError()).prefix(prefix).send();
      e.printStackTrace();
    }
    
    return false;
  }

  private boolean other(CommandSender commandSender, String label, String[] strings, String prefix) {
    final Player target = Bukkit.getPlayer(strings[0]);
    final Map<String, String> replaces = new HashMap<>();
    final boolean available = target != null && target.isOnline();

    if (available)
      replaces.put("<player>", target.getName());

    if (commandSender instanceof Player)
      return otherPlayer((Player) commandSender, target, label, available, replaces, prefix);
    return otherConsole(commandSender, target, label, available, replaces, prefix);
  }

  private boolean otherConsole(CommandSender commandSender, Player target, String label, boolean available, Map<String, String> replaces, String prefix) {
    if (!available) {
      commandSender.sendMessage(Message.replace(prefix, messageConfig.getPlayerNotFound(), null));
      return false;
    }
  
    try {
      final TeleportLocation location = syntaxRTP.getCommandAliasLocation(label);
      syntaxRTP.teleportAsync(target, location).handle((aBoolean, throwable) -> {
        if (throwable == null) {
          commandSender.sendMessage(Message.replace(prefix, messageConfig.getTeleportOther().split(";")[1], replaces));
          if (!aBoolean)
            new Message(target, messageConfig.getTeleportedWithoutCountdown()).prefix(prefix).send();
        } else {
          commandSender.sendMessage(Message.replace(prefix, throwable.getCause().getMessage(), null));
        }
        return true;
      });
    } catch (LocationNotFound e) {
      new Message(target, messageConfig.getTeleportError()).prefix(prefix).send();
      e.printStackTrace();
    }

    return false;
  }

  private boolean otherPlayer(Player sender, Player target, String label, boolean available, Map<String, String> replaces, String prefix) {
    if (!sender.hasPermission(permissionConfig.getTeleportOther())) {
      new Message(sender, messageConfig.getNoPermission()).prefix(prefix).send();
      return false;
    }

    if (!available) {
      new Message(sender, messageConfig.getPlayerNotFound()).prefix(prefix).send();
      return false;
    }
  
    try {
      final TeleportLocation location = syntaxRTP.getCommandAliasLocation(label);
      syntaxRTP.teleportAsync(target, location).handle((aBoolean, throwable) -> {
        if (throwable == null) {
          new Message(sender, messageConfig.getTeleportOther()).prefix(prefix).send(replaces);
          if (!aBoolean)
            new Message(target, messageConfig.getTeleportedWithoutCountdown()).prefix(prefix).send();
        } else {
          new Message(sender, throwable.getCause().getMessage()).prefix(messageConfig.getPrefix()).prefix(prefix).send();
        }
        return true;
      });
    } catch (LocationNotFound e) {
      new Message(target, messageConfig.getTeleportError()).prefix(prefix).send();
      e.printStackTrace();
    }
    
    return false;
  }
}
