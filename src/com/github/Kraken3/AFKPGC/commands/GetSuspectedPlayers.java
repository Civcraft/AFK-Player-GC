package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class GetSuspectedPlayers extends AbstractCommand {

	public GetSuspectedPlayers(AFKPGC instance) {
		super(instance, "getsuspectedplayers");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		String s = "";
		for (int i = 0; i < BotDetector.suspectedBotters.size(); i++) {
			s = s + BotDetector.suspectedBotters.get(i) + ", ";
		}
		if (s.length() != 0) {
			s = s.substring(0, s.length() - 1); // remove last comma
		}
		sender.sendMessage("Currently suspected by the botdetector are: " + s);
		return true;
	}

}
