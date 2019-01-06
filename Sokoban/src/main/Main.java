package main;

import controls.PlayerMovement;
import controls.PlayerView;
import data.State;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

public class Main extends Application {

	public static final int SIZE = 10;
	public static final double FLOOR_THICKNESS = 2.0;
	public static final double RELATIVE_CAMERA_LEVEL = 0.8;
	public static final double FIELD_OF_VIEW = 40.0;

	private static final Point3D CAMERA_OFFSET = new Point3D(0.0, - (RELATIVE_CAMERA_LEVEL - 0.5) * SIZE, 0.0);

	private static final String INPUT_FILE = "in\\input.txt";

	private State state;

	@Override
	public void init() throws Exception {
		super.init();
		state = new State(INPUT_FILE);
	}

	@Override
	public void start(Stage stage) throws Exception {

		Group blocks = new Group();
		for(int i = 0; i < state.getWidth(); i++) {
			for(int j = 0; j < state.getHeight(); j++) {
				for(int k = 0; k < state.getLength(); k++) {
					Node block = state.getBlock(i, j, k);
					if(block != null) {
						blocks.getChildren().add(block);
					}
				}
			}
		}

		Box floor = new Box(state.getWidth() * SIZE, FLOOR_THICKNESS, state.getLength() * SIZE);
		floor.getTransforms().add(new Translate(0.0, FLOOR_THICKNESS * 0.5, 0.0));
		Group root = new Group(blocks, floor);

		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(1000.0);
		camera.setFieldOfView(FIELD_OF_VIEW);

		Point3D cameraPosition = state.getInitialPlayerPosition();
		Point3D finalCameraPosition = cameraPosition.add(CAMERA_OFFSET);
		PlayerView view = new PlayerView(stage);
		PlayerMovement movement = new PlayerMovement(camera, finalCameraPosition, view, state);

		Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
		scene.setCursor(Cursor.NONE);
		scene.setCamera(camera);
		scene.setOnKeyPressed(movement);
		scene.setOnMouseMoved(view);

		stage.setScene(scene);
		stage.setFullScreen(true);
		stage.setTitle("Sokoban");
		stage.show();

	}

	public static void main(String args[]) {
		Application.launch();
	}

}