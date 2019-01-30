package controls;

import data.Crate;
import data.GridCoordinates;
import data.State;
import javafx.geometry.Point3D;
import javafx.scene.transform.Translate;

public class FallingCrate {

	private Crate crate;
	private State state;
	private GridCoordinates from;
	private GridCoordinates to;
	private double height;
	private double x;
	private double z;
	private double floorHeight;
	private double fallingVelocity;
	private long lastFrameTime;

	public FallingCrate(Crate crate, GridCoordinates from, GridCoordinates to, State state, long startOfFall) {
		this.crate = crate;
		this.from = from;
		this.to= to;
		this.state = state;
		Point3D startingPosition = state.gridToRealCoordinates(from);
		height = startingPosition.getY();
		x = startingPosition.getX();
		z = startingPosition.getZ();
		floorHeight = state.gridToRealCoordinates(to).getY();
		lastFrameTime = startOfFall;
		state.startedMovingCrate(crate, from, to);
	}

	public boolean makeItFall(long now) {
		long elapsedTime = now - lastFrameTime;
		lastFrameTime = now;
		fallingVelocity += elapsedTime * PlayerMovement.GRAVITY * 1E-9;
		height += fallingVelocity;
		boolean fell = height >= floorHeight;
		if(fell) {
			height = floorHeight;
		}
		crate.getModel().getTransforms().setAll(new Translate(x, height, z));
		return fell;
	}

	public void thisCrateFell() {
		state.finsishedMovingCrate(crate, from, to);
	}

}
