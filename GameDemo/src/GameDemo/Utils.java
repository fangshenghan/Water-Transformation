package GameDemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.util.TaskManager;
import com.gmail.filoghost.holograms.api.Hologram;
import com.gmail.filoghost.holograms.api.HolographicDisplaysAPI;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sk89q.worldedit.math.BlockVector3;

import GameDemo.Helper.DataWatcher;
import GameDemo.Helper.Detector;
import GameDemo.Helper.Fan;
import GameDemo.Helper.PlayerData;
import GameDemo.Helper.ToolMode;
import GameDemo.Helper.Vapor;

public class Utils {
	
	public static Gson gson = new Gson();
	public static boolean levelLoaded = false;
	
	public static String Chat(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	public static void NoPerm(CommandSender sender) {
		sender.sendMessage(Utils.Chat("&4你没有权限执行这个命令"));
	}
	
	public static List<Location> getAdhereBlockLocations(Location loc, Material... m){
		List<Location> locs = new ArrayList<Location>();
		int x = loc.getBlockX();int y = loc.getBlockY();int z = loc.getBlockZ();
		Location loc1 = loc.clone();Location loc2 = loc.clone();Location loc3 = loc.clone();
		Location loc4 = loc.clone();Location loc5 = loc.clone();Location loc6 = loc.clone();
		
		loc1.setY(y - 1);loc2.setY(y + 1);loc3.setX(x + 1);loc4.setX(x - 1);loc5.setZ(z + 1);loc6.setZ(z - 1);
		
		List<Material> ml = Arrays.asList(m);
		
		if(ml.contains(loc1.getBlock().getType())) {
			locs.add(loc1);
		}
		if(ml.contains(loc2.getBlock().getType())) {
			locs.add(loc2);
		}
		if(ml.contains(loc3.getBlock().getType())) {
			locs.add(loc3);
		}
		if(ml.contains(loc4.getBlock().getType())) {
			locs.add(loc4);
		}
		if(ml.contains(loc5.getBlock().getType())) {
			locs.add(loc5);
		}
		if(ml.contains(loc6.getBlock().getType())) {
			locs.add(loc6);
		}
		return locs;
	}
	
	public static List<Location> getAllAdhereBlockLocations(Block b, int maxAmount, int maxY, int minY) {
		List<Location> foundLocations = new ArrayList<>();
		Queue<Location> locationsToExplore = new LinkedList<>();
		foundLocations.add(b.getLocation());
		locationsToExplore.add(b.getLocation());
		
		while(!locationsToExplore.isEmpty() && foundLocations.size() < maxAmount) {
			Location currentLoc = locationsToExplore.poll();
			
			for(Location adhere : Utils.getAdhereBlockLocations(currentLoc, currentLoc.getBlock().getType())) {
				if(adhere.getBlockY() > maxY || adhere.getBlockY() < minY) {
					continue;
				}
				if(!foundLocations.contains(adhere)) {
					foundLocations.add(adhere);
					locationsToExplore.add(adhere);
				}
			}
		}
		
		return foundLocations;
	}
	
	public static void replaceAllAdhereBlocks(Block from, Material to, int maxY, int minY, Particle particle, int particleAmount) {
		List<Location> locs = Utils.getAllAdhereBlockLocations(from, 200, maxY, minY);
		for(Location loc : locs) {
			loc.getBlock().setType(to);
			if(particle != null) {
				loc.getWorld().spawnParticle(particle, loc, particleAmount);
			}
		}
	}
	
	public static void processSnowball(final Player p, final Snowball ball) {
		final PlayerData pd = Main.data.get(p);
		BukkitTask run = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if(ball == null || ball.isDead()) {
						this.cancel();
						return;
					}
					
					if(ball.getLocation().getBlock().getType() == Material.WATER) {
						this.cancel();
						ball.remove();
						Utils.replaceAllAdhereBlocks(ball.getLocation().getBlock(), Material.ICE, 256, 0, Particle.FIREWORKS_SPARK, 20);
						p.playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 1.5F);
					}
					
					for(Vapor v : Main.vapors) {
						for(Location vl : v.locs) {
							if(vl.distanceSquared(ball.getLocation()) < 3) {
								ball.remove();
								p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1.5F);
								for(Location l : v.locs) {
									for(int i = 0;i < 8;i++) {
										l.getWorld().spawnParticle(Particle.FALLING_WATER, l.clone().add(Main.rand.nextFloat() * 2 - 1F, Main.rand.nextFloat() * 2 - 1F, Main.rand.nextFloat() * 2 - 1F),
												5, Main.rand.nextFloat() / 2.0F - 0.5, 0.05, Main.rand.nextFloat() / 2.0F - 0.5);
									}
								}
								
								int height = 0;
								Location ballLoc = ball.getLocation();
								while(ballLoc.getBlockY() > 0 && ballLoc.getBlock().getType() == Material.AIR) {
									ballLoc.add(0, -1, 0);
									height++;
								}
								
								BukkitTask bt = new BukkitRunnable() {
									@Override
									public void run() {
										for(Location l : v.locs) {
											while(l.getBlockY() > 0 && l.getBlock().getType() == Material.AIR) {
												l.add(0, -1, 0);
											}
											l.add(0, 1, 0);
											Block b = l.getBlock();
											int height = 0;
											List<Location> locs = Utils.getAllAdhereBlockLocations(b, 200, b.getY(), b.getY() - 2);
											if(locs.size() > 20) {
												continue;
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
								}.runTaskLater(Main.getPlugin(Main.class), (long) ((double) height * 2.5) - Math.max(0, height - 6)); // calc time to fall
								Main.runnables.add(bt);
								Main.vapors.remove(v);
								// spawn water particles and add water
								this.cancel();
								return;
							}
						}
					}
				}catch(Exception ex) {
					ex.printStackTrace();
					this.cancel();
				}
			}
		}.runTaskTimer(Main.getPlugin(Main.class), 1L, 1L);
		Main.runnables.add(run);
	}
	
	public static void processFireball(final Player p, final Fireball ball) {
		final PlayerData pd = Main.data.get(p);
		BukkitTask run = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if(ball == null || ball.isDead()) {
						this.cancel();
						return;
					}
					
					if(ball.getLocation().getBlock().getType() == Material.WATER) {
						this.cancel();
						ball.remove();
						Vapor v = new Vapor(Utils.getAdhereBlockLocations(ball.getLocation(), Material.WATER), 0.25D, ball.getLocation().getY() + 20F);
						Main.vapors.add(v);
						Utils.replaceAllAdhereBlocks(ball.getLocation().getBlock(), Material.AIR, 256, 0, Particle.FIREWORKS_SPARK, 20);
						p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1.5F);
					}
				}catch(Exception ex) {
					ex.printStackTrace();
					this.cancel();
				}
			}
		}.runTaskTimer(Main.getPlugin(Main.class), 1L, 1L);
		Main.runnables.add(run);
	}
	
	public static void processRedstoneLamp(Location start) {
		if(!(start.getBlock().getBlockData() instanceof Lightable)) return;
		Lightable l = (Lightable) start.getBlock().getBlockData();
		l.setLit(true);
		start.getBlock().setBlockData(l);
		
		List<Location> visited = new ArrayList<>();
		visited.add(start.toBlockLocation());
		List<Location> current = new ArrayList<>();
		current.add(start);
		
		BukkitTask run = new BukkitRunnable() {
			@Override
			public void run() {
				int moved = 0;
				for(int i = 0;i < current.size();i++) {
					Location c = current.get(i);
					Lightable ll = (Lightable) c.getBlock().getBlockData();
					ll.setLit(false);
					c.getBlock().setBlockData(ll);
					for(Location loc : Utils.getAdhereBlockLocations(c, Material.REDSTONE_LAMP)) {
						loc = loc.toBlockLocation();
						if(!visited.contains(loc)) {
							visited.add(loc);
							moved++;
							if(current.get(i).equals(c)) {
								current.set(i, loc);
							}else {
								current.add(i, loc);
							}
							
							Lightable l = (Lightable) loc.getBlock().getBlockData();
							l.setLit(true);
							loc.getBlock().setBlockData(l);
							
							List<Location> endings = Utils.getAdhereBlockLocations(loc, Material.RED_CONCRETE);
							if(endings.size() > 0) {
								for(Player p : Bukkit.getOnlinePlayers()) {
									p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
								}
							}
							for(Location el : endings) {
								el.getBlock().setType(Material.LIME_CONCRETE);
								
								List<Location> elevators = Utils.getAdhereBlockLocations(el, Material.LIGHT_BLUE_STAINED_GLASS);
								for(Location elevator : elevators) {
									Utils.processElevator(elevator, 2, 7);
								}
								
								List<Location> doors = Utils.getAdhereBlockLocations(el, Material.BLACK_STAINED_GLASS);
								for(Location door : doors) {
									Utils.processDoor(door, 3);
								}
								
								List<Location> fans = Utils.getAdhereBlockLocations(el, Material.LIGHT_GRAY_CONCRETE);
								for(Location fan : fans) {
									for(Location gray : Utils.getAdhereBlockLocations(fan, Material.GRAY_CONCRETE)) {
										for(Location sign : Utils.getAdhereBlockLocations(gray, Material.WARPED_WALL_SIGN, Material.CRIMSON_WALL_SIGN)) {
											Utils.changeFanState(sign, el);
										}
									}
								}
							}
						}
					}
				}
				if(moved == 0) {
					this.cancel();
				}
			}
		}.runTaskTimer(Main.getPlugin(Main.class), 4L, 4L);
		Main.runnables.add(run);
	}
	
	public static void processDoor(Location first, int speed) {
		List<Location> allDoors = Utils.getAllAdhereBlockLocations(first.getBlock(), 100, 256, 0);
		
		for(Location d : allDoors) {
			if(Utils.getAdhereBlockLocations(d, Material.RED_CONCRETE).size() > 0) {
				return;
			}
		}
		
		Location highest = null, lowest = null;
		for(Location loc : allDoors) {
			if(highest == null || loc.getY() > highest.getY()) {
				highest = loc;
			}
			if(lowest == null || loc.getY() < lowest.getY()) {
				lowest = loc;
			}
		}
		
		if(highest.clone().add(0, 1, 0).getBlock().getType() == Material.BARRIER) { // go up
			Utils.replaceAllAdhereBlocks(highest.clone().add(0, 1, 0).getBlock(), Material.AIR, 255, 0, null, 0);
			
			List<ArmorStand> stands = new ArrayList<>();
			
			for(Location l : allDoors) {
				l.getBlock().setType(Material.AIR);
				
				ArmorStand stand = (ArmorStand) l.getWorld().spawnEntity(l.add(0.5, -0.5, 0.5), EntityType.ARMOR_STAND);
				stand.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1000000, speed - 1, true));
				stand.setSmall(true);
				stand.setVisible(false);
				
				FallingBlock f = l.getWorld().spawnFallingBlock(l.add(0, 2, 0), Material.BLACK_STAINED_GLASS, (byte) 0);
				stand.setPassenger(f);
				stands.add(stand);
			}
			
			BukkitTask run = new BukkitRunnable() {
				@Override
				public void run() {
					boolean flag = false;
					for(ArmorStand as : stands) {
						if(as.getLocation().add(0, 1.4, 0).getBlock().getType() != Material.AIR || as.getLocation().getY() > first.getY() + 20) {
							flag = true;
							break;
						}
					}
					
					if(!flag) return;
					
					this.cancel();
					for(ArmorStand as : stands) {
						as.getEyeLocation().getBlock().setType(Material.BLACK_STAINED_GLASS);
						as.getPassenger().remove();
						as.remove();
					}
				}
			}.runTaskTimer(Main.getPlugin(Main.class), 1L, 1L);
			Main.runnables.add(run);
		}else { // go down
			final DataWatcher dw = new DataWatcher();
			dw.intData1 = lowest.getBlockY();
			dw.intData2 = highest.getBlockY();
			BukkitTask run = new BukkitRunnable() {
				@Override
				public void run() {
					for(Location l : allDoors) {
						if(l.getBlockY() == dw.intData1) {
							l.getBlock().setType(Material.AIR);
							l.getWorld().spawnFallingBlock(l.clone().add(0.5, 0, 0.5), Material.BLACK_STAINED_GLASS, (byte) 0);
						}
					}
					dw.intData1++;
					if(dw.intData1 > dw.intData2) {
						this.cancel();
						BukkitTask run = new BukkitRunnable() {
							@Override
							public void run() {
								for(Location l : allDoors) {
									l.getBlock().setType(Material.BARRIER);
								}
							}
						}.runTaskLater(Main.getPlugin(Main.class), 10L);
						Main.runnables.add(run);
					}
				}
			}.runTaskTimer(Main.getPlugin(Main.class), 3L, 3L);
			Main.runnables.add(run);
		}
		
		BukkitTask run = new BukkitRunnable() {
			@Override
			public void run() {
				for(Location d : allDoors) {
					for(Location red: Utils.getAdhereBlockLocations(d, Material.LIME_CONCRETE)) {
						red.getBlock().setType(Material.RED_CONCRETE);
					}
				}
			}
		}.runTaskLater(Main.getPlugin(Main.class), 100L);
		Main.runnables.add(run);
	}
	
	public static void processElevator(Location pad, int speed, int height) {
		List<Location> allPads = Utils.getAllAdhereBlockLocations(pad.getBlock(), 100, 256, 0);
		
		for(Location d : allPads) {
			if(Utils.getAdhereBlockLocations(d, Material.RED_CONCRETE).size() > 0) {
				return;
			}
		}
		
		List<ArmorStand> stands = new ArrayList<>();
		List<Player> players = new ArrayList<>();
		
		for(Location l : allPads) {
			for(Player p : l.getNearbyPlayers(1.5)) {
				if(!players.contains(p)) {
					p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1000000, speed - 1, true));
					players.add(p);
					p.setVelocity(new Vector(0, 0.02, 0));
				}
			}
			l.getBlock().setType(Material.AIR);
			
			ArmorStand stand = (ArmorStand) l.getWorld().spawnEntity(l.add(0.5, -0.5, 0.5), EntityType.ARMOR_STAND);
			stand.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 1000000, speed - 1, true));
			stand.setSmall(true);
			stand.setVisible(false);
			
			FallingBlock f = l.getWorld().spawnFallingBlock(l.add(0, 2, 0), Material.LIGHT_BLUE_STAINED_GLASS, (byte) 0);
			stand.setPassenger(f);
			stands.add(stand);
		}
		
		BukkitTask run = new BukkitRunnable() {
			@Override
			public void run() {
				if((int) stands.get(0).getEyeLocation().getY() >= (int) pad.getY() + height) {
					List<Location> locs = new ArrayList<>();
					for(ArmorStand as : stands) {
						as.getEyeLocation().getBlock().setType(Material.LIGHT_BLUE_STAINED_GLASS);
						as.getPassenger().remove();
						as.remove();
						locs.add(as.getEyeLocation().getBlock().getLocation());
					}
					for(Player p : players) {
						p.removePotionEffect(PotionEffectType.LEVITATION);
					}
					this.cancel();
					BukkitTask run = new BukkitRunnable() {
						@Override
						public void run() {
							for(Location l : locs) {
								l.getBlock().setType(Material.AIR);
								l.getWorld().spawnFallingBlock(l.add(0.5, 0, 0.5), Material.LIGHT_BLUE_STAINED_GLASS, (byte) 0);
							}
							BukkitTask run = new BukkitRunnable() {
								@Override
								public void run() {
									for(Location ll : Utils.getAdhereBlockLocations(pad, Material.LIME_CONCRETE)) {
										ll.getBlock().setType(Material.RED_CONCRETE);
									}
								}
							}.runTaskLater(Main.getPlugin(Main.class), 60L);
							Main.runnables.add(run);
						}
					}.runTaskLater(Main.getPlugin(Main.class), 100L);
					Main.runnables.add(run);
				}
			}
		}.runTaskTimer(Main.getPlugin(Main.class), 1L, 1L);
		Main.runnables.add(run);
	}
	
	public static void changeFanState(Location sign, Location concrete) {
		for(Fan f : Main.fans) {
			if(f.loc.getBlock().equals(sign.getBlock())) {
				f.isRunning = !f.isRunning;
				if(f.isRunning) {
					sign.getBlock().setType(Material.WARPED_WALL_SIGN);
				}else {
					sign.getBlock().setType(Material.CRIMSON_WALL_SIGN);
				}
				WallSign ws = (WallSign) sign.getBlock().getBlockData();
				ws.setFacing(f.facing);
				sign.getBlock().setBlockData(ws);
				
				BukkitTask run = new BukkitRunnable() {
					@Override
					public void run() {
						concrete.getBlock().setType(Material.RED_CONCRETE);
					}
				}.runTaskLater(Main.getPlugin(Main.class), 60L);
				Main.runnables.add(run);
				
				return;
			}
		}
	}
	
	public static void pasteSchematic(String path, Location location, boolean async) {
		try {
			Utils.levelLoaded = false;
			if(async) {
				TaskManager.IMP.async(() -> {
		            try {
		                BlockVector3 vec = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		                FaweAPI.load(new File(path)).paste(FaweAPI.getWorld(location.getWorld().getName()), vec);
		                Utils.levelLoaded = true;
		            }catch(Exception ex) {
		                ex.printStackTrace();
		            }
		        });
			}else {
				 try {
	                BlockVector3 vec = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	                FaweAPI.load(new File(path)).paste(FaweAPI.getWorld(location.getWorld().getName()), vec);
	                Utils.levelLoaded = true;
	            }catch(Exception ex) {
	                ex.printStackTrace();
	            }
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void clearLevel() {
		for(Entity e : Bukkit.getWorld("world").getEntities()) {
			if(e.getType() != EntityType.PLAYER) {
				e.remove();
			}
		}
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			for(PotionEffect pe : p.getActivePotionEffects()) {
				p.removePotionEffect(pe.getType());
			}
		}
		
		Main.vapors.clear();
		Main.detectors.clear();
		Main.fans.clear();
		
		for(BukkitTask bt : Main.runnables) {
			bt.cancel();
		}
		Main.runnables.clear();
		
		for(Hologram h : HolographicDisplaysAPI.getHolograms(Main.getPlugin(Main.class))) {
			h.delete();
		}
	}
	
	public static boolean doesLevelExist(int level) {
		String path = "plugins\\GameDemo\\Level" + level;

		if(!new File(path).exists()) {
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("deprecation")
	public static void loadLevel(int level) {
		if(!Utils.doesLevelExist(level)) {
			return;
		}
		
		Utils.clearLevel();
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			p.teleport(new Location(Bukkit.getWorld("world"), 100.5, 129, 0.5, 180, 0));
			p.sendTitle(Utils.Chat("Loading Level..."), Utils.Chat("&7Please wait..."));
		}
		
		String path = "plugins\\GameDemo\\Level" + level;
		
		JsonObject json = gson.fromJson(Utils.read(path + "\\level.json"), JsonObject.class);
		JsonArray holograms = gson.fromJson(Utils.read(path + "\\holograms.json"), JsonArray.class);
		JsonArray devices = gson.fromJson(Utils.read(path + "\\devices.json"), JsonArray.class);
		Utils.pasteSchematic(path + "\\level.schem", new Location(Bukkit.getWorld("world"), 0, 32, 0), true);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!Utils.levelLoaded) return;
				this.cancel();
				
				for(JsonElement h : holograms) {
					JsonObject o = h.getAsJsonObject();
					String[] split = o.get("loc").getAsString().split(",");
					HolographicDisplaysAPI.createHologram(Main.getPlugin(Main.class), 
							new Location(Bukkit.getWorld("world"), 
									Float.valueOf(split[0]), 
									Float.valueOf(split[1]), 
									Float.valueOf(split[2])), 
							Utils.Chat(o.get("text").getAsString()));
				}
				
				for(JsonElement h : devices) {
					JsonObject o = h.getAsJsonObject();
					String[] split = o.get("loc").getAsString().split(",");
					if(o.get("type").getAsString().equals("ice") || o.get("type").getAsString().equals("water")) {
						Main.detectors.add(new Detector(new Location(Bukkit.getWorld("world"), 
								Integer.valueOf(split[0]), 
								Integer.valueOf(split[1]), 
								Integer.valueOf(split[2])), o.get("type").getAsString()));
					}else if(o.get("type").getAsString().equals("vapor")) {
						Main.detectors.add(new Detector(new Location(Bukkit.getWorld("world"), 
								Integer.valueOf(split[0]), 
								Integer.valueOf(split[1]), 
								Integer.valueOf(split[2])), "vapor", o.get("mode").getAsString()));
					}else if(o.get("type").getAsString().equals("fan")) {
						Main.fans.add(new Fan(new Location(Bukkit.getWorld("world"), 
								Integer.valueOf(split[0]), 
								Integer.valueOf(split[1]), 
								Integer.valueOf(split[2])), 
								BlockFace.valueOf(o.get("facing").getAsString()),
								o.get("range").getAsInt(), o.get("running").getAsBoolean()));
					}
				}
				
				Main.currentLevel = level;
				
				for(Player p : Bukkit.getOnlinePlayers()) {
					Main.data.put(p, new PlayerData(p.getName()));
					if(p.getGameMode() == GameMode.SURVIVAL) {
						p.getInventory().clear();
						p.getInventory().addItem(new ItemStack(Material.SNOWBALL, json.get("snowballs").getAsInt()));
						
						ItemStack eye = new ItemStack(Material.MAP);
						ItemMeta meta = eye.getItemMeta();
						meta.setDisplayName(Utils.Chat("&aReset Level"));
						eye.setItemMeta(meta);
						p.getInventory().setItem(8, eye);
						p.updateInventory();
					}
					String[] split = json.get("spawn").getAsString().split(",");
					p.teleport(new Location(Bukkit.getWorld("world"), Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]), Integer.valueOf(split[4])));
					p.setHealth(p.getMaxHealth());
					p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
					p.sendTitle(Utils.Chat("&aLevel " + level), Utils.Chat(json.get("subtitle").getAsString()));
				}
			}
		}.runTaskTimer(Main.getPlugin(Main.class), 2L, 2L);
	}
	
	public static String read(String path) {
		File file = new File(path);
		InputStreamReader isr;
		try {
			isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			StringBuffer res = new StringBuffer();
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					res.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return res.toString();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return "";
	}
	
	public static boolean write(String cont, File dist) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(dist), "UTF-8");
			BufferedWriter writer = new BufferedWriter(osw);
			writer.write(cont);
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}
