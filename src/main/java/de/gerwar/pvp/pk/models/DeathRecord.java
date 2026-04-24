package de.gerwar.pvp.pk.models;

import java.time.Instant;
import lombok.Data;

@Data
public class DeathRecord
{
	private String id;
	private String killer;
	private Instant at;
	private int world;
	private long lostValueEstimate;
}
