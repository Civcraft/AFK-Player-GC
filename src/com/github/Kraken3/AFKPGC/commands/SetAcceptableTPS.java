package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class SetAcceptableTPS extends AbstractCommand {

	public SetAcceptableTPS(AFKPGC instance) {
		super(instance, "setacceptabletps");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		if (args.size() == 1) {
			try {
				int a = Integer.parseInt(args.get(0));
				BotDetector.acceptableTPS = a;
				sender.sendMessage("Set acceptable TPS to " + a);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}

		}
		return false;
	}

}