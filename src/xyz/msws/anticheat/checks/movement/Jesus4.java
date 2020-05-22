package xyz.msws.anticheat.checks.movement;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import xyz.msws.anticheat.NOPE;
import xyz.msws.anticheat.checks.Check;
import xyz.msws.anticheat.checks.CheckType;
import xyz.msws.anticheat.data.CPlayer;

/**
 * 
 * @author imodm
 *
 */
public class Jesus4 implements Check, Listener {

	private NOPE plugin;

	@Override
	public CheckType getType() {
		return CheckType.MOVEMENT;
	}

	@Override
	public void register(NOPE plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		CPlayer cp = plugin.getCPlayer(player);

		if (!player.isOnGround())
			return;

		if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().isSolid())
			return;
		if (!player.getLocation().getBlock().isLiquid())
			return;
		if (player.getLocation().add(0, 1, 0).getBlock().isLiquid())
			return;

		cp.flagHack(this, 50);

	}

	@Override
	public String getCategory() {
		return "Jesus";
	}

	@Override
	public String getDebugName() {
		return getCategory() + "#4";
	}

	@Override
	public boolean lagBack() {
		return true;
	}
}