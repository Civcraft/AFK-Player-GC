package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.ConfigurationReader;

public class Reload extends AbstractCommand{
	public Reload(AFKPGC instance) {
		super(instance, "reload");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		AFKPGC.enabled = ConfigurationReader.readConfig();
		if (AFKPGC.enabled)
			sender.sendMessage("New configuration accepted, plugin enabled");
		else
			sender.sendMessage("New configuration rejected, plugin disabled");
		return true;
		
		
		
	}
}
