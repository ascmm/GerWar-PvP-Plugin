package de.gerwar.pvp.pk.views;

import de.gerwar.pvp.common.Formats;
import de.gerwar.pvp.pk.PkBalance;
import de.gerwar.pvp.pk.PkTracker;
import de.gerwar.pvp.pk.models.DeathRecord;
import de.gerwar.pvp.pk.models.KillRecord;
import de.gerwar.pvp.pk.models.KillStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;

@Singleton
public class PkTabPanel extends JPanel
{
	private final JLabel killsLbl = valueLabel();
	private final JLabel deathsLbl = valueLabel();
	private final JLabel kdLbl = valueLabel();
	private final JLabel netLbl = valueLabel();
	private final JPanel killsListPanel = new JPanel();
	private final JPanel deathsListPanel = new JPanel();

	private final PkTracker tracker;

	@Inject
	public PkTabPanel(PkTracker tracker)
	{
		this.tracker = tracker;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Header
		JPanel header = new JPanel(new GridLayout(2, 4, 4, 4));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		header.add(labelTitle("K"));
		header.add(labelTitle("D"));
		header.add(labelTitle("K/D"));
		header.add(labelTitle("Net"));
		header.add(killsLbl);
		header.add(deathsLbl);
		header.add(kdLbl);
		header.add(netLbl);

		// Kills list
		killsListPanel.setLayout(new BoxLayout(killsListPanel, BoxLayout.Y_AXIS));
		killsListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane killsScroll = new JScrollPane(killsListPanel);
		killsScroll.setBorder(titledBorder("Kills"));
		killsScroll.setPreferredSize(new Dimension(200, 250));

		// Deaths list
		deathsListPanel.setLayout(new BoxLayout(deathsListPanel, BoxLayout.Y_AXIS));
		deathsListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane deathsScroll = new JScrollPane(deathsListPanel);
		deathsScroll.setBorder(titledBorder("Deaths"));
		deathsScroll.setPreferredSize(new Dimension(200, 150));

		// Buttons
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
		buttons.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JButton resetBtn = new JButton("Reset");
		resetBtn.addActionListener(e -> {
			tracker.reset();
			refresh();
		});
		buttons.add(resetBtn);

		JPanel center = new JPanel();
		center.setBackground(ColorScheme.DARK_GRAY_COLOR);
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(header);
		center.add(Box.createVerticalStrut(6));
		center.add(killsScroll);
		center.add(Box.createVerticalStrut(6));
		center.add(deathsScroll);

		add(center, BorderLayout.CENTER);
		add(buttons, BorderLayout.SOUTH);

		refresh();
	}

	public void refresh()
	{
		SwingUtilities.invokeLater(this::doRefresh);
	}

	private void doRefresh()
	{
		PkBalance b = tracker.balance();
		killsLbl.setText(String.valueOf(b.getKills()));
		deathsLbl.setText(String.valueOf(b.getDeaths()));
		kdLbl.setText(Formats.ratio(b.kdRatio()));
		netLbl.setText(Formats.signedGp(b.net()));
		netLbl.setForeground(b.net() >= 0 ? new Color(100, 200, 100) : new Color(220, 80, 80));

		killsListPanel.removeAll();
		List<KillRecord> killList = new ArrayList<>(tracker.getKills());
		killList.sort(Comparator.comparing(KillRecord::getAt, Comparator.nullsLast(Comparator.reverseOrder())));
		for (KillRecord k : killList)
		{
			killsListPanel.add(killRow(k));
		}

		deathsListPanel.removeAll();
		List<DeathRecord> deathList = new ArrayList<>(tracker.getDeaths());
		deathList.sort(Comparator.comparing(DeathRecord::getAt, Comparator.nullsLast(Comparator.reverseOrder())));
		for (DeathRecord d : deathList)
		{
			deathsListPanel.add(deathRow(d));
		}

		killsListPanel.revalidate();
		killsListPanel.repaint();
		deathsListPanel.revalidate();
		deathsListPanel.repaint();
	}

	private Component killRow(KillRecord k)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

		JLabel name = new JLabel(k.getVictim() != null ? k.getVictim() : "?");
		name.setForeground(Color.WHITE);

		JLabel right = new JLabel();
		right.setHorizontalAlignment(SwingConstants.RIGHT);
		String status = statusBadge(k);
		right.setText(Formats.ago(k.getAt()) + "  " + status);
		switch (k.getStatus())
		{
			case VALIDATED:
				right.setForeground(new Color(100, 200, 100));
				break;
			case UNCLAIMED:
				right.setForeground(new Color(180, 150, 80));
				break;
			default:
				right.setForeground(Color.LIGHT_GRAY);
				break;
		}

		row.add(name, BorderLayout.CENTER);
		row.add(right, BorderLayout.EAST);
		return row;
	}

	private String statusBadge(KillRecord k)
	{
		if (k.getStatus() == KillStatus.VALIDATED)
		{
			return Formats.gp(k.getGpValue());
		}
		if (k.getStatus() == KillStatus.UNCLAIMED)
		{
			return "UNCLAIMED";
		}
		return "PENDING";
	}

	private Component deathRow(DeathRecord d)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		JLabel name = new JLabel(d.getKiller() != null ? d.getKiller() : "?");
		name.setForeground(Color.WHITE);
		JLabel right = new JLabel(Formats.ago(d.getAt()), SwingConstants.RIGHT);
		right.setForeground(Color.LIGHT_GRAY);
		row.add(name, BorderLayout.CENTER);
		row.add(right, BorderLayout.EAST);
		return row;
	}

	private static JLabel labelTitle(String s)
	{
		JLabel l = new JLabel(s, SwingConstants.CENTER);
		l.setForeground(Color.LIGHT_GRAY);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		return l;
	}

	private static JLabel valueLabel()
	{
		JLabel l = new JLabel("-", SwingConstants.CENTER);
		l.setForeground(Color.WHITE);
		l.setFont(l.getFont().deriveFont(Font.BOLD, 14f));
		return l;
	}

	private static javax.swing.border.Border titledBorder(String title)
	{
		return BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR, 1),
			title);
	}
}
