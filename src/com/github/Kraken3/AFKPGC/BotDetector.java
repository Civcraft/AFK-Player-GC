package com.github.Kraken3.AFKPGC;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/*
 * @author Maxopoly
 */
public class BotDetector implements Runnable {
	public static boolean longBans;
	public static float currentTPS = 20;
	public static float acceptableTPS;
	public static float criticalTPSChange;
	public static long frequency; // how often this runs in ticks
	public static File banfile;
	float lastRoundTPS;
	UUID topSuspect = null;
	String topSuspectName = ""; // needs to be stored additionally,
								// because retrieving the name off
								// players after kicking them is only
								// possible
								// over deprecated/buggy methods
	Location topSuspectsLocation = null;
	UUID lastRoundSuspect = null;
	String lastRoundSuspectName = "";
	Location lastRoundSuspectsLocation = null;
	public static LinkedList<String> suspectedBotters = new LinkedList<String>();
	public static LinkedList<String> bannedPlayers = new LinkedList<String>();
	// this is needed as a separated list, so we know the difference between
	// players who were banned by AFKPGC and players who were banned for other
	// reasons

	static BanList banList = AFKPGC.plugin.getServer().getBanList(
			BanList.Type.NAME); // ban after names not ips

	public void run() {
		if (!AFKPGC.enabled) {
			return;
		}
		currentTPS = TpsReader.getTPS();
		Map<UUID, LastActivity> lastActivities = LastActivity.lastActivities;
		if (currentTPS < acceptableTPS) {
			int smallestMovedDistance = 1024;
			Set<Map.Entry<UUID, LastActivity>> entries = lastActivities
					.entrySet();
			for (Map.Entry<UUID, LastActivity> entry : entries) {
				UUID playerUUID = entry.getKey();
				if (lastActivities.containsKey(playerUUID)) { // according to
																// the author of
																// AFKGPC, there
																// might be
																// inconsistencies
																// in this list,
																// so this
																// additional
																// check is
																// needed
					LastActivity la = entry.getValue();
					la.loggedLocations.add(Bukkit.getPlayer(playerUUID)
							.getLocation());
					if (la.loggedLocations.size() >= 5) {
						if (la.loggedLocations.size() > 5) {
							la.loggedLocations.removeFirst();
						}
						int itWasntMeISwear = la.calculateMovementradius();
						if (itWasntMeISwear < smallestMovedDistance) {
							smallestMovedDistance = itWasntMeISwear;
							Player dirtyLiar = Bukkit.getPlayer(playerUUID);

							topSuspect = playerUUID;
							topSuspectsLocation = dirtyLiar.getLocation();
							topSuspectName = dirtyLiar.getName();
						}

					}

				}
			}
			if (topSuspect != null) {
				if (!AFKPGC.immuneAccounts.contains(topSuspect)) {
					Date currentDate = new Date();
					long bantime = (long) (frequency / currentTPS * 1000);
					// Because the ban time is based on real time and the next
					// run
					// of this method is based on the tick, the ban time needs
					// to be
					// adjusted to the tick. The long form of this would be:
					// (20/currentTPS) * (frequency/20) * 1000
					// The time is in ms
					banList.addBan(topSuspectName,
							"You were suspected to cause lag and banned for "
									+ bantime / 1000 + " seconds", new Date(
									currentDate.getTime() + bantime), null);
					// ban the player for ~ a minute
				}
			}
			if (lastRoundSuspect != null) {
				if (!AFKPGC.immuneAccounts.contains(lastRoundSuspect)) {
					if (currentTPS - lastRoundTPS > criticalTPSChange) { // value
																			// needs
																			// to
																			// be
						// configured, that was
						// just the first thing
						// that came to mind.
						// This can be
						// relatively sensitive,
						// because it will only
						// ban players for a
						// longer period of
						// time, if it catches
						// them twice here
						Date currentDate = new Date();
						if (suspectedBotters.contains(lastRoundSuspectName)) {
							if (longBans) {
								banList.addBan(
										lastRoundSuspectName,
										"Kicking you resulted in a noticeable TPS im"
												+ "provement, so you were banned until the TPS goes"
												+ " back to normal values.",
										new Date(
												currentDate.getTime() + 6 * 3600 * 1000),
										null); // 6h ban
								bannedPlayers.add(lastRoundSuspectName);
								addToBanfile(lastRoundSuspectName);
								suspectedBotters.remove(lastRoundSuspectName);
							}
							AFKPGC.logger.info("The player "
													+ lastRoundSuspectName
													+ " causes lag and is a repeated offender,"
													+ " kicking him resulted in a TPS improvement of "
													+ String.valueOf(currentTPS
															- lastRoundTPS)
													+ " at the location "
													+ lastRoundSuspectsLocation
															.toString());

						} else {
							suspectedBotters.add(lastRoundSuspectName);
							AFKPGC.logger.info( "The player "
													+ lastRoundSuspectName
													+ " is suspected to cause lag,"
													+ " kicking him resulted in a TPS improvement of "
													+ String.valueOf(currentTPS
															- lastRoundTPS)
													+ " at the location "
													+ lastRoundSuspectsLocation
															.toString());
						}
					}
				}
			}

			lastRoundTPS = currentTPS;
			lastRoundSuspect = topSuspect;
			lastRoundSuspectName = topSuspectName;
			lastRoundSuspectsLocation = topSuspectsLocation; // as preparation
																// for the next
																// run;
		} else { // TPS is high enough
			if (bannedPlayers.size() != 0) {
				freeEveryone(); // not everyone, but everyone banned by this
								// plugin
			}
		}

	}

	public static void freeEveryone() {
		for (int i = 0; i < bannedPlayers.size(); i++) {
			banList.pardon(bannedPlayers.get(i));
		}
		bannedPlayers.clear();
		banfile.delete();
	}

	public void addToBanfile(String name) {
		try {
			FileWriter fileWriter = new FileWriter(banfile);

			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			bufferedWriter.write(name);
			bufferedWriter.newLine();
			bufferedWriter.close();
		} catch (IOException e) {
			AFKPGC.logger.warning("Error while trying to add " + name
							+ " to the banned players file");
		}
	}

	public static void parseBanlist() {
		if (banfile == null && !banfile.exists()) {
			return;
		}
		String line = null;
		try {
			FileReader fileReader = new FileReader(banfile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				bannedPlayers.add(line);
			}
			bufferedReader.close();
		} catch (IOException ex) {
			AFKPGC.logger.warning("Error while trying to parse the banned players file");
		}
	}

}
