package main;

import controls.PlayerControls;
import data.State;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.stage.Stage;

public class Main extends Application {

	public static final int SIZE = 10;

	private static final String INPUT_FILE = "in\\input.txt";

	private State state;

	@Override
	public void init() throws Exception {
		super.init();
		state = new State(INPUT_FILE);
	}

	@Override
	public void start(Stage stage) throws Exception {

		state.setStage(stage);

		Group blocks = state.getBlocks();
		Group destinations = state.getDestinations();
		Group root = new Group(blocks, destinations);

		Scene scene = new Scene(root, 800, 600, true, SceneAntialiasing.BALANCED);
		scene.setCursor(Cursor.NONE);

		PlayerControls controls = new PlayerControls(state, stage, scene);

		stage.setScene(scene);
		stage.setFullScreen(true);
		stage.setTitle("Sokoban");
		stage.show();

		controls.startAnimations();

	}

	public static void main(String args[]) {
		Application.launch();
	}

}
