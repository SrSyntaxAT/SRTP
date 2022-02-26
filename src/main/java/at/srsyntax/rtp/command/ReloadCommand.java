package at.srsyntax.rtp.command;

import at.srsyntax.rtp.SyntaxRTP;
import at.srsyntax.rtp.api.message.Message;
import at.srsyntax.rtp.config.PluginConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/*
 * CONFIDENTIAL
 *  Unpublished Copyright (c) 2021 Sytonix, All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Sytonix. The intellectual and
 * technical concepts contained herein are proprietary to Sytonix and may be covered by U.S. and Foreign
 * Patents, patents in process, and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Sytonix.  Access to the source code contained herein is hereby forbidden to anyone without written
 * permission Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 * The copyright notice above does not evidence any actual or intended publication or disclosure  of  this source code,
 * which includes information that is confidential and/or proprietary, and is a trade secret, of Sytonix.
 * ANY REPRODUCTION, MODIFICATION, DISTRIBUTION, PUBLIC  PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE  OF THIS
 * SOURCE CODE  WITHOUT  THE EXPRESS WRITTEN CONSENT OF Sytonix IS STRICTLY PROHIBITED, AND IN VIOLATION OF
 * APPLICABLE LAWS AND INTERNATIONAL TREATIES.  THE RECEIPT OR POSSESSION OF  THIS SOURCE CODE AND/OR RELATED
 * INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE ITS CONTENTS, OR TO
 * MANUFACTURE, USE, OR SELL ANYTHING THAT IT  MAY DESCRIBE, IN WHOLE OR IN PART.
 */
public class ReloadCommand extends Command {

	private final PluginConfig config;
	
	public ReloadCommand(SyntaxRTP plugin) {
		super("srtpreload", "Reload the SRTP Plugin", "/srtpreload", Collections.emptyList());
		this.config = plugin.getPluginConfig();
	}
	
	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
		final String prefix = config.getMessages().getPrefix();
		
		if (sender.hasPermission(config.getPermissions().getReload())) {
			try {
				sendMessage(sender, config.getMessages().getReload(), prefix);
				SyntaxRTP.getAPI().reload();
			} catch (Exception exception) {
				sendMessage(sender, config.getMessages().getReloadError(), prefix);
				exception.printStackTrace();
			}
		} else {
			sendMessage(sender, config.getMessages().getNoPermission(), prefix);
		}
		return false;
	}
	
	private void sendMessage(CommandSender sender, String message, String prefix) {
		if (sender instanceof Player)
			new Message((Player) sender, message).prefix(prefix).send();
		else
			sender.sendMessage(Message.replace(prefix, message, null));
	}
}
