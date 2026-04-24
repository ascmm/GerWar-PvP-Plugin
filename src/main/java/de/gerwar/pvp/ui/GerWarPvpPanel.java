package de.gerwar.pvp.ui;

import de.gerwar.pvp.pk.views.GpBalancePanel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import net.runelite.client.ui.PluginPanel;

public class GerWarPvpPanel extends PluginPanel
{
	private final JTabbedPane tabs = new JTabbedPane();

	public GerWarPvpPanel()
	{
		super(false);
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		tabs.addTab("Fights", placeholder("Fights panel loading..."));
		tabs.addTab("PK K/D", placeholder("PK K/D panel loading..."));
		tabs.addTab("GP", placeholder("GP Balance panel loading..."));
		tabs.addTab("Splits", placeholder("Splits panel loading..."));
		tabs.addTab("Settings", placeholder("Open plugin settings via the side gear icon."));

		add(tabs, BorderLayout.CENTER);
	}

	public void setFightsTab(JPanel panel)
	{
		tabs.setComponentAt(0, panel);
	}

	public void setPkTab(JPanel panel)
	{
		tabs.setComponentAt(1, panel);
	}

	public void setGpBalanceTab(GpBalancePanel panel)
	{
		tabs.setComponentAt(2, panel);
	}

	public void setSplitsTab(JPanel panel)
	{
		tabs.setComponentAt(3, panel);
	}

	private static JPanel placeholder(String text)
	{
		JPanel p = new JPanel(new BorderLayout());
		JLabel l = new JLabel(text, SwingConstants.CENTER);
		l.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
		p.add(l, BorderLayout.CENTER);
		return p;
	}
}
