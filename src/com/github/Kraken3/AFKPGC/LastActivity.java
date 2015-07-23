package com.github.Kraken3.AFKPGC;



import java.util.Map;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.Location;


public class LastActivity{
	public static Map<UUID, LastActivity> lastActivities = new TreeMap<UUID, LastActivity>();
	public static long currentTime; 	//OCD compels me to save a few System.currentTimeMillis() calls..	
	public LinkedList <Location> loggedLocations;
	public long timeOfLastActivity;
	public long timeOflastKickerPass; //time of the last Kicker.run call, relevant for warnings
	public UUID playerName; //useful only in onCommandList
	
	//let's be polite.. I strongly dislike bukkit.. onPlayerQuitEvent doesn't trigger on all
	//player log off events for some reason. This causes LastActivity.lastActivities to contain more players
	//than there are playing on the server. FixInconsitencies() fixes this problem.
	//It's ok. We are in agreement with our hatred.
	static public void FixInconsitencies(){
		Map<UUID, LastActivity> lastActivities = LastActivity.lastActivities;	
		Player[] players = new Player[AFKPGC.plugin.getServer().getOnlinePlayers().size()];
		int x = 0;
		for (Player p: AFKPGC.plugin.getServer().getOnlinePlayers()){
			players[x] = p;
			x++;
		}
		TreeSet<UUID> playersTree = new TreeSet<UUID>();		
		
		for(Player p:players) {
			UUID uuid = p.getUniqueId();
			LastActivity la = lastActivities.get(uuid);
			if(la == null) {
				la = AFKPGC.addPlayer(p);
			}
			if(la != null) {
				la.timeOflastKickerPass = LastActivity.currentTime;
				playersTree.add(uuid);
			}
		}				
		
		UUID[] keySet = lastActivities.keySet().toArray(new UUID[0]);		   
		for(UUID i:keySet){
			if(!playersTree.contains(i)) AFKPGC.removerPlayer(i);			   
		}		
		
	}
	public int calculateMovementradius() {
		Location current=loggedLocations.getLast();
		int distance=0;
		for(int i=0;i<loggedLocations.size()-1;i++) {
			int possibleNewDistance=(int)loggedLocations.get(i).distance(current);
			if (possibleNewDistance>distance) {
				distance=possibleNewDistance;			
			}
		}
		return distance;
	}
}
