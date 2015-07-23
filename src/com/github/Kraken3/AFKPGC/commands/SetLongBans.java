package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class SetLongBans extends AbstractCommand {
	public SetLongBans(AFKPGC instance) {
		super(instance, "setlongbans");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		if (args.size() == 1) {
			if (args.get(0).matches("true")) {
				BotDetector.longBans = true;
				sender.sendMessage("Set long bans to true");
				return true;
			}
			if (args.get(0).matches("false")) {
				BotDetector.longBans = false;
				sender.sendMessage("Set long bans to false");
				return true;
			}
		}
		return false;
	}

}
