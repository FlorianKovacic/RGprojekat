package data;

import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import main.Main;

public class Destination extends Block {

	private static Color COLOR = new Color(0.8, 0.8, 0.15, 1.0);
	private static Material MATERIAL = new PhongMaterial(COLOR);
	private static double RELATIVE_SIZE = 0.45;

	public Destination(GridCoordinates gridCoordinates, Translate translate) {
		super(gridCoordinates);
		model = new Sphere(Main.SIZE * RELATIVE_SIZE);
		model.setMaterial(MATERIAL);
		position(translate);
	}

	@Override
	public boolean pushable() {
		return false;
	}

	@Override
	public boolean immaterial() {
		return true;
	}

}
