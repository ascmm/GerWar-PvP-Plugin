package de.gerwar.pvp.pk.views;

import de.gerwar.pvp.common.Formats;
import de.gerwar.pvp.pk.PkTracker;
import de.gerwar.pvp.pk.models.DeathRecord;
import de.gerwar.pvp.pk.models.KillRecord;
import de.gerwar.pvp.pk.models.KillStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import net.runelite.client.ui.ColorScheme;

@Singleton
public class GpBalancePanel extends JPanel
{
	private static final Color WIN_COLOR = new Color(100, 200, 100);
	private static final Color LOSE_COLOR = new Color(220, 80, 80);

	private final JLabel totalWinLbl = bigLabel("-", WIN_COLOR);
	private final JLabel totalLoseLbl = bigLabel("-", LOSE_COLOR);
	private final JLabel netLbl = bigLabel("-", Color.WHITE);
	private final JPanel winsListPanel = new JPanel();
	private final JPanel losesListPanel = new JPanel();

	private final PkTracker tracker;

	@Inject
	public GpBalancePanel(PkTracker tracker)
	{
		this.tracker = tracker;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Top summary strip
		JPanel summary = new JPanel(new GridLayout(2, 3, 6, 2));
		summary.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		summary.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		summary.add(sectionTitle("WIN", WIN_COLOR));
		summary.add(sectionTitle("NET", Color.LIGHT_GRAY));
		summary.add(sectionTitle("LOSE", LOSE_COLOR));
		summary.add(totalWinLbl);
		summary.add(netLbl);
		summary.add(totalLoseLbl);

		// Wins list
		winsListPanel.setLayout(new BoxLayout(winsListPanel, BoxLayout.Y_AXIS));
		winsListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane winsScroll = new JScrollPane(winsListPanel);
		winsScroll.setBorder(titledBorder("Wins (validated loot keys)", WIN_COLOR));
		winsScroll.setPreferredSize(new Dimension(200, 220));

		// Loses list
		losesListPanel.setLayout(new BoxLayout(losesListPanel, BoxLayout.Y_AXIS));
		losesListPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane losesScroll = new JScrollPane(losesListPanel);
		losesScroll.setBorder(titledBorder("Loses (deaths)", LOSE_COLOR));
		losesScroll.setPreferredSize(new Dimension(200, 150));

		JPanel center = new JPanel();
		center.setBackground(ColorScheme.DARK_GRAY_COLOR);
		center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
		center.add(summary);
		center.add(Box.createVerticalStrut(8));
		center.add(winsScroll);
		center.add(Box.createVerticalStrut(6));
		center.add(losesScroll);

		add(center, BorderLayout.CENTER);
		refresh();
	}

	public void refresh()
	{
		SwingUtilities.invokeLater(this::doRefresh);
	}

	private void doRefresh()
	{
		long totalWin = tracker.getKills().stream()
			.filter(k -> k.getStatus() == KillStatus.VALIDATED)
			.mapToLong(KillRecord::getGpValue)
			.sum();
		long totalLose = tracker.getDeaths().stream()
			.mapToLong(d -> d.getLostValueEstimate())
			.sum();
		long net = totalWin - totalLose;

		totalWinLbl.setText(Formats.gp(totalWin));
		totalLoseLbl.setText(Formats.gp(totalLose));
		netLbl.setText(Formats.signedGp(net));
		netLbl.setForeground(net >= 0 ? WIN_COLOR : LOSE_COLOR);

		// Wins list
		winsListPanel.removeAll();
		List<KillRecord> wins = new ArrayList<>(tracker.getKills());
		wins.removeIf(k -> k.getStatus() != KillStatus.VALIDATED);
		wins.sort(Comparator.comparing(KillRecord::getAt, Comparator.nullsLast(Comparator.reverseOrder())));
		for (KillRecord k : wins)
		{
			winsListPanel.add(winRow(k));
		}
		if (wins.isEmpty())
		{
			winsListPanel.add(emptyHint("No validated kills yet"));
		}

		// Loses list
		losesListPanel.removeAll();
		List<DeathRecord> deaths = new ArrayList<>(tracker.getDeaths());
		deaths.sort(Comparator.comparing(DeathRecord::getAt, Comparator.nullsLast(Comparator.reverseOrder())));
		for (DeathRecord d : deaths)
		{
			losesListPanel.add(loseRow(d));
		}
		if (deaths.isEmpty())
		{
			losesListPanel.add(emptyHint("No deaths recorded"));
		}

		winsListPanel.revalidate();
		winsListPanel.repaint();
		losesListPanel.revalidate();
		losesListPanel.repaint();
	}

	private JPanel winRow(KillRecord k)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		JLabel name = new JLabel(k.getVictim() != null ? k.getVictim() : "?");
		name.setForeground(Color.WHITE);
		JLabel gp = new JLabel("+" + Formats.gp(k.getGpValue()), SwingConstants.RIGHT);
		gp.setForeground(WIN_COLOR);
		JLabel time = new JLabel(Formats.ago(k.getAt()), SwingConstants.RIGHT);
		time.setForeground(Color.LIGHT_GRAY);
		JPanel right = new JPanel(new GridLayout(1, 2, 4, 0));
		right.setBackground(ColorScheme.DARK_GRAY_COLOR);
		right.add(time);
		right.add(gp);
		row.add(name, BorderLayout.CENTER);
		row.add(right, BorderLayout.EAST);
		return row;
	}

	private JPanel loseRow(DeathRecord d)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
		JLabel name = new JLabel(d.getKiller() != null ? d.getKiller() : "?");
		name.setForeground(Color.WHITE);
		long lost = d.getLostValueEstimate();
		JLabel gp = new JLabel(lost > 0 ? "-" + Formats.gp(lost) : "~lost", SwingConstants.RIGHT);
		gp.setForeground(LOSE_COLOR);
		JLabel time = new JLabel(Formats.ago(d.getAt()), SwingConstants.RIGHT);
		time.setForeground(Color.LIGHT_GRAY);
		JPanel right = new JPanel(new GridLayout(1, 2, 4, 0));
		right.setBackground(ColorScheme.DARK_GRAY_COLOR);
		right.add(time);
		right.add(gp);
		row.add(name, BorderLayout.CENTER);
		row.add(right, BorderLayout.EAST);
		return row;
	}

	private JPanel emptyHint(String text)
	{
		JPanel p = new JPanel(new BorderLayout());
		p.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JLabel l = new JLabel(text, SwingConstants.CENTER);
		l.setForeground(Color.DARK_GRAY);
		l.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
		p.add(l, BorderLayout.CENTER);
		return p;
	}

	private static JLabel bigLabel(String text, Color color)
	{
		JLabel l = new JLabel(text, SwingConstants.CENTER);
		l.setForeground(color);
		l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
		return l;
	}

	private static JLabel sectionTitle(String text, Color color)
	{
		JLabel l = new JLabel(text, SwingConstants.CENTER);
		l.setForeground(color);
		l.setFont(l.getFont().deriveFont(Font.BOLD));
		return l;
	}

	private static javax.swing.border.Border titledBorder(String title, Color color)
	{
		return BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(color.darker(), 1),
			title);
	}
}
