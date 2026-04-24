package de.gerwar.pvp.pk;

import de.gerwar.pvp.GerWarPvpConfig;
import de.gerwar.pvp.common.PersistenceManager;
import de.gerwar.pvp.pk.models.DeathRecord;
import de.gerwar.pvp.pk.models.KillRecord;
import de.gerwar.pvp.pk.models.KillStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
public class PkTracker
{
	public static final String KILLS_FILE = "pk-kills.json";
	public static final String DEATHS_FILE = "pk-deaths.json";

	@Inject
	private Client client;

	@Inject
	private GerWarPvpConfig config;

	@Inject
	private PersistenceManager persistence;

	private final List<KillRecord> kills = new CopyOnWriteArrayList<>();
	private final List<DeathRecord> deaths = new CopyOnWriteArrayList<>();

	private String lastOpponentName;
	private int lastOpponentCombatLevel;
	private Instant lastLocalHitOnOpponent;

	public void startUp()
	{
		kills.clear();
		kills.addAll(persistence.readList(KILLS_FILE, KillRecord.class));
		deaths.clear();
		deaths.addAll(persistence.readList(DEATHS_FILE, DeathRecord.class));
		log.debug("PkTracker loaded: {} kills, {} deaths", kills.size(), deaths.size());
	}

	public void shutDown()
	{
		save();
	}

	public void save()
	{
		persistence.write(KILLS_FILE, kills);
		persistence.write(DEATHS_FILE, deaths);
	}

	public List<KillRecord> getKills()
	{
		return Collections.unmodifiableList(kills);
	}

	public List<DeathRecord> getDeaths()
	{
		return Collections.unmodifiableList(deaths);
	}

	public PkBalance balance()
	{
		return PkBalance.compute(kills, deaths);
	}

	public synchronized void reset()
	{
		kills.clear();
		deaths.clear();
		save();
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Actor target = event.getActor();
		Hitsplat hitsplat = event.getHitsplat();
		Player local = client.getLocalPlayer();
		if (local == null || target == null || !(target instanceof Player))
		{
			return;
		}
		Player victim = (Player) target;
		if (victim == local)
		{
			return;
		}
		// Only count hits from the local player
		if (hitsplat != null && hitsplat.isMine())
		{
			lastOpponentName = victim.getName();
			lastOpponentCombatLevel = victim.getCombatLevel();
			lastLocalHitOnOpponent = Instant.now();
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		Actor actor = event.getActor();
		Player local = client.getLocalPlayer();
		if (local == null || actor == null)
		{
			return;
		}

		if (actor == local)
		{
			// Local player died — record death
			DeathRecord d = new DeathRecord();
			d.setId(UUID.randomUUID().toString());
			d.setKiller(lastOpponentName != null ? lastOpponentName : "Unknown");
			d.setAt(Instant.now());
			d.setWorld(client.getWorld());
			d.setLostValueEstimate(0L); // can be estimated later via price lookup
			deaths.add(d);
			save();
			return;
		}

		if (actor instanceof Player)
		{
			Player victim = (Player) actor;
			// Credit local player if we hit this target recently (within 10s)
			if (lastLocalHitOnOpponent != null
				&& Duration.between(lastLocalHitOnOpponent, Instant.now()).getSeconds() < 10
				&& victim.getName() != null
				&& victim.getName().equalsIgnoreCase(lastOpponentName))
			{
				KillRecord k = new KillRecord();
				k.setId(UUID.randomUUID().toString());
				k.setVictim(victim.getName());
				k.setAt(Instant.now());
				k.setWorld(client.getWorld());
				k.setCombatLevel(victim.getCombatLevel());
				k.setStatus(KillStatus.PENDING);
				kills.add(k);
				save();
				log.debug("PkTracker: recorded pending kill on {}", victim.getName());
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Expire PENDING kills older than configured timeout
		int timeoutMinutes = config.pkPendingTimeoutMinutes();
		if (timeoutMinutes <= 0)
		{
			return;
		}
		Instant cutoff = Instant.now().minus(Duration.ofMinutes(timeoutMinutes));
		boolean anyChanged = false;
		for (KillRecord k : kills)
		{
			if (k.getStatus() == KillStatus.PENDING && k.getAt() != null && k.getAt().isBefore(cutoff))
			{
				k.setStatus(KillStatus.UNCLAIMED);
				anyChanged = true;
			}
		}
		if (anyChanged)
		{
			save();
		}
	}

	/**
	 * Invoked by the loot-key listener when a loot key is opened and its contents
	 * are resolved. Finds the most recent matching pending kill and validates it.
	 * Returns the validated record, or null if no match was found.
	 */
	public synchronized KillRecord validateWithLoot(String sourceName, long gpValue)
	{
		KillRecord target = null;
		for (int i = kills.size() - 1; i >= 0; i--)
		{
			KillRecord k = kills.get(i);
			if (k.getStatus() != KillStatus.PENDING)
			{
				continue;
			}
			if (sourceName == null
				|| (k.getVictim() != null && k.getVictim().equalsIgnoreCase(sourceName)))
			{
				target = k;
				break;
			}
		}

		if (target == null)
		{
			// Fallback: create a VALIDATED record directly (loot-chest without a known kill)
			KillRecord k = new KillRecord();
			k.setId(UUID.randomUUID().toString());
			k.setVictim(sourceName != null ? sourceName : "Loot Chest");
			k.setAt(Instant.now());
			k.setWorld(client.getWorld());
			k.setStatus(KillStatus.VALIDATED);
			k.setGpValue(gpValue);
			k.setValidatedAt(Instant.now());
			kills.add(k);
			save();
			return k;
		}

		target.setStatus(KillStatus.VALIDATED);
		target.setGpValue(gpValue);
		target.setValidatedAt(Instant.now());
		save();
		return target;
	}

	public List<KillRecord> mutableKills()
	{
		return kills;
	}
}
