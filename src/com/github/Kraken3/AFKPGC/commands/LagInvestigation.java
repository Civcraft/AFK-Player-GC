package com.github.Kraken3.AFKPGC.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.LagScanner;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.apache.commons.lang.mutable.MutableLong;

public class LagInvestigation extends AbstractCommand {

	public LagInvestigation(AFKPGC instance) {
		super(instance, "investigate");
	}

	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			MutableLong chunkWeight = new MutableLong(0L);
			Location nextUp = LagScanner.getNextBadChunk(chunkWeight, true);
			if (nextUp != null) {
				sender.sendMessage("Teleporting you to an uninvestigated Lag area (weight " + chunkWeight.longValue() + "): " + nextUp.toString());
				if (p.isInsideVehicle()) {
					p.leaveVehicle();
				}
				p.teleport(nextUp);
			} else {
				sender.sendMessage("Nothing left to investigate");
			}
		} else {
			sender.sendMessage("Can only invoke if a player in game");
		}
		return true;
	}

}

