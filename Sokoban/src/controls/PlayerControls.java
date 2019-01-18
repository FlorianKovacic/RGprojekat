package controls;

import data.GridCoordinates;
import data.State;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.PerspectiveCamera;
import javafx.stage.Stage;
import main.Animations;
import main.Main;

public class PlayerControls {

	private static final double NEAR_CLIP = 1E-3;

	private PlayerView view;
	private PlayerMovement movement;
	private PerspectiveCamera camera;

	public PlayerControls(State state, Stage stage, Animations animations) {
		camera = new PerspectiveCamera(true);
		camera.setFarClip(Main.FAR_CLIP);
		camera.setNearClip(NEAR_CLIP);
		camera.setFieldOfView(Main.FIELD_OF_VIEW);
		GridCoordinates cameraPositionGrid = state.getInitialPlayerPositionGrid();
		Point3D cameraPositionReal = state.getInitialPlayerPositionReal();
		Point3D finalCameraPosition = cameraPositionReal.add(Main.CAMERA_OFFSET);
		view = new PlayerView(stage);
		movement = new PlayerMovement(camera, cameraPositionGrid, finalCameraPosition, view, state, animations);
		view.addRotations(camera);
	}

	public PlayerView getView() {
		return view;
	}

	public PlayerMovement getMovement() {
		return movement;
	}

	public Camera getCamera() {
		return camera;
	}

}
