package com.github.Kraken3.AFKPGC.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.command.CommandSender;

import com.github.Kraken3.AFKPGC.AFKPGC;
import com.github.Kraken3.AFKPGC.LastActivity;

public class ListPlayers extends AbstractCommand{
	public ListPlayers(AFKPGC instance) {
		super(instance,"list");
	}
	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		int p = 10;
		if (args.size() == 1) {
			try {
				p = Integer.parseInt(args.get(0));
			} catch (Exception e) {
			}
		}
		if (p < 0)
			p = 10;
		sender.sendMessage("List of "+p+" most inactive players:");

		ArrayList<LastActivity> las = new ArrayList<LastActivity>();
		Set<UUID> set = LastActivity.lastActivities.keySet();
		for (UUID i : set)
			las.add(LastActivity.lastActivities.get(i));
		int laslen = las.size();
		Collections.sort(las, new Comparator<LastActivity>() {
			public int compare(LastActivity arg0, LastActivity arg1) {
				return (int) (arg0.timeOfLastActivity - arg1.timeOfLastActivity);
			}
		});
		for (int i = 0; i < laslen; i++) {
			if (i == p)
				break;
			int t = (int) ((LastActivity.currentTime - las.get(i).timeOfLastActivity) / 1000);
			sender.sendMessage(las.get(i).playerName+" last interacted on the server "+CommandHandler.readableTimeSpan(t)+" ago");
		}
		return true;
	}
}
