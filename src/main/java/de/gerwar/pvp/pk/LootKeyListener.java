package de.gerwar.pvp.pk;

import de.gerwar.pvp.GerWarPvpConfig;
import de.gerwar.pvp.pk.models.KillRecord;
import de.gerwar.pvp.splits.SplitService;
import de.gerwar.pvp.splits.models.PendingValue;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PlayerLootReceived;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;

@Slf4j
@Singleton
public class LootKeyListener
{
	private static final String LOOT_CHEST = "Loot Chest";

	@Inject
	private PkTracker pkTracker;

	@Inject
	private GerWarPvpConfig config;

	@Inject
	private ItemManager itemManager;

	@Subscribe
	public void onPlayerLootReceived(PlayerLootReceived event)
	{
		String playerName = event.getPlayer() != null ? event.getPlayer().getName() : null;
		long total = totalGp(event.getItems());
		KillRecord validated = pkTracker.validateWithLoot(playerName, total);
		log.debug("PkLootKey: PlayerLootReceived from {} worth {} gp -> kill {}", playerName, total, validated != null ? validated.getId() : "none");
		handoffToSplits(playerName, total);
	}

	@Subscribe
	public void onLootReceived(LootReceived event)
	{
		if (event.getName() == null || !event.getName().equalsIgnoreCase(LOOT_CHEST))
		{
			return;
		}
		long total = totalGp(event.getItems());
		KillRecord validated = pkTracker.validateWithLoot(null, total);
		log.debug("PkLootKey: Loot Chest opened worth {} gp -> kill {}", total, validated != null ? validated.getId() : "none");
		handoffToSplits(LOOT_CHEST, total);
	}

	private void handoffToSplits(String source, long gpValue)
	{
		if (!config.handoffValidatedLootToSplits() || gpValue <= 0)
		{
			return;
		}
		SplitService split = SplitService.instance();
		if (split == null || !split.hasActiveSession())
		{
			return;
		}
		PendingValue pv = PendingValue.of(
			PendingValue.Type.PVP,
			"GerWar PvP (loot key)",
			"Validated loot from " + (source != null ? source : "Loot Chest"),
			gpValue,
			null
		);
		split.addPendingValue(pv);
		log.debug("PkLootKey: handed off {} gp to active split session", gpValue);
	}

	private long totalGp(Iterable<ItemStack> items)
	{
		if (items == null)
		{
			return 0;
		}
		long total = 0;
		for (ItemStack is : items)
		{
			try
			{
				ItemComposition comp = itemManager.getItemComposition(is.getId());
				int unit = itemManager.getItemPrice(is.getId());
				if (unit <= 0 && comp != null)
				{
					unit = comp.getPrice();
				}
				total += (long) unit * is.getQuantity();
			}
			catch (Exception e)
			{
				log.debug("Could not price item {}", is.getId(), e);
			}
		}
		return total;
	}
}
