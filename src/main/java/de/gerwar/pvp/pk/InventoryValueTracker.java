package de.gerwar.pvp.pk;

import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

@Slf4j
@Singleton
@SuppressWarnings("deprecation")
public class InventoryValueTracker
{
	@Inject
	private ItemManager itemManager;

	private long inventoryValue = 0L;
	private long equipmentValue = 0L;

	// Snapshot from the previous game tick — used at death time because
	// inventory/equipment are cleared before we can react to ActorDeath.
	@Getter
	private long snapshotInventoryValue = 0L;

	@Getter
	private long snapshotEquipmentValue = 0L;

	public long snapshotRiskValue()
	{
		return snapshotInventoryValue + snapshotEquipmentValue;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Roll forward the snapshot each tick so the death handler sees
		// the state from one tick ago, before the death wipe.
		snapshotInventoryValue = inventoryValue;
		snapshotEquipmentValue = equipmentValue;
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		ItemContainer c = event.getItemContainer();
		if (c == null)
		{
			return;
		}
		int id = event.getContainerId();
		if (id == InventoryID.INVENTORY.getId())
		{
			inventoryValue = totalValue(c);
		}
		else if (id == InventoryID.EQUIPMENT.getId())
		{
			equipmentValue = totalValue(c);
		}
	}

	private long totalValue(ItemContainer c)
	{
		Item[] items = c.getItems();
		if (items == null)
		{
			return 0L;
		}
		long total = 0L;
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			try
			{
				int unit = itemManager.getItemPrice(item.getId());
				if (unit <= 0)
				{
					unit = itemManager.getItemComposition(item.getId()).getPrice();
				}
				if (unit > 0)
				{
					total += (long) unit * item.getQuantity();
				}
			}
			catch (Exception e)
			{
				log.debug("Could not price item {}", item.getId(), e);
			}
		}
		return total;
	}
}
