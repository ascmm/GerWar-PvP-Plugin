package de.gerwar.pvp.common;

import java.util.List;

public final class MarkdownFormatter
{
	private MarkdownFormatter()
	{
	}

	public static String table(List<String> headers, List<List<String>> rows)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
		sb.append("|");
		for (int i = 0; i < headers.size(); i++)
		{
			sb.append(" --- |");
		}
		sb.append('\n');
		for (List<String> row : rows)
		{
			sb.append("| ").append(String.join(" | ", row)).append(" |\n");
		}
		return sb.toString();
	}

	public static String codeBlock(String content)
	{
		return "```\n" + content + "\n```";
	}
}
