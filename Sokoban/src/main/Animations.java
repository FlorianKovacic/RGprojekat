package main;

import controls.PlayerMovement;
import data.GridCoordinates;
import data.State;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Translate;

public class Animations extends AnimationTimer {

	private static final long TIME_OF_PUSHING_A_CRATE = 2_000_000_000;
	private static final double SPEED_OF_ADJUSTING_CAMERA_TO_CENTER = 2E-9;

	private State state;
	private PlayerMovement playerMovement;

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

	public void setPlayerMovement(PlayerMovement playerMovement) {
		this.playerMovement = playerMovement;
	}

	public Animations(State state) {
		this.state = state;
	}

	public void pushCrate(GridCoordinates gc, GridCoordinates direction) {
		//System.out.println("Camera position grid: "+ gc);
		//System.out.println("Direction: " + direction);
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
		playerMovement.setTranslationX(currentCameraPosition.getX());
		playerMovement.setTranslationY(currentCameraPosition.getY());
		playerMovement.setTranslationZ(currentCameraPosition.getZ());
		playerMovement.updateTranslation();
	}

	private void cameraAdjustmentUpdate(double progress) {
		Point3D currentCameraPosition = cameraAdjustmentStartingPositionReal.multiply(1 - progress).add(cameraAdjustmentFinalPositionReal.multiply(progress));
		playerMovement.setTranslationX(currentCameraPosition.getX());
		playerMovement.setTranslationY(currentCameraPosition.getY());
		playerMovement.setTranslationZ(currentCameraPosition.getZ());
		playerMovement.updateTranslation();
	}

	@Override
	public void handle(long currentTime) {
		if(!adjustingCamera) {
			if(!pushing) {
				if(crateToPush != null) {
					//System.out.println("Starting pushing.");
					playerMovement.stopListeningToKeyboardInput();
					adjustingCamera = true;
					cameraAdjustmentStartingPositionReal = playerMovement.currentPositionReal();
					cameraAdjustmentFinalPositionReal = startingPositionReal.add(relativeCameraPosition);
					double difference = cameraAdjustmentFinalPositionReal.distance(cameraAdjustmentStartingPositionReal);
					timeToAdjustCamera = (long) (difference / SPEED_OF_ADJUSTING_CAMERA_TO_CENTER);
					startingTime = currentTime;
				}
			}else {
				long elapsedTime = currentTime - startingTime;
				if(elapsedTime >= TIME_OF_PUSHING_A_CRATE) {
					updatePosition(1.0);
					pushing = false;
					//System.out.println("Finished pushing.");
					crateToPush = null;
					state.updatePush(startingPositionGrid, finalPositionGrid);
					playerMovement.setPosition(startingPositionGrid);
					playerMovement.continueListeningToKeyboardInput();
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
