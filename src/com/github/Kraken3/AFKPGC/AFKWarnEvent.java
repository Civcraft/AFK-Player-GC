package com.github.Kraken3.AFKPGC;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AFKWarnEvent extends Event implements Cancellable {
	private final static HandlerList handlers = new HandlerList();
	
	public static ArrayList<UUID> warnedPlayers = new ArrayList<UUID>();
	
	private boolean cancelled = false;
	private Player player;
	
	public AFKWarnEvent(Player player) {
		this.player = player;
		if(warnedPlayers.contains(player.getUniqueId())) {
			cancelled = true;
		} else {
			warnedPlayers.add(player.getUniqueId());
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public HandlerList getHandlers() {
		return handlers;
	}
}
