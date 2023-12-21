package GameDemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import GameDemo.Helper.Detector;
import GameDemo.Helper.Fan;
import GameDemo.Helper.PlayerData;
import GameDemo.Helper.ToolMode;
import GameDemo.Helper.Vapor;

public class Main extends JavaPlugin implements Listener {
	
	public static HashMap<Player, PlayerData> data = new HashMap<>();
	public static List<Vapor> vapors = new CopyOnWriteArrayList<>();
	public static List<Detector> detectors = new CopyOnWriteArrayList<>();
	public static List<Fan> fans = new CopyOnWriteArrayList<>();
	public static List<BukkitTask> runnables = new CopyOnWriteArrayList<>();
	
	public static Random rand = new Random();
	
	public static int currentLevel = 1;
	
	public static boolean isPlaying = false;
	
	public void onEnable(){
		Bukkit.getConsoleSender().sendMessage(Utils.Chat("&aGameDemo Enabled"));
		Bukkit.getPluginManager().registerEvents(this, this);
		new Commands().register();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				boolean flag = true;
				for(Player p : Bukkit.getOnlinePlayers()) {
					p.sendActionBar(Utils.Chat("&a&lCurrent Mode: &b&l" + data.get(p).mode.name + " &7(Press 'Q' to switch)"));
					if(p.getGameMode() == GameMode.SURVIVAL) {
						flag = false;
					}
				}
				
				if(flag || !Utils.doesLevelExist(currentLevel)) {
					Main.isPlaying = false;
				}
				
				for(Vapor v : vapors) {
					v.update();
					for(Location loc : v.locs) {
						if(loc.getY() >= v.maxY) {
							vapors.remove(v);
							break;
						}
					}
				}
				
				for(Detector d : detectors) {
					if(d.type.equals("water")) {
						boolean hasWater = Utils.getAdhereBlockLocations(d.loc, Material.WATER).size() > 0;
						if(d.activated && !hasWater) {
							d.activated = false;
						}else if(!d.activated && hasWater){
							d.activated = true;
							for(Location lamp : Utils.getAdhereBlockLocations(d.loc, Material.REDSTONE_LAMP)) {
								Utils.processRedstoneLamp(lamp);
							}
						}
					}else if(d.type.equals("ice")) {
						boolean hasIce = Utils.getAdhereBlockLocations(d.loc, Material.ICE).size() > 0;
						if(d.activated && !hasIce) {
							d.activated = false;
						}else if(!d.activated && hasIce){
							d.activated = true;
							for(Location lamp : Utils.getAdhereBlockLocations(d.loc, Material.REDSTONE_LAMP)) {
								Utils.processRedstoneLamp(lamp);
							}
						}
					}else if(d.type.equals("vapor")) {
						boolean hasVapor = false;
						Vapor chosen = null;
						for(Vapor v : vapors) {
							for(Location vloc : v.locs) {
								if(Math.abs((d.loc.getBlockY() + 1) - vloc.getBlockY()) <= 1 && vloc.distanceSquared(d.loc) <= 4) {
									hasVapor = true;
									chosen = v;
									break;
								}
							}
						}
						
						if(d.activated && !hasVapor) {
							d.activated = false;
						}else if(!d.activated && hasVapor){
							d.activated = true;
							if(d.mode.equals("remove")) {
								vapors.remove(chosen);
							}
							for(Location concrete : Utils.getAdhereBlockLocations(d.loc, Material.WHITE_CONCRETE)) {
								for(Location lamp : Utils.getAdhereBlockLocations(concrete, Material.REDSTONE_LAMP)) {
									Utils.processRedstoneLamp(lamp);
								}
							}
						}
					}
				}
				
				for(Fan f : fans) {
					if(f.isRunning) {
						float oX = 0, oZ = 0;
						if(f.facing == BlockFace.NORTH) {
							oZ = -1F;
						}else if(f.facing == BlockFace.EAST) {
							oX = 1F;
						}else if(f.facing == BlockFace.SOUTH) {
							oZ = 1F;
						}else if(f.facing == BlockFace.WEST) {
							oX = -1F;
						}
						
						for(int i = 0;i < rand.nextInt(3) + 2;i++) {
							f.loc.getWorld().spawnParticle(Particle.SQUID_INK, 
									f.loc.clone().add(rand.nextFloat() * 2 - 1F, rand.nextFloat() * 3 - 1F, rand.nextFloat() * 2 - 1F),
									0, oX, 0.05, oZ, 0.75);
						}
						
						List<Vapor> pushed = new ArrayList<>();
						for(int i = 0;i < f.range;i++) {
							for(Vapor v : vapors) {
								if(!pushed.contains(v)) {
									for(Location vloc : v.locs) {
										if(vloc.getBlockY() == f.loc.getBlockY() && vloc.distanceSquared(f.loc.clone().add(oX * i, 0, oZ * i)) <= 4) {
											for(Location l : v.locs) {
												l.add(oX / 3F, 0, oZ / 3F);
											}
											v.lastFanPush = System.currentTimeMillis();
											pushed.add(v);
											break;
										}
									}
								}
							}
						}
					}
				}
			}
		}.runTaskTimer(this, 2L, 2L);
	}
	
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage(Utils.Chat("&cGameDemo Disabled"));
	}
	
	@EventHandler
	public static void PlayerJoinEvent(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		p.getInventory().clear();
		data.put(p, new PlayerData(p.getName()));
		
		if(Main.isPlaying) {
			p.setGameMode(GameMode.SPECTATOR);
			p.teleport(new Location(Bukkit.getWorld("world"), 0.5, 32, 0.5, 180, 0));
		}else {
			p.setGameMode(GameMode.SURVIVAL);
			p.teleport(new Location(Bukkit.getWorld("world"), 100.5, 129, 0.5, 180, 0));
			ItemStack start = new ItemStack(Material.SLIME_BALL);
			ItemMeta meta = start.getItemMeta();
			meta.setDisplayName(Utils.Chat("&aPress &f'Q' &ato start!"));
			start.setItemMeta(meta);
			p.getInventory().setItem(4, start);
			p.updateInventory();
		}
	}
	
	@EventHandler
	public static void PlayerQuitEvent(PlayerQuitEvent e) {
		data.remove(e.getPlayer());
	}
	
	@EventHandler
	public static void ProjectileLaunchEvent(ProjectileLaunchEvent e) {
		if(e.getEntityType() != EntityType.SNOWBALL) return;
		
		Snowball snowball = (Snowball) e.getEntity();
		Player p = (Player) snowball.getShooter();
		
		if(p.getGameMode() != GameMode.SURVIVAL) return;
		
		Utils.processSnowball(p, snowball);
	}
	
	@EventHandler
	public static void ProjectileHitEvent(ProjectileHitEvent e) {
		if(e.getEntityType() != EntityType.SNOWBALL) return;
		
		Block b = e.getHitBlock();
		if(b.getType() != Material.ICE) return;
		
		Snowball snowball = (Snowball) e.getEntity();
		Player p = (Player) snowball.getShooter();
		
		if(p.getGameMode() != GameMode.SURVIVAL) return;
		
		PlayerData pd = data.get(p);
		
		if(pd.mode == ToolMode.WATER) {
			if(Utils.getAllAdhereBlockLocations(b, 200, 256, 0).size() > 1) { // is connected with other ice blocks
				return;
			}
			
			b.setType(Material.AIR);
			int height = 0;
			List<Location> locs = Utils.getAllAdhereBlockLocations(b, 200, b.getY(), b.getY() - 2);
			if(locs.size() > 20) {
				b.setType(Material.ICE);
				return;
			}
			while(height < 20) {
				locs = Utils.getAllAdhereBlockLocations(b, 200, b.getY() + height, b.getY() - 2);
				if(locs.size() > 20) {
					height--;
					break;
				}
				height++;
			}
			locs = Utils.getAllAdhereBlockLocations(b, 200, b.getY() + height, b.getY() - 2);
			for(Location loc : locs) {
				loc.getBlock().setType(Material.WATER);
				loc.getWorld().spawnParticle(Particle.WATER_DROP, loc, 10);
			}
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_SWIM, 1F, 2F);
		}
	}
	
	@EventHandler
	public static void BlockFromToEvent(BlockFromToEvent e) {
		if(e.getBlock().getType() == Material.ICE && e.getToBlock().getType() == Material.WATER) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public static void BlockBreakEvent(BlockBreakEvent e) {
		if(e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
		
		e.setCancelled(true);
		if(e.getBlock().getType() == Material.ICE) {
			if(Utils.getAdhereBlockLocations(e.getBlock().getLocation(), Material.ICE).size() == 0) {
				return;
			}
			Utils.replaceAllAdhereBlocks(e.getBlock(), Material.AIR, 256, 0, null, 0);
			e.getBlock().setType(Material.AIR);
			e.getPlayer().getInventory().addItem(new ItemStack(Material.ICE));
		}
	}
	
	@EventHandler
	public static void BlockPlaceEvent(BlockPlaceEvent e) {
		if(e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
		
		if(e.getBlock().getType() == Material.ICE) {
			if(Utils.getAdhereBlockLocations(e.getBlock().getLocation(), Material.ICE).size() > 0) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public static void PlayerDropItemEvent(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if(p.getGameMode() != GameMode.SURVIVAL) return;
		
		if(e.getItemDrop().getItemStack().getType() == Material.ENDER_EYE) {
			e.getItemDrop().remove();
			Utils.clearLevel();
			Utils.loadLevel(currentLevel);
			Main.isPlaying = true;
			return;
		}
		
		if(e.getItemDrop().getItemStack().getType() == Material.SLIME_BALL) {
			e.getItemDrop().remove();
			Main.isPlaying = true;
			for(Player ps : Bukkit.getOnlinePlayers()) {
				ps.setGameMode(GameMode.SPECTATOR);
			}
			p.setGameMode(GameMode.SURVIVAL);
			Utils.loadLevel(1);
			return;
		}
		
		e.setCancelled(true);
		
		PlayerData pd = data.get(p);
		if(pd.mode == ToolMode.WATER) {
			pd.mode = ToolMode.ICE;
		}else if(pd.mode == ToolMode.ICE) {
			pd.mode = ToolMode.VAPOR;
		}else {
			pd.mode = ToolMode.WATER;
		}
		
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 2F);
	}
	
	@EventHandler
	public static void PlayerMoveEvent(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(p.getGameMode() != GameMode.SURVIVAL) return;
		
		if(p.getLocation().getBlock().getType() == Material.LIGHT_WEIGHTED_PRESSURE_PLATE &&
				p.getLocation().add(0, -1, 0).getBlock().getType() == Material.DIAMOND_BLOCK) {
			if(Utils.doesLevelExist(currentLevel + 1)) {
				p.setGameMode(GameMode.SPECTATOR);
				for(Player ps : Bukkit.getOnlinePlayers()) {
					ps.sendTitle(Utils.Chat("&bLevel Complete!"), "Congratulations!");
					ps.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
				}
				currentLevel++;
				BukkitTask run = new BukkitRunnable() {
					@Override
					public void run() {
						p.teleport(new Location(Bukkit.getWorld("world"), 100.5, 129, 0.5, 180, 0));
						p.setGameMode(GameMode.SURVIVAL);
						Utils.loadLevel(currentLevel);
					}
				}.runTaskLater(Main.getPlugin(Main.class), 100L);
				Main.runnables.add(run);
			}else {
				for(Player ps : Bukkit.getOnlinePlayers()) {
					ps.getInventory().clear();
					
					ItemStack start = new ItemStack(Material.SLIME_BALL);
					ItemMeta meta = start.getItemMeta();
					meta.setDisplayName(Utils.Chat("&aPress &f'Q' &ato start!"));
					start.setItemMeta(meta);
					ps.getInventory().setItem(4, start);
					ps.updateInventory();
					
					ps.setGameMode(GameMode.SURVIVAL);
					ps.teleport(new Location(Bukkit.getWorld("world"), 100.5, 129, 0.5, 180, 0));
					ps.sendTitle(Utils.Chat("&6You won!"), Utils.Chat("&aAll levels finished!"));
					ps.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
				}
			}
		}
	}
	
	@EventHandler
	public static void PlayerInteractEvent(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if(p.getGameMode() == GameMode.CREATIVE && e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.REDSTONE_LAMP && p.isSneaking()) {
			Utils.processRedstoneLamp(e.getClickedBlock().getLocation());
		}
	}
	
	@EventHandler
	public static void EntityDamageEvent(EntityDamageEvent e) {
		if(e.getEntityType() == EntityType.PLAYER && e.getCause() == DamageCause.FALL) {
			e.setCancelled(true);
		}
	}
	
}
