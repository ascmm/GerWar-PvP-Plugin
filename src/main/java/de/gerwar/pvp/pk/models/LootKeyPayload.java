package de.gerwar.pvp.pk.models;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LootKeyPayload
{
	private final String sourcePlayer;
	private final long totalGp;
	private final Instant openedAt;
	private final List<String> itemNames;

	public static LootKeyPayload of(String source, long gp)
	{
		return new LootKeyPayload(source, gp, Instant.now(), Collections.emptyList());
	}
}
