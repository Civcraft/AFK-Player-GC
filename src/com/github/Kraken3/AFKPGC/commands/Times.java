package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.Kicker;

public class Times extends AbstractCommand {
	public Times(AFKPGC instance) {
		super(instance, "times");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		int[] kt = Kicker.kickThresholds;
		int ktlen = kt.length;
		int starti = -1;
		int stopi = -1;
		int val = -1;
		for (int i = 0; i < ktlen; i++) {
			if (val == kt[i]) {
				stopi = i;
			} else {
				if (starti != -1)
					if (val != 0) {
						sender.sendMessage(starti + 1 + " - " + stopi + 1
								+ " players online: Kicking after "
								+ CommandHandler.readableTimeSpan(val)
								+ " of being AFK");
					}
				starti = i;
				stopi = i;
			}
			val = kt[i];
		}
		if (val != 0) {
			sender.sendMessage(starti + 1 + " - " + stopi + 1
					+ " players online: Kicking after "
					+ CommandHandler.readableTimeSpan(val) + " of being AFK");
		}
		return true;
	}

}
