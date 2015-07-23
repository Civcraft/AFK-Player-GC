package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class SetCriticalTPSChange extends AbstractCommand {

	public SetCriticalTPSChange(AFKPGC instance) {
		super(instance, "setcriticaltpschange");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		if (args.size() == 1) {
			try {
				double a = Double.parseDouble(args.get(0));
				BotDetector.criticalTPSChange = (float)a;
				sender.sendMessage("Set critical TPS change to "+a);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}

		}
		return false;
	}

}
