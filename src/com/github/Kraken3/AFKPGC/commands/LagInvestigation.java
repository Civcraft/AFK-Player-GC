package com.github.Kraken3.AFKPGC.commands;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.LagScanner;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

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
			final Location nextUp = LagScanner.getNextBadChunk(chunkWeight, true);
			if (nextUp != null) {
				sender.sendMessage("Teleporting you to an uninvestigated Lag area (weight " + chunkWeight.longValue() + "): " + nextUp.toString());
				if (p.isInsideVehicle()) {
					if (!p.leaveVehicle()) {
						Entity veh = p.getVehicle();
						veh.eject();
					}
				}
				final UUID playerUUID = p.getUniqueId();
				new BukkitRunnable() {
						@Override
						public void run() {
							Player q = Bukkit.getPlayer(playerUUID);
							q.teleport(nextUp);
						}
				}.runTaskLater(super.plugin, 2);
			} else {
				sender.sendMessage("Nothing left to investigate");
			}
		} else {
			sender.sendMessage("Can only invoke if a player in game");
		}
		return true;
	}

}

