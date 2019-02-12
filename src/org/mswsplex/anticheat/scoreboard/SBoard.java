package org.mswsplex.anticheat.scoreboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.mswsplex.anticheat.data.CPlayer;
import org.mswsplex.anticheat.msws.AntiCheat;
import org.mswsplex.anticheat.utils.MSG;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Ordering;

public class SBoard {
	Scoreboard board;
	ConfigurationSection scoreboard;

	int length = 25;
	String name, prefix = "";

	private AntiCheat plugin;

	public SBoard(AntiCheat plugin) {
		this.plugin = plugin;
		scoreboard = plugin.config.getConfigurationSection("Scoreboard");

		vlRankings = new ArrayList<>();

		name = "&c&lAntiCheat Violations";

		register();
	}

	private List<String> vlRankings;

	public void register() {
		new BukkitRunnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				vlRankings = new ArrayList<>();
				Map<Player, Integer> ranks = new HashMap<>();

				for (Player player : Bukkit.getOnlinePlayers()) {
					CPlayer cp = plugin.getCPlayer(player);
					if (cp.getTotalVL() == 0)
						continue;
					ranks.put(player, cp.getTotalVL());
				}

				ranks = ImmutableSortedMap.copyOf(ranks, Ordering.natural().onResultOf(Functions.forMap(ranks)));

				for (int i = 0; i < ranks.size(); i++) {
					Player player = (Player) ranks.keySet().toArray()[i];
					CPlayer cp = plugin.getCPlayer(player);

					int highVl = cp.getSaveInteger("vls." + cp.getHighestHack());

					String addon = MSG.getVlColor(highVl) + (i + 1) + ": &7" + player.getName() + " &e"
							+ cp.getHighestHack() + " " + MSG.getVlColor(highVl)
							+ cp.getSaveInteger("vls." + cp.getHighestHack());

					if (cp.hasSaveData("isBanwaved")) {
						addon += " &d[BW]";
					}
					vlRankings.add(addon);
				}

				if (vlRankings.isEmpty()) {
					vlRankings.add("&cNo Violations");
				}

				if (vlRankings.size() < 13) {
					vlRankings.add(" ");
					vlRankings.add("&7Next Banwave");
					vlRankings.add("&e" + MSG.getTime((double) plugin.getBanwave().timeToNextBanwave()));
				} else if (vlRankings.size() < 14) {
					vlRankings.add(" ");
					vlRankings.add("&7Next Banwave &e" + MSG.getTime((double) plugin.getBanwave().timeToNextBanwave()));
				} else {
					vlRankings.add("&7Next Banwave &e" + MSG.getTime((double) plugin.getBanwave().timeToNextBanwave()));
				}

				vlRankings = flip(vlRankings.subList(0, Math.min(vlRankings.size(), 15)));

				for (Player player : Bukkit.getOnlinePlayers()) {
					CPlayer cp = plugin.getCPlayer(player);
					if (cp.hasSaveData("scoreboard") && !cp.getSaveData("scoreboard", Boolean.class))
						continue;

					List<String> lines = new ArrayList<String>();
					board = player.getScoreboard();
					// Anti Lag/Flash Scoreboard functions

					if (board != null && player.getScoreboard().getObjective("anticheat") != null
							&& cp.hasTempData("oldLines")) {
						if (board.getObjectives().size() > vlRankings.size()) {
							continue;
						}
						Objective obj = board.getObjective("anticheat");
						List<String> oldLines = cp.getTempData("oldLines", List.class);
						for (int i = 0; i < 15 && i < vlRankings.size() && i < oldLines.size(); i++) {
							String sLine = parse(player, vlRankings.get(i));
							lines.add(sLine);
							if (board.getEntries().contains(sLine))
								continue;
							board.resetScores(parse(player, oldLines.get(i)));
							obj.getScore(sLine).setScore(i + 1);
						}
						name = parse(player, name);
						cp.setTempData("oldLines", lines);
						// obj.setDisplaySlot(DisplaySlot.SIDEBAR);
					} else {
						board = Bukkit.getScoreboardManager().getNewScoreboard();
						Objective obj = board.registerNewObjective("anticheat", "dummy");
						player.setScoreboard(board);
						obj.setDisplaySlot(DisplaySlot.SIDEBAR);
						obj.setDisplayName(MSG.color(name));
						int pos = 1;
						for (String res : vlRankings) {
							String line = parse(player, res);
							line = line.substring(0, Math.min(40, line.length()));
							obj.getScore(line).setScore(pos);
							lines.add(line);
							if (pos >= 15 || pos >= vlRankings.size())
								break;
							pos++;
						}
						cp.setTempData("oldLines", lines);
					}
					if (board.getEntries().size() > vlRankings.size())
						refresh(player);
				}
			}
		}.runTaskTimer(plugin, 0, 1);
	}

	private List<String> flip(List<String> array) {
		List<String> result = new ArrayList<String>();
		for (int i = array.size() - 1; i >= 0; i--) {
			result.add(array.get(i));
		}
		return result;
	}

	private String parse(Player player, String entry) {
		return MSG.color(entry.substring(0, Math.min(40, entry.length())));
	}

	private void refresh(Player player) {
		for (String res : board.getEntries()) {
			boolean keep = false;
			for (String line : vlRankings) {
				if (parse(player, res).equals(parse(player, line)))
					keep = true;
			}
			if (!keep)
				board.resetScores(res);
		}
	}
}