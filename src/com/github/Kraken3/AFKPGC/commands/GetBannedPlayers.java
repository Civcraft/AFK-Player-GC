package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class GetBannedPlayers extends AbstractCommand {

	public GetBannedPlayers(AFKPGC instance) {
		super(instance, "getbannedplayers");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		String s = "";
		for (int i = 0; i < BotDetector.bannedPlayers.size(); i++) {
			s = s + BotDetector.bannedPlayers.get(i) + ", ";
		}
		if (s.length() != 0) {
			s = s.substring(0, s.length() - 1); // remove last comma
		}
		sender.sendMessage("Currently banned by the botdetector are: " + s);
		return true;
	}

}
