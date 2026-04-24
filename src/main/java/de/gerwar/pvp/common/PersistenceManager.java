package de.gerwar.pvp.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import net.runelite.client.RuneLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceManager
{
	private static final Logger log = LoggerFactory.getLogger(PersistenceManager.class);

	private final Gson gson;
	private final Path dataDir;

	public PersistenceManager()
	{
		this.gson = new GsonBuilder()
			.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
			.setPrettyPrinting()
			.create();
		this.dataDir = RuneLite.RUNELITE_DIR.toPath().resolve("gerwar-pvp");
		try
		{
			Files.createDirectories(dataDir);
		}
		catch (IOException e)
		{
			log.warn("Could not create data dir {}", dataDir, e);
		}
	}

	public Path dataDir()
	{
		return dataDir;
	}

	public Path file(String fileName)
	{
		return dataDir.resolve(fileName);
	}

	public <T> T read(String fileName, Type type, T fallback)
	{
		Path f = file(fileName);
		if (!Files.exists(f))
		{
			return fallback;
		}
		try (Reader r = Files.newBufferedReader(f, StandardCharsets.UTF_8))
		{
			T value = gson.fromJson(r, type);
			return value != null ? value : fallback;
		}
		catch (Exception e)
		{
			log.warn("Failed to read {} — returning fallback", f, e);
			return fallback;
		}
	}

	public <T> List<T> readList(String fileName, Class<T> elementClass)
	{
		Type type = TypeToken.getParameterized(List.class, elementClass).getType();
		return read(fileName, type, Collections.emptyList());
	}

	public void write(String fileName, Object value)
	{
		Path f = file(fileName);
		Path tmp = f.resolveSibling(fileName + ".tmp");
		try (Writer w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8))
		{
			gson.toJson(value, w);
		}
		catch (IOException e)
		{
			log.warn("Failed to write {}", tmp, e);
			return;
		}
		try
		{
			Files.move(tmp, f, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
		}
		catch (IOException e)
		{
			log.warn("Failed to rename {} to {}", tmp, f, e);
		}
	}

	public Gson gson()
	{
		return gson;
	}
}
