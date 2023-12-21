package GameDemo;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Commands implements CommandExecutor {
	
	public void register() {
		Main.getPlugin(Main.class).getCommand("loadlevel").setExecutor(this);
		Main.getPlugin(Main.class).getCommand("savelevel").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) {
		if(cmd.getName().equalsIgnoreCase("loadlevel")) {
			if(args.length == 0) {
				sender.sendMessage(Utils.Chat("&c使用方法: /loadlevel <关卡>"));
			}else {
				if(Utils.doesLevelExist(Integer.valueOf(args[0]))) {
					Utils.loadLevel(Integer.valueOf(args[0]));
					sender.sendMessage(Utils.Chat("&a加载成功!"));
				}else {
					sender.sendMessage(Utils.Chat("&c无法找到关卡!"));
				}
			}
		}else if(cmd.getName().equalsIgnoreCase("savelevel")) {
			Player p = (Player) sender;
			p.setFlying(true);
			if(p.getGameMode() == GameMode.CREATIVE) {
				p.teleport(new Location(Bukkit.getWorld("world"), -50, 0, -100));
				new BukkitRunnable() {
					@Override
					public void run() {
						p.chat("//1");
						new BukkitRunnable() {
							@Override
							public void run() {
								p.teleport(new Location(Bukkit.getWorld("world"), 50, 128, 100));
								new BukkitRunnable() {
									@Override
									public void run() {
										p.chat("//2");
										new BukkitRunnable() {
											@Override
											public void run() {
												p.teleport(new Location(Bukkit.getWorld("world"), 0, 32, 0));
												new BukkitRunnable() {
													@Override
													public void run() {
														p.chat("//copy");
														p.setFlying(false);
														new BukkitRunnable() {
															@Override
															public void run() {
																p.chat("//schem save level -f");
																p.sendMessage(Utils.Chat("&a正在保存检测器信息"));
																JsonArray devices = new JsonArray();
																World w = Bukkit.getWorld("world");
																for(int x = -50;x <= 50;x++) {
																	for(int y = 0;y <= 128;y++) {
																		for(int z = -100;z <= 100;z++) {
																			Block b = w.getBlockAt(x, y, z);
																			if(b.getType() == Material.BLUE_CONCRETE) {
																				JsonObject water = new JsonObject();
																				water.addProperty("type", "water");
																				water.addProperty("loc", x + "," + y + "," + z);
																				devices.add(water);
																			}else if(b.getType() == Material.LIGHT_BLUE_CONCRETE) {
																				JsonObject ice = new JsonObject();
																				ice.addProperty("type", "ice");
																				ice.addProperty("loc", x + "," + y + "," + z);
																				devices.add(ice);
																			}else if((b.getType() == Material.WARPED_WALL_SIGN || b.getType() == Material.CRIMSON_WALL_SIGN) &&
																					Utils.getAdhereBlockLocations(new Location(w, x, y, z), Material.ACACIA_TRAPDOOR).size() == 4) {
																				JsonObject fan = new JsonObject();
																				fan.addProperty("type", "fan");
																				fan.addProperty("loc", x + "," + y + "," + z);
																				fan.addProperty("facing", ((WallSign) b.getBlockData()).getFacing().toString());
																				fan.addProperty("range", ((Sign) b.getState()).getLine(0));
																				fan.addProperty("running", b.getType() == Material.WARPED_WALL_SIGN ? true : false);
																				devices.add(fan);
																			}else if(b.getType() == Material.IRON_BARS || b.getType() == Material.GLASS_PANE) {
																				if(Utils.getAdhereBlockLocations(new Location(w, x, y, z), Material.WHITE_CONCRETE).size() == 4) {
																					JsonObject vapor = new JsonObject();
																					vapor.addProperty("type", "vapor");
																					vapor.addProperty("mode", b.getType() == Material.IRON_BARS ? "remove" : "pass");
																					vapor.addProperty("loc", x + "," + y + "," + z);
																					devices.add(vapor);
																				}
																			}
																		}
																	}
																}
																Utils.write(devices.toString(), new File("plugins\\FastAsyncWorldEdit\\schematics\\devices.json"));
																p.sendMessage(Utils.Chat("&a检测器信息保存成功"));
															}
														}.runTaskLater(Main.getPlugin(Main.class), 50L);
													}
												}.runTaskLater(Main.getPlugin(Main.class), 10L);
											}
										}.runTaskLater(Main.getPlugin(Main.class), 10L);
									}
								}.runTaskLater(Main.getPlugin(Main.class), 10L);
							}
						}.runTaskLater(Main.getPlugin(Main.class), 10L);
					}
				}.runTaskLater(Main.getPlugin(Main.class), 10L);
			}else {
				p.sendMessage(Utils.Chat("&c你必须在创造模式才能使用这个指令!"));
			}
		}
		return true;
	}
}
