package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;

public class Stop extends AbstractCommand {
	public Stop(AFKPGC instance) {
		super(instance, "stop");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		AFKPGC.enabled=false;
		sender.sendMessage("Plugin disabled");
		return true;
	}

}
