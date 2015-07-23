package com.github.Kraken3.AFKPGC.commands;

import java.util.List;

import com.github.Kraken3.AFKPGC.AFKPGC;

import org.bukkit.command.CommandSender;

public abstract class AbstractCommand {

	protected final AFKPGC plugin;
	protected final String name;

	public AbstractCommand(AFKPGC instance, String commandName) {
		plugin = instance;
		name = commandName;
	}

	public abstract boolean onCommand(CommandSender sender, List<String> args);

	public boolean onConsoleCommand(CommandSender sender, List<String> args) {
		return onCommand(sender, args);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		try {
			return plugin.getCommand("afkpgc " + name).getDescription();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public String getUsage() {
		try {
			return plugin.getCommand("afkpgc " + name).getUsage();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public String getPermission() {
		try {
			return plugin.getCommand("afkpgc " + name).getPermission();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public List<String> getAliases() {
		try {
			return plugin.getCommand("afkpgc " + name).getAliases();
		} catch (NullPointerException e) {
			return null;
		}
	}
}
