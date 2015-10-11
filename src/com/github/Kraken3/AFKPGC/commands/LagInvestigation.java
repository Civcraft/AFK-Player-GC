package com.github.Kraken3.AFKPGC.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.LagScanner;

import org.bukkit.Location;

import org.apache.commons.lang.mutable.MutableLong;

public class LagInvestigation extends AbstractCommand {

	public LagInvestigation(AFKPGC instance) {
		super(instance, "investigate");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		MutableLong chunkWeight = new MutableLong(0L);
		Location nextUp = LagScanner.getNextBadChunk(chunkWeight, true);
		sender.sendMessage("Teleporting you to an uninvestigated Lag area (weight " + chunkWeight.longValue() + "): " + nextUp.toString());
		return true;
	}

}

