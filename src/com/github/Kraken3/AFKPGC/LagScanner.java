package com.github.Kraken3.AFKPGC;

import org.bukkit.Location;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashSet;
import org.apache.commons.lang.mutable.MutableLong;

/**
 * Should be runnable with callback. First iteration will be straight mainthread code, b/c testing.
 * 
 * TODO: Make a runnable, redesign BotDetector to play well with deferred answers.
 * 
 * @author ProgrammerDan
 */
public class LagScanner {

	private static Map<String, Map<Long, LagScanner.Result>> cache = 
			new HashMap<String, Map<Long, LagScanner.Result>>();

	private static Map<String, TreeMap<Long, HashSet<Long>>> worstChunksUnchecked = 
			new HashMap<String, TreeMap<Long, HashSet<Long>>>();
	private static Map<String, TreeMap<Long, Long>> worstChunksUncheckedIdx = 
			new HashMap<String, TreeMap<Long, Long>>();
	private static Map<String, TreeSet<Long>> worstChunksChecked = 
			new HashMap<String, TreeSet<Long>>();

	/**
	 * Gets another chunk. Goes world by world from worst chunk to best in the world.
	 */
	public static Location getNextBadChunk(MutableLong ml, boolean mark) {
		synchronized(worstChunksChecked) {
			for (String world : worstChunksUnchecked.keySet()) {
				TreeMap<Long, HashSet<Long>> worldBad = worstChunksUnchecked.get(world);
				if (worldBad != null && !worldBad.isEmpty()) {
					for (Long score : worldBad.descendingKeySet()) {
						HashSet<Long> locations = worldBad.get(score);
						if (locations != null && !locations.isEmpty()) {
							Long location = locations.iterator().next();
							long chunkX = location >> 32L;
							long chunkZ = location - (chunkX << 32L);
							World worldQ = Bukkit.getWorld(world);
							int locX = ((int)chunkX) * 16 + 8;
							int locZ = ((int)chunkZ) * 16 + 8;
							int locY = worldQ.getHighestBlockYAt(locX, locZ) + 2;
							if (ml != null) {ml.setValue(score);}
							if (mark) { LagScanner.markBadChunk(world, location); }
							return new Location(worldQ, (double)locX, (double)locY, (double)locZ);
							// find and return first location found.
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Marks a bad chunk as reviewed
	 */
	public static void markBadChunk(String world, Long chunk) {
		synchronized(worstChunksChecked) {
			TreeSet<Long> worldChecked = worstChunksChecked.get(world);
			if (worldChecked != null) {
				if (worldChecked.contains(chunk)) {
					return; // already marked, so fail fast.
				}
			}
			if (worldChecked == null) {
				worldChecked = new TreeSet<Long>();
				worstChunksChecked.put(world, worldChecked);
			}
			worldChecked.add(chunk);
			// now remove from unchecked list.
			TreeMap<Long, Long> worldIndex = worstChunksUncheckedIdx.get(world);
			TreeMap<Long, HashSet<Long>> worldChunks = worstChunksUnchecked.get(world);
			if (worldIndex != null) {
				Long curval = worldIndex.get(chunk);
				if (curval != null) { // exists.
					HashSet<Long> valChunks = worldChunks.get(curval);
					if (valChunks != null) {
						valChunks.remove(chunk);
						worldIndex.remove(chunk);
					} else {
						// TODO: soft exception
					}
				} // else doesn't exist, don't remove.
			} // else doesn't exist, don't remove.
		}
	}


	/**
	 * Adds or updates a "bad chunk" for admin review
	 */
	public static void addBadChunk(String world, Long chunk, Long weight) {
		synchronized(worstChunksChecked) {
			TreeSet<Long> worldChecked = worstChunksChecked.get(world);
			if (worldChecked != null) {
				if (worldChecked.contains(chunk)) {
					return; // checked already so fail fast.
				}
			} else {
				worldChecked = new TreeSet<Long>();
				worstChunksChecked.put(world, worldChecked);
			}
			TreeMap<Long, HashSet<Long>> worldC = worstChunksUnchecked.get(world);
			TreeMap<Long, Long> worldI = worstChunksUncheckedIdx.get(world);
			if (worldC == null) {
				worldC = new TreeMap<Long, HashSet<Long>>(); // create new world weight index
				worstChunksUnchecked.put(world, worldC);
			}
			if (worldI == null) {
				worldI = new TreeMap<Long, Long>(); // create new world chunk index
				worstChunksUncheckedIdx.put(world, worldI); 
			}
			Long prevWeight = worldI.get(chunk);
			if (prevWeight != null) {
				if (prevWeight.equals(weight)) {
					return; // no change;
				}
				HashSet<Long> chunks = worldC.get(prevWeight);
				chunks.remove(chunk); // remove old from weight index
			}
			HashSet<Long> chunks = worldC.get(weight);
			if (chunks == null) {
				chunks = new HashSet<Long>();
				worldC.put(weight, chunks); // create weight index
			}
			chunks.add(chunk); // add to weight index
			worldI.put(chunk, weight); // add to chunk index
		}
	}


	public static long cacheTimeout;
	public static long lagSourceThreshold;
	public static long extremeLagSourceThreshold;
	public static long unloadThreshold;
	public static long innocentThreshold;
	public static boolean performUnload;
	public static boolean fullScan;
	public static long normalChunkValue;

	private Location center;
	private Integer radius;
	private ScanCallback<Boolean> callback;
	private boolean noCache;

	private Boolean isLagSource;
	private Long lagCompute;

	public LagScanner(Location center, Integer radius, ScanCallback<Boolean> callback, boolean noCache) {
		this.center = center;
		this.radius = radius;
		this.callback = callback;
		this.isLagSource = null;
		this.lagCompute = null;
		this.noCache = noCache;
	}

	public static LagScanner instance(Location center, Integer radius, ScanCallback<Boolean> callback, boolean noCache){
		return new LagScanner(center, radius, callback, noCache);
	}

	public void run() {
		boolean lagSource = false;
		Chunk originChunk = center.getChunk();
		World chunkWorld = originChunk.getWorld();
		int oX = originChunk.getX();
		int oZ = originChunk.getZ();
		long now = System.currentTimeMillis(); // all tests against same millis ... for now.
		long lagSum = 0L;
		int chunksTested = 0;

		// radius in minecraft is square.
		for (int r = 0; r <= radius && (fullScan || lagSum < extremeLagSourceThreshold); r++) {
			for (int x = oX - r; x <= oX + r && (fullScan || lagSum < extremeLagSourceThreshold); x++) {
				for (int z = oZ - r; z <= oZ + r; z++) {
					if (x > oX - r && x < oX + r &&
						z > oZ - r && z < oZ + r) {
						continue; // don't retread inner chunks of prior radius
					}
					LagScanner.Result test = testChunk(chunkWorld.getChunkAt(x, z), now);
					chunksTested ++;
					lagSum += test.lagContrib;
					if (lagSum >= lagSourceThreshold) {
						lagSource = true;
						if (!fullScan && lagSum >= extremeLagSourceThreshold) {
							break;
						}
					}
				}
			}
		}

		this.isLagSource = lagSource;
		this.lagCompute = lagSum;
		AFKPGC.debug("LagScanner completed in ", (System.currentTimeMillis() - now), "ms centered on ", 
				center, " found lag weight ", lagSum, " after scanning ",
				chunksTested, " and ", (lagSource) ? "is ": "is not ", "a lag source");
		if (callback != null) {
			callback.callback(lagSource);
		}
	}

	/**
	 * Uses the cached results exclusively to determine the worst offender chunks to
	 * unload immediately.
	 */
	public static void unloadChunks(Location center, int radius) {
		if (!performUnload) {
			return;
		}
		Chunk originChunk = center.getChunk();
		World chunkWorld = originChunk.getWorld();
		int oX = originChunk.getX();
		int oZ = originChunk.getZ();
		String world = chunkWorld.getName();

		Map<Long, LagScanner.Result> lcache = LagScanner.cache.get(world);
		if (lcache == null) {
			return;
		}
		AFKPGC.debug("Attempting to unload laggiest chunks");
		for (int x = oX - radius; x <= oX + radius; x++) {
			for (int z = oZ - radius; z <= oZ + radius; z++) {
				long chunkId = ((long) x << 32L) + (long) z;
				LagScanner.Result result = lcache.get(chunkId);
				if (result != null && result.lagContrib >= unloadThreshold) {
					chunkWorld.unloadChunkRequest(x, z, true);
				}
			}
		}
	}

	public LagScanner.Result testChunk(Chunk chunk, long now) {
		String world = chunk.getWorld().getName();
		long chunkId = ((long) chunk.getX() << 32L) + (long) chunk.getZ();
		Map<Long, LagScanner.Result> worldCache = null;
		if (!LagScanner.cache.containsKey(world)) {
			worldCache = new HashMap<Long, LagScanner.Result>();
			cache.put(world, worldCache);
		} else {
			worldCache = cache.get(world);
		}

		LagScanner.Result result = null;
		if (!noCache && worldCache.containsKey(chunkId)) {
			LagScanner.Result cachedResult = worldCache.get(chunkId);
			if (cachedResult.lastUpdate + cacheTimeout > now) {
				// hasn't exceeded cache timeout, so use it.
				result = cachedResult;
			} else {
				// cache expired, remove.
				worldCache.remove(chunkId);
			}
		} // else not cached

		if (result == null) {
			Map<String, Long> stats = new HashMap<String, Long>();
			Map<String, Integer> statCount = new HashMap<String, Integer>();
			// not in the cache, so let's compute.
			long totalCost = 0L;

			// Test tiles
			BlockState[] lagTiles = chunk.getTileEntities();
			for (BlockState tile : lagTiles) {
				Material tiletype = tile.getType();
				long tilecost = (long) LagCostConfig.getInstance().cost(tiletype);
				totalCost += tilecost;
				if (stats.containsKey(tiletype.name())) {
					stats.put(tiletype.name(), tilecost + stats.get(tiletype.name()));
					statCount.put(tiletype.name(), 1 + statCount.get(tiletype.name()));
				} else {
					stats.put(tiletype.name(), tilecost);
					statCount.put(tiletype.name(), 1);
				}
				if (totalCost >= extremeLagSourceThreshold && !fullScan) {
					break; // if we cross the threshold, don't keep adding.
				}
			}

			if (totalCost < lagSourceThreshold) {
				// Test entities
				Entity[] lagEntity = chunk.getEntities();
				for (Entity entity : lagEntity) {
					EntityType enttype = entity.getType();
					long entcost = (long) LagCostConfig.getInstance().cost(enttype);
					totalCost += entcost;
					if (stats.containsKey(enttype.name())) {
						stats.put(enttype.name(), entcost + stats.get(enttype.name()));
						statCount.put(enttype.name(), 1 + statCount.get(enttype.name()));
					} else {
						stats.put(enttype.name(), entcost);
						statCount.put(enttype.name(), 1);
					}
					if (totalCost >= extremeLagSourceThreshold && !fullScan) {
						break; // if we cross the threshold, don't keep adding.
					}
				}
			}
			StringBuffer sb = new StringBuffer();
			for (Map.Entry<String, Long> stat : stats.entrySet()) {
				sb.append(stat.getKey()).append("[").append(statCount.get(stat.getKey()))
						.append("]: ").append(stat.getValue()).append("  ");
			}				
			// record the result.
			result = new LagScanner.Result(world, chunkId, chunk.getX(), chunk.getZ(), totalCost, now);
			worldCache.put(chunkId, result);
			AFKPGC.debug("The chunk ", chunk.getX(), ", ", chunk.getZ(), " measures ", result.lagContrib, " lag sources",
					result.lagContrib < normalChunkValue ? " (ignored)" : "",", details: ", sb);
		}
		else {
			AFKPGC.debug("The chunk ", chunk.getX(), ", ", chunk.getZ(), " was loaded from the cache with a measure of ", 
					result.lagContrib, " lag sources", result.lagContrib < normalChunkValue ? " (ignored)": "");
		}
		if (result.lagContrib < normalChunkValue) {
			return new LagScanner.Result(null, 0, 0, 0, 0L, 0L);
		}
		// add to admincrimes list
		LagScanner.addBadChunk(world, chunkId, result.lagContrib);
		return result;
	}

	public boolean isLagSource() {
		return (isLagSource == null) ? false : isLagSource;
	}
	
	public boolean isExtremeLagSource() {
		if (lagCompute != null && lagCompute >= extremeLagSourceThreshold) {
			return true;
		}
		return false;
	}
	
	public long getLagCompute() {
		return (lagCompute == null) ? -1L : lagCompute;
	}

	public boolean hasAnswer() {
		return isLagSource == null;
	}

	class Result {
		public String worldId;
		public long chunkId;
		public int chunkX;
		public int chunkZ;
		public long lagContrib;
		public long lastUpdate;

		public Result(String worldId, long chunkId, int chunkX, int chunkZ, long lagContrib, long lastUpdate) {
			this.worldId = worldId;
			this.chunkId = chunkId;
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
			this.lagContrib = lagContrib;
			this.lastUpdate = lastUpdate;
		}
	}
}
