package de.gerwar.pvp.splits.views;

import de.gerwar.pvp.splits.KnownPlayers;
import de.gerwar.pvp.splits.SplitService;
import de.gerwar.pvp.splits.SplitsConfigKeys;
import de.gerwar.pvp.splits.controllers.PanelController;

public class PopoutView extends SplitsTabPanel
{
	public PopoutView(SplitService sessionManager, SplitsConfigKeys config, KnownPlayers playerManager, PanelController controller)
	{
		super(sessionManager, config, playerManager, controller);
	}
}
