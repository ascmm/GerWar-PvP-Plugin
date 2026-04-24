package de.gerwar.pvp.pk.models;

import java.time.Instant;
import lombok.Data;

@Data
public class KillRecord
{
	private String id;
	private String victim;
	private Instant at;
	private int world;
	private int combatLevel;
	private KillStatus status = KillStatus.PENDING;
	private long gpValue;
	private Instant validatedAt;
}
