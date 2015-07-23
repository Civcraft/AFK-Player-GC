package com.github.Kraken3.AFKPGC;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurationReader {
	public static boolean readConfig() {

		// TODO: Change so that the plugin copies jar internal default
		// config.yml into FS only when one doesn't exist there;
		AFKPGC.plugin.saveDefaultConfig();
		AFKPGC.plugin.reloadConfig();
		FileConfiguration conf = AFKPGC.plugin.getConfig();

		int max_players = AFKPGC.plugin.getServer().getMaxPlayers();
		AFKPGC.logger.info("Server reports maximum of " + max_players + " players");

		int[] kickThresholds = new int[max_players];
		for (int i = 0; i < max_players; i++)
			kickThresholds[i] = -1;

		List<String> ktl = conf.getStringList("kick_thresholds");
		int[] nums = new int[3];
		for (String s : ktl) {
			nums[0] = nums[1] = nums[2] = -1;
			parseNaturals(s, nums);
			int min = nums[0], max = nums[1], t = nums[2];
			AFKPGC.logger.info("Between " + min + " and " + max + " kick after " + t + " seconds.");
			if (min > max || min < 1 || max < 1 || t < 0) {
				AFKPGC.logger.warning("Configuration file error: " + s);
				return false;
			}

			for (int i = min; i <= max; i++) {
				if (i <= max_players) {
					if (kickThresholds[i - 1] != -1)
						AFKPGC.logger.config("Previously defined threshold getting redefined in: "
										+ s);
					kickThresholds[i - 1] = t;
				} else {
					break; // don't keep going if we're not gonna use it.
				}
			}
		}

		boolean foundEmptyThreshold = false;
		int gapStart = -1;
		int gapEnd = -1;
		for (int i = 0; i < max_players; i++) {
			if (kickThresholds[i] == -1) {
				if (gapStart > -1) {
					gapEnd = i;
				} else {
					gapStart = i;
					gapEnd = i;
				}
				foundEmptyThreshold = true;
			} else if (gapStart > -1) {
				AFKPGC.logger.warning("Configuration file incomplete - plugin doesn't know when to kick players when there are between "
						+ (gapStart + 1) + " and "
						+ (gapEnd + 1) + " players online");
				gapStart = -1;
				gapEnd = -1;
			}

		}

		if (foundEmptyThreshold) {
			if (gapStart > -1) {
				AFKPGC.logger.warning("Configuration file incomplete - plugin doesn't know when to kick players when there are between "
						+ (gapStart + 1) + " and "
						+ (gapEnd + 1) + " players online");
			}
			return false;
		}

		ArrayList<Warning> warnings = new ArrayList<Warning>();
		ktl = conf.getStringList("warnings");
		for (String s : ktl) {
			s = s.trim();
			int slen = s.length();
			StringBuilder sb = new StringBuilder();
			int n = 0;
			boolean numberPart = true;
			for (int i = 0; i < slen; i++) {
				char c = s.charAt(i);
				if (numberPart && c >= '0' && c <= '9') {
					n = n * 10 + c - '0';
				} else {
					numberPart = false;
					sb.append(c);
				}
			}
			warnings.add(new Warning(n * 1000, sb.toString().trim()));
		}

		int wlen = warnings.size();
		Warning[] wa = new Warning[wlen];
		for (int i = 0; i < wlen; i++)
			wa[i] = warnings.get(i);

		Set<UUID> immuneAccounts = new HashSet<UUID>();
		for (String account_id : conf.getStringList("immune_accounts")) {
			try {
				UUID uuid = UUID.fromString(account_id);
				// TODO: When Bukkit gets their act together with the account
				// ID migrations and makes Server.getOfflinePlayer(UUID)
				// reasonably efficient, validate the account ID is a real
				// player on this server.
				immuneAccounts.add(uuid);
			} catch (Exception ex) {
				AFKPGC.logger.info("Invalid UUID in immune_accounts: "
						+ account_id);
			}
		}
		BotDetector.acceptableTPS = conf.getInt("acceptable_TPS");
		BotDetector.criticalTPSChange = (float) conf
				.getDouble("critical_TPS_Change");
		BotDetector.frequency = conf.getInt("kicking_frequency");
		BotDetector.longBans = conf.getBoolean("long_bans");
		Kicker.message_on_kick = conf.getString("kick_message");
		Kicker.warnings = wa;
		Kicker.kickThresholds = kickThresholds;
		AFKPGC.immuneAccounts = immuneAccounts;

		return true;
	}

	/**
	 * attempt to fill numbers but quietly fail.
	 */
	public static void parseNaturals(String str, int[] numbers) {
		Pattern num = Pattern.compile("[0-9]+");
		Matcher findnum = num.matcher(str);
		for (int i = 0; i < numbers.length; i++) {
			if (findnum.find()) {
				String cmatch = findnum.group();
				try {
					numbers[i] = Integer.valueOf(cmatch);
				} catch (NumberFormatException nfe) {
					break;
				}
			} else {
				break;
			}
		}
	}
}
