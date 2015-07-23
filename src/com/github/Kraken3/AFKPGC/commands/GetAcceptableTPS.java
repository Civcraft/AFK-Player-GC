package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.BotDetector;

public class GetAcceptableTPS extends AbstractCommand {

	public GetAcceptableTPS(AFKPGC instance) {
		super(instance, "getacceptabletps");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		sender.sendMessage("The current acceptable TPS value for the bot detector is: "
				+ BotDetector.acceptableTPS);
		return true;
	}

}
