package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class getLongBans extends AbstractCommand {

	public getLongBans(AFKPGC instance) {
		super(instance, "getLongBans");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		if (BotDetector.longBans)
			sender.sendMessage("Long bans are currently enabled in the bot detector");
		else
			sender.sendMessage("Long bans are currently not enabled in the bot detector");
		return true;
	}
}
