package de.gerwar.pvp.pk;

import de.gerwar.pvp.pk.models.DeathRecord;
import de.gerwar.pvp.pk.models.KillRecord;
import de.gerwar.pvp.pk.models.KillStatus;
import java.util.List;
import lombok.Value;

@Value
public class PkBalance
{
	int kills;
	int deaths;
	long totalGained;
	long totalLost;

	public double kdRatio()
	{
		if (deaths == 0)
		{
			return kills == 0 ? 0.0 : Double.POSITIVE_INFINITY;
		}
		return kills / (double) deaths;
	}

	public long net()
	{
		return totalGained - totalLost;
	}

	public static PkBalance compute(List<KillRecord> kills, List<DeathRecord> deaths)
	{
		long gained = kills.stream()
			.filter(k -> k.getStatus() == KillStatus.VALIDATED)
			.mapToLong(KillRecord::getGpValue)
			.sum();
		long lost = deaths.stream()
			.mapToLong(DeathRecord::getLostValueEstimate)
			.sum();
		return new PkBalance(kills.size(), deaths.size(), gained, lost);
	}
}
