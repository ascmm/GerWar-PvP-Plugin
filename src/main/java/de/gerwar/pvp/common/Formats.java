package de.gerwar.pvp.common;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;

public final class Formats
{
	private Formats()
	{
	}

	private static final NumberFormat INT = NumberFormat.getIntegerInstance();
	private static final DecimalFormat RATIO = new DecimalFormat("0.00");
	private static final DecimalFormat PERCENT = new DecimalFormat("0.0%");

	public static String gp(long amount)
	{
		long abs = Math.abs(amount);
		String sign = amount < 0 ? "-" : "";
		if (abs >= 1_000_000_000L)
		{
			return sign + new DecimalFormat("0.00B").format(abs / 1_000_000_000d);
		}
		if (abs >= 1_000_000L)
		{
			return sign + new DecimalFormat("0.00M").format(abs / 1_000_000d);
		}
		if (abs >= 10_000L)
		{
			return sign + new DecimalFormat("0.0K").format(abs / 1_000d);
		}
		return sign + INT.format(abs);
	}

	public static String signedGp(long amount)
	{
		if (amount > 0)
		{
			return "+" + gp(amount);
		}
		return gp(amount);
	}

	public static String ratio(double r)
	{
		if (!Double.isFinite(r))
		{
			return "∞";
		}
		return RATIO.format(r);
	}

	public static String percent(double p)
	{
		if (!Double.isFinite(p))
		{
			return "-";
		}
		return PERCENT.format(p);
	}

	public static String ago(Instant at)
	{
		if (at == null)
		{
			return "-";
		}
		Duration d = Duration.between(at, Instant.now());
		long s = Math.max(0, d.getSeconds());
		if (s < 60)
		{
			return s + "s ago";
		}
		if (s < 3600)
		{
			return (s / 60) + "m ago";
		}
		if (s < 86400)
		{
			return (s / 3600) + "h ago";
		}
		return (s / 86400) + "d ago";
	}
}
