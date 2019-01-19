package controls;

import data.GridCoordinates;
import data.State;
import javafx.geometry.Point3D;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.Main;

public class PlayerControls {

	private static final double FAR_CLIP = 1000.0;
	private static final double NEAR_CLIP = 1E-3;
	private static final double FIELD_OF_VIEW = 40.0;
	private static final double RELATIVE_CAMERA_LEVEL = 0.8;
	private static final Point3D CAMERA_OFFSET = new Point3D(0.0, - (RELATIVE_CAMERA_LEVEL - 0.5) * Main.SIZE, 0.0);

	private PlayerView view;
	private PlayerMovement movement;
	private PerspectiveCamera camera;
	private Animations animations;

	public PlayerControls(State state, Stage stage, Scene scene) {
		camera = new PerspectiveCamera(true);
		camera.setFarClip(FAR_CLIP);
		camera.setNearClip(NEAR_CLIP);
		camera.setFieldOfView(FIELD_OF_VIEW);

		GridCoordinates cameraPositionGrid = state.getInitialPlayerPositionGrid();
		Point3D cameraPositionReal = state.getInitialPlayerPositionReal();
		Point3D finalCameraPosition = cameraPositionReal.add(CAMERA_OFFSET);
		view = new PlayerView(stage);
		animations = new Animations(state);
		movement = new PlayerMovement(camera, cameraPositionGrid, finalCameraPosition, state, animations);
		animations.setView(view);
		animations.setMovement(movement);
		view.addRotations(camera);

		scene.setCamera(camera);
		scene.setOnKeyPressed(movement);
		scene.setOnKeyReleased(movement);
		scene.setOnMouseMoved(view);
	}

	public void startAnimations() {
		animations.start();
	}

}
