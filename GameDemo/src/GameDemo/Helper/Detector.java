package GameDemo.Helper;

import org.bukkit.Location;

public class Detector {
	
	public Location loc;
	public String type;
	
	public String mode;
	
	public boolean activated = false;
	
	public Detector(Location loc, String type) {
		this.loc = loc;
		this.type = type;
	}

	
	public Detector(Location loc, String type, String mode) {
		this.loc = loc;
		this.type = type;
		this.mode = mode;
	}
}
