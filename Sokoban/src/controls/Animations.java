package controls;

import data.GridCoordinates;
import data.State;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class Animations extends AnimationTimer {

	private static final long TIME_OF_PUSHING_A_CRATE = 2_000_000_000;
	private static final double SPEED_OF_ADJUSTING_CAMERA_TO_CENTER = 2E-9;

	private static final double MOVEMENT_SPEED = 0.4;

	private State state;
	private PlayerMovement movement;
	private PlayerView view;

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
	private Node crateToPush;
	private GridCoordinates startingPositionGrid;
	private GridCoordinates finalPositionGrid;
	private Point3D startingPositionReal;
	private Point3D finalPositionReal;
	private Point3D relativeCameraPosition;
	private Point3D cameraAdjustmentStartingPositionReal;
	private Point3D cameraAdjustmentFinalPositionReal;
	private long timeToAdjustCamera;
	private long startingTime;

	public void setMovement(PlayerMovement movement) {
		this.movement = movement;
	}

	public void setView(PlayerView view) {
		this.view = view;
	}

	public Animations(State state) {
		this.state = state;
	}

	public void pushCrate(GridCoordinates gc, GridCoordinates direction) {
		startingPositionGrid = gc.add(direction);
		finalPositionGrid = startingPositionGrid.add(direction);
		startingPositionReal = state.gridToRealCoordinates(startingPositionGrid);
		finalPositionReal = state.gridToRealCoordinates(finalPositionGrid);
		relativeCameraPosition = State.gridVectorToReal(direction).multiply(-0.5 - PlayerMovement.EDGE_WIDTH);
		crateToPush = state.getBlock(startingPositionGrid);
	}

	private void updatePosition(double progress) {
		Point3D currentCratePosition = startingPositionReal.multiply(1 - progress).add(finalPositionReal.multiply(progress));
		crateToPush.getTransforms().setAll(new Translate(currentCratePosition.getX(), currentCratePosition.getY(), currentCratePosition.getZ()));
		Point3D currentCameraPosition = currentCratePosition.add(relativeCameraPosition);
		movement.setTranslationX(currentCameraPosition.getX());
		movement.setTranslationY(currentCameraPosition.getY());
		movement.setTranslationZ(currentCameraPosition.getZ());
		movement.updateTranslation();
	}

	private void cameraAdjustmentUpdate(double progress) {
		Point3D currentCameraPosition = cameraAdjustmentStartingPositionReal.multiply(1 - progress).add(cameraAdjustmentFinalPositionReal.multiply(progress));
		movement.setTranslationX(currentCameraPosition.getX());
		movement.setTranslationY(currentCameraPosition.getY());
		movement.setTranslationZ(currentCameraPosition.getZ());
		movement.updateTranslation();
	}

	private void haltMovement() {
		movingForwards = false;
		movingBackwards = false;
		movingLeft = false;
		movingRight = false;
	}

	private void stopListeningToKeyboardInput() {
		movement.setListeningToKeyboardInput(false);
		haltMovement();
	}

	private void continueListeningToKeyboardInput() {
		movement.setListeningToKeyboardInput(true);
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

	@Override
	public void handle(long currentTime) {
		if(!adjustingCamera) {
			if(!pushing) {
				if(crateToPush != null) {
					stopListeningToKeyboardInput();
					adjustingCamera = true;
					cameraAdjustmentStartingPositionReal = movement.currentPositionReal();
					cameraAdjustmentFinalPositionReal = startingPositionReal.add(relativeCameraPosition);
					double difference = cameraAdjustmentFinalPositionReal.distance(cameraAdjustmentStartingPositionReal);
					timeToAdjustCamera = (long) (difference / SPEED_OF_ADJUSTING_CAMERA_TO_CENTER);
					startingTime = currentTime;
				}else {
					generateMovement();
				}
			}else {
				long elapsedTime = currentTime - startingTime;
				if(elapsedTime >= TIME_OF_PUSHING_A_CRATE) {
					updatePosition(1.0);
					pushing = false;
					crateToPush = null;
					state.updatePush(startingPositionGrid, finalPositionGrid);
					movement.setPosition(startingPositionGrid);
					continueListeningToKeyboardInput();
				}else {
					double progress = (double) (elapsedTime) / (double) (TIME_OF_PUSHING_A_CRATE);
					updatePosition(progress);
				}
			}
		}else {
			long elapsedTime = currentTime - startingTime;
			if(elapsedTime >= timeToAdjustCamera) {
				cameraAdjustmentUpdate(1.0);
				adjustingCamera = false;
				pushing = true;
				startingTime = currentTime;
			}else {
				double progress = (double) (elapsedTime) / (double) (timeToAdjustCamera);
				cameraAdjustmentUpdate(progress);
			}
		}
	}

}
