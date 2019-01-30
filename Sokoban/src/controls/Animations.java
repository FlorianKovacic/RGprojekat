package controls;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import data.Crate;
import data.GridCoordinates;
import data.State;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class Animations extends AnimationTimer {

	private static Function<Double, Double> interpolation(List<Point2D> points, Function<Double, Double> approximation) {
		return (x) -> {
			int ordinal = 1;
			Point2D end = points.get(ordinal);
			while(x > end.getX()) {
				ordinal++;
				end = points.get(ordinal);
			}
			Point2D beginning = points.get(ordinal - 1);
			return beginning.getY() + approximation.apply((x - beginning.getX()) / (end.getX() - beginning.getX())) * (end.getY() - beginning.getY());
		};
	}

	private static final Function<Double, Double> SMOOTH_TRANSITION = (x) -> (Math.sin((x - 0.5) * Math.PI) + 1) * 0.5;

	private static final Function<Double, Double> linearInterpolation(List<Point2D> points){
		return interpolation(points, (x) -> x);
	}

	private static final long TIME_OF_PUSHING_A_CRATE = 1_000_000_000L;
	private static final long TIME_OF_CLIMBING_A_BLOCK = 2_000_000_000L;
	private static final double SPEED_OF_ADJUSTING_CAMERA_TO_CENTER = 2E-8;

	private static final Function<Double, Double> CLIMBING_HORIZONTAL = linearInterpolation(Arrays.asList(new Point2D(0.0, 0.0), new Point2D(0.2, 0.0), new Point2D(0.4, 0.0), new Point2D(0.6, 0.8), new Point2D(0.8, 0.95), new Point2D(1.0, 1.0)));
	private static final Function<Double, Double> CLIMBING_VERTICAL = linearInterpolation(Arrays.asList(new Point2D(0.0, 0.0), new Point2D(0.2, 0.1), new Point2D(0.4, 0.1), new Point2D(0.6, 0.5), new Point2D(0.8, 0.6), new Point2D(1.0, 1.0)));
	private static final Function<Double, Double> CLIMBING_LEANING = linearInterpolation(Arrays.asList(new Point2D(0.0, 0.0), new Point2D(0.2, 0.6), new Point2D(0.4, 0.0), new Point2D(0.6, -1.0), new Point2D(0.8, 0.0), new Point2D(1.0, 0.0)));

	private static final double LEAN_ANGLE = 30.0;

	private static final double MOVEMENT_SPEED = 0.4;

	private State state;
	private PlayerMovement movement;
	private PlayerView view;
	private Camera camera;

	private boolean movingForwards;
	private boolean movingBackwards;
	private boolean movingLeft;
	private boolean movingRight;

	public void setMovingForwards(boolean movingForwards) {
		this.movingForwards = movingForwards;
	}

	public void setMovingBackwards(boolean movingBackwards) {
		this.movingBackwards = movingBackwards;
	}

	public void setMovingLeft(boolean movingLeft) {
		this.movingLeft = movingLeft;
	}

	public void setMovingRight(boolean movingRight) {
		this.movingRight = movingRight;
	}

	private boolean adjustingCamera;
	private boolean pushing;
	private Crate crateToPush;
	private GridCoordinates startingPositionGrid;
	private GridCoordinates finalPositionGrid;
	private Point3D startingPositionReal;
	private Point3D finalPositionReal;
	private Point3D relativeCameraPosition;
	private Point3D cameraAdjustmentStartingPositionReal;
	private Point3D cameraAdjustmentFinalPositionReal;
	private long timeToAdjustCamera;
	private long startingTime;

	private Point3D climbFrom;
	private GridCoordinates climbTo;
	private Point3D climbToReal;
	private boolean needToClimb;
	private boolean climbing;
	private Rotate lean;

	private List<FallingCrate> fallingCrates;

	public void setMovement(PlayerMovement movement) {
		this.movement = movement;
	}

	public void setView(PlayerView view) {
		this.view = view;
	}

	public Animations(State state, Camera camera) {
		this.state = state;
		this.camera = camera;
		fallingCrates = new LinkedList<FallingCrate>();
	}

	public void pushCrate(GridCoordinates gc, GridCoordinates direction) {
		startingPositionGrid = gc.add(direction);
		finalPositionGrid = startingPositionGrid.add(direction);
		startingPositionReal = state.gridToRealCoordinates(startingPositionGrid);
		finalPositionReal = state.gridToRealCoordinates(finalPositionGrid);
		relativeCameraPosition = State.gridVectorToReal(direction).multiply(-0.5 - PlayerMovement.EDGE_WIDTH);
		crateToPush = (Crate) state.getBlock(startingPositionGrid);
	}

	private void updatePosition(double progress) {
		progress = SMOOTH_TRANSITION.apply(progress);
		Point3D currentCratePosition = startingPositionReal.multiply(1 - progress).add(finalPositionReal.multiply(progress));
		crateToPush.getModel().getTransforms().setAll(new Translate(currentCratePosition.getX(), currentCratePosition.getY(), currentCratePosition.getZ()));
		Point3D currentCameraPosition = currentCratePosition.add(relativeCameraPosition);
		movement.setTranslationX(currentCameraPosition.getX());
		//movement.setTranslationY(currentCameraPosition.getY());
		movement.setTranslationZ(currentCameraPosition.getZ());
		movement.updateTranslation();
	}

	private void cameraAdjustmentUpdate(double progress) {
		Point3D currentCameraPosition = cameraAdjustmentStartingPositionReal.multiply(1 - progress).add(cameraAdjustmentFinalPositionReal.multiply(progress));
		movement.setTranslationX(currentCameraPosition.getX());
		//movement.setTranslationY(currentCameraPosition.getY());
		movement.setTranslationZ(currentCameraPosition.getZ());
		movement.updateTranslation();
	}

	private void updateClimb(double progress) {
		double progressHorizontal = CLIMBING_HORIZONTAL.apply(progress);
		double progressVertical = CLIMBING_VERTICAL.apply(progress);
		double progressLeaning = CLIMBING_LEANING.apply(progress);
		Point3D newCameraPositionHorizontal = climbFrom.multiply(1 - progressHorizontal).add(climbToReal.multiply(progressHorizontal));
		movement.setTranslationX(newCameraPositionHorizontal.getX());
		movement.setTranslationZ(newCameraPositionHorizontal.getZ());
		Point3D newCameraPositionVertical = climbFrom.multiply(1 - progressVertical).add(climbToReal.multiply(progressVertical));
		movement.setTranslationY(newCameraPositionVertical.getY());
		movement.updateTranslation();
		double angle = LEAN_ANGLE * progressLeaning;
		lean.setAngle(angle);
	}

	public void haltMovement() {
		movingForwards = false;
		movingBackwards = false;
		movingLeft = false;
		movingRight = false;
	}

	private void generateMovement() {
		Point3D step = new Point3D(0.0, 0.0, 0.0);
		if(movingForwards) {
			step = step.add(new Point3D(0.0, 0.0, 1.0));
		}
		if(movingLeft) {
			step = step.add(new Point3D(-1.0, 0.0, 0.0));
		}
		if(movingBackwards) {
			step = step.add(new Point3D(0.0, 0.0, -1.0));
		}
		if(movingRight) {
			step = step.add(new Point3D(1.0, 0.0, 0.0));
		}
		if(!step.equals(Point3D.ZERO)) {
			Transform combined = view.getRotationX().createConcatenation(view.getRotationY());
			step = combined.deltaTransform(step);
			step = new Point3D(step.getX(), 0.0, step.getZ());
			step = step.normalize();
			step = step.multiply(MOVEMENT_SPEED);
			movement.move(step);
		}
	}

	private void letItFall(Crate crate, long now) {
		GridCoordinates fallsFrom = crate.getGridCoordinates();
		GridCoordinates fallsTo = state.fallsTo(fallsFrom);
		if(fallsTo != null) {
			fallingCrates.add(new FallingCrate(crate, fallsFrom, fallsTo, state, now));
		}
	}

	private void initiateAdjustingCamera(long now) {
		movement.stopListeningToKeyboardInput();
		adjustingCamera = true;
		cameraAdjustmentStartingPositionReal = movement.currentPositionReal();
		cameraAdjustmentFinalPositionReal = startingPositionReal.add(relativeCameraPosition);
		double difference = cameraAdjustmentFinalPositionReal.distance(cameraAdjustmentStartingPositionReal);
		timeToAdjustCamera = (long) (difference / SPEED_OF_ADJUSTING_CAMERA_TO_CENTER);
		startingTime = now;
	}

	private void push(long now) {
		long elapsedTime = now - startingTime;
		if(elapsedTime >= TIME_OF_PUSHING_A_CRATE) {
			updatePosition(1.0);
			pushing = false;
			Crate thatWasPushed = crateToPush;
			crateToPush = null;
			state.finsishedMovingCrate(thatWasPushed, startingPositionGrid, finalPositionGrid);
			movement.setPosition(startingPositionGrid);
			letItFall(thatWasPushed, now);
			movement.continueListeningToKeyboardInput();
		}else {
			double progress = (double) (elapsedTime) / (double) (TIME_OF_PUSHING_A_CRATE);
			updatePosition(progress);
		}
	}

	private void adjustCamera(long now) {
		long elapsedTime = now - startingTime;
		if(elapsedTime >= timeToAdjustCamera) {
			cameraAdjustmentUpdate(1.0);
			adjustingCamera = false;
			pushing = true;
			state.startedMovingCrate(crateToPush, startingPositionGrid, finalPositionGrid);
			startingTime = now;
		}else {
			double progress = (double) (elapsedTime) / (double) (timeToAdjustCamera);
			cameraAdjustmentUpdate(progress);
		}
	}

	public void requestClimbing(GridCoordinates from, GridCoordinates direction) {
		GridCoordinates neighbor = from.add(direction);
		climbTo = neighbor.add(new GridCoordinates(0, 1, 0));
		Point3D centerClimbTo = state.gridToRealCoordinates(climbTo);
		climbToReal = centerClimbTo.add(State.gridVectorToReal(direction).multiply(- 0.5 + PlayerMovement.EDGE_WIDTH));
		lean = new Rotate(0.0, Rotate.Z_AXIS);
		camera.getTransforms().add(lean);
		needToClimb = true;
	}

	private void startClimbing(long now) {
		movement.stopListeningToKeyboardInput();
		climbing = true;
		needToClimb = false;
		climbFrom = movement.currentPositionReal();
		startingTime = now;
	}

	private void climb(long now) {
		long elapsedTime = now - startingTime;
		if(elapsedTime >= TIME_OF_CLIMBING_A_BLOCK) {
			updateClimb(1.0);
			climbing = false;
			camera.getTransforms().remove(lean);
			movement.setPosition(climbTo);
			movement.continueListeningToKeyboardInput();
		}else {
			double progress = (double) (elapsedTime) / (double) (TIME_OF_CLIMBING_A_BLOCK);
			updateClimb(progress);
		}
	}

	@Override
	public void handle(long now) {
		for(FallingCrate fc: fallingCrates) {
			if(fc.makeItFall(now)) {
				fallingCrates.remove(fc);
				fc.thisCrateFell();
			}
		}
		if(!adjustingCamera) {
			if(!pushing) {
				if(!climbing) {
					if(needToClimb) {
						startClimbing(now);
					}else if(crateToPush != null) {
						initiateAdjustingCamera(now);
					}else {
						generateMovement();
					}
				}else {
					climb(now);
				}
			}else {
				push(now);
			}
		}else {
			adjustCamera(now);
		}
		movement.fall(now);
	}

}
