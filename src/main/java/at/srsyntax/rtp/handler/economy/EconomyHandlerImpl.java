package at.srsyntax.rtp.handler.economy;

import at.srsyntax.rtp.api.handler.economy.EconomyHandler;
import at.srsyntax.rtp.api.handler.HandlerException;
import at.srsyntax.rtp.api.location.TeleportLocation;
import lombok.AllArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

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
public class EconomyHandlerImpl implements EconomyHandler {

  private final Economy economy;
  private final TeleportLocation teleportLocation;
  private final Player player;

  @Override
  public void handle() throws HandlerException {
    if (economy == null || teleportLocation.getPrice() <= 0 || canBypass()) return;
    final EconomyResponse response = economy.withdrawPlayer(player, teleportLocation.getPrice());
    if (response.type != EconomyResponse.ResponseType.SUCCESS)
      throw new HandlerException(response.errorMessage);
  }

  @Override
  public boolean canBypass() {
    return canBypass(player, "syntaxrtp.buy.bypass", teleportLocation.getName());
  }
}
