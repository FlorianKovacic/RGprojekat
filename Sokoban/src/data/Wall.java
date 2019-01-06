package data;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import main.Main;

public class Wall extends Block {

	public Wall(GridCoordinates gridCoordinates, Translate translate) {
		super(gridCoordinates);
		model = new Box(Main.SIZE, Main.SIZE, Main.SIZE);
		model.setMaterial(new PhongMaterial(Color.BROWN));
		position(translate);
	}

}
