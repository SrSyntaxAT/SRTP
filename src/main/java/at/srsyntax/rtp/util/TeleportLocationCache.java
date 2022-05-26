package at.srsyntax.rtp.util;

import at.srsyntax.rtp.RTPPlugin;
import at.srsyntax.rtp.api.location.TeleportLocation;
import at.srsyntax.rtp.api.location.LocationCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;

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
@AllArgsConstructor
@Getter
public class TeleportLocationCache implements TeleportLocation {

  private final String name;
  private final LocationCache location;

  private final String permission;
  private final int size, countdown, cooldown;
  private final double price;

  private final String[] aliases;

  private transient final LinkedList<LocationCache> locationCaches = new LinkedList<>();

  @Override
  public boolean hasPermission(CommandSender sender) {
    if (permission == null) return true;
    final String prefix = "syntaxrtp.teleport.";
    return sender.hasPermission(RTPPlugin.ADMIN_PERMISSION) || sender.hasPermission(prefix + "*") || sender.hasPermission(prefix + getName());
  }

  @Override
  public void teleport(Player player) {
    RTPPlugin.getApi().teleport(player, this);
  }

}
