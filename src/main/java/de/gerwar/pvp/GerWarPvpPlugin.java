package de.gerwar.pvp;

import com.google.inject.Provides;
import de.gerwar.pvp.fights.FightTracker;
import de.gerwar.pvp.fights.views.FightsTabPanel;
import de.gerwar.pvp.pk.LootKeyListener;
import de.gerwar.pvp.pk.PkTracker;
import de.gerwar.pvp.pk.views.PkTabPanel;
import de.gerwar.pvp.splits.SplitManager;
import de.gerwar.pvp.splits.views.SplitsTabPanel;
import de.gerwar.pvp.ui.GerWarPvpPanel;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "GerWar PvP",
	description = "Fight tracking, K/D balance (loot-key validated) and group loot splits.",
	tags = {"pvp", "lms", "pking", "splits", "loot", "kd", "gerwar"}
)
public class GerWarPvpPlugin extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private EventBus eventBus;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private PkTracker pkTracker;

	@Inject
	private LootKeyListener lootKeyListener;

	@Inject
	private PkTabPanel pkTabPanel;

	private GerWarPvpPanel panel;
	private NavigationButton navButton;

	@Provides
	GerWarPvpConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GerWarPvpConfig.class);
	}

	@Override
	protected void startUp()
	{
		panel = new GerWarPvpPanel();
		panel.setPkTab(pkTabPanel);

		// Pull the sibling service plugins' panels and embed them as tabs.
		// Scheduled on the EDT because sibling plugins may initialize after us.
		SwingUtilities.invokeLater(this::attachSiblingPanels);

		BufferedImage icon = ImageUtil.loadImageResource(GerWarPvpPlugin.class, "/de/gerwar/pvp/icon.png");

		navButton = NavigationButton.builder()
			.tooltip("GerWar PvP")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		pkTracker.startUp();
		eventBus.register(pkTracker);
		eventBus.register(lootKeyListener);
	}

	private void attachSiblingPanels()
	{
		try
		{
			pluginManager.getPlugins().forEach(p ->
			{
				if (p instanceof FightTracker)
				{
					FightTracker ft = (FightTracker) p;
					FightsTabPanel fightsPanel = ft.getPanel();
					if (fightsPanel != null)
					{
						panel.setFightsTab(fightsPanel);
					}
				}
				else if (p instanceof SplitManager)
				{
					SplitManager sm = (SplitManager) p;
					JPanel splits = asJPanel(sm);
					if (splits != null)
					{
						panel.setSplitsTab(splits);
					}
				}
			});
		}
		catch (Exception e)
		{
			log.warn("Failed to attach sibling panels", e);
		}
	}

	private JPanel asJPanel(SplitManager sm)
	{
		try
		{
			java.lang.reflect.Field f = SplitManager.class.getDeclaredField("view");
			f.setAccessible(true);
			Object v = f.get(sm);
			if (v instanceof JPanel)
			{
				return (JPanel) v;
			}
			if (v instanceof SplitsTabPanel)
			{
				return (SplitsTabPanel) v;
			}
		}
		catch (Exception e)
		{
			log.debug("Could not reflect SplitManager view", e);
		}
		return null;
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(lootKeyListener);
		eventBus.unregister(pkTracker);
		pkTracker.shutDown();

		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
	}
}
