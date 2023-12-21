package GameDemo.Helper;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;

import GameDemo.Main;

public class Vapor {
	
	public List<Location> locs;
	public double riseSpeed = 0.25F;
	public double maxY = 256;
	
	public long lastFanPush = 0;
	
	public Vapor(List<Location> locs, double riseSpeed, double maxY) {
		this.locs = locs;
		this.riseSpeed = riseSpeed;
		this.maxY = maxY;
	}
	
	public void update() {
		for(Location loc : locs) {
			for(int i = 0;i < 2;i++) {
				loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc.clone().add(Main.rand.nextFloat() - 0.5F, Main.rand.nextFloat() - 0.5F, Main.rand.nextFloat() - 0.5F),
						0, Main.rand.nextFloat() / 8.0F - 0.0625F, 0.05, Main.rand.nextFloat() / 8.0F - 0.0625F, 2);
			}
			if(System.currentTimeMillis() - this.lastFanPush > 1000L) {
				loc.add(0, this.riseSpeed, 0);
			}
		}
	}

}
