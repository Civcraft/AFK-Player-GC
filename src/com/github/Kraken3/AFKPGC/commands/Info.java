package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;

public class Info extends AbstractCommand{
	public Info(AFKPGC instance) {
		super(instance,"info");
	}
	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		sender.sendMessage("AFKPGC version: "+AFKPGC.plugin.getDescription().getVersion()+", the plugin is "+(AFKPGC.enabled ? "enabled" : "disabled"));
		return true;
	}
}
