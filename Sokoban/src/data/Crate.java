package data;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import main.Main;

public class Crate extends Block {

	public Crate(GridCoordinates gridCoordinates, Translate translate) {
		super(gridCoordinates);
		model = new Box(Main.SIZE, Main.SIZE, Main.SIZE);
		model.setMaterial(new PhongMaterial(new Color(0.4, 0.15, 0.15, 1.0)));
		position(translate);
	}

	@Override
	public boolean pushable() {
		return true;
	}

}
