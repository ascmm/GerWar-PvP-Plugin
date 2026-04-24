package de.gerwar.pvp;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(GerWarPvpConfig.GROUP)
public interface GerWarPvpConfig extends Config
{
	String GROUP = "gerwarpvp";

	@ConfigSection(
		name = "General",
		description = "Cross-module settings",
		position = 0
	)
	String generalSection = "general";

	@ConfigSection(
		name = "PK K/D",
		description = "Kills, deaths and PK loot balance",
		position = 20
	)
	String pkSection = "pk";

	// ---- General ---------------------------------------------------------

	@ConfigItem(
		keyName = "handoffValidatedLootToSplits",
		name = "Auto-hand off validated loot to active split session",
		description = "When a loot key is opened during an active split session, add its GP value as a pending split entry.",
		section = generalSection,
		position = 0
	)
	default boolean handoffValidatedLootToSplits()
	{
		return true;
	}

	// ---- PK -------------------------------------------------------------

	@ConfigItem(
		keyName = "pkPendingTimeoutMinutes",
		name = "Pending kill timeout (minutes)",
		description = "A pending kill becomes UNCLAIMED if not validated via a loot key within this many minutes. Set to 0 to disable.",
		section = pkSection,
		position = 0
	)
	default int pkPendingTimeoutMinutes()
	{
		return 30;
	}

	@ConfigItem(
		keyName = "pkTrackDeathValue",
		name = "Estimate death value via item prices",
		description = "Try to estimate lost GP on death using GE prices of recently-equipped items.",
		section = pkSection,
		position = 1
	)
	default boolean pkTrackDeathValue()
	{
		return true;
	}
}
