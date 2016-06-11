package com.github.Kraken3.AFKPGC;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BanWarnEvent extends Event {
	private final static HandlerList handlers = new HandlerList();
	
	private boolean cancelled = false;
	private Player player;
	
	public BanWarnEvent(Player player) {
		this.player = player;
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
