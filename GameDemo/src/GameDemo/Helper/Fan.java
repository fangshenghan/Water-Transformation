package GameDemo.Helper;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class Fan {
	
	public Location loc;
	public BlockFace facing;
	public int range;
	
	public boolean isRunning;
	
	public Fan(Location loc, BlockFace facing, int range, boolean isRunning) {
		this.loc = loc;
		this.facing = facing;
		this.range = range;
		this.isRunning = isRunning;
	}

}
