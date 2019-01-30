package data;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import main.Main;

public class Wall extends Block {

	private static Color DEFAULT_COLOR = new Color(0.6, 0.3, 0.3, 1.0);
	private static Image diffuseMap = new Image("file:textures/wall_diffuse.jpg", 512.0, 512.0, false, false);
	private static Material MATERIAL = new PhongMaterial(DEFAULT_COLOR, diffuseMap, null, null, null);

	public Wall(GridCoordinates gridCoordinates, Translate translate) {
		super(gridCoordinates);
		model = new Box(Main.SIZE, Main.SIZE, Main.SIZE);
		model.setMaterial(MATERIAL);
		position(translate);
	}

	@Override
	public boolean pushable() {
		return false;
	}

	@Override
	public boolean immaterial() {
		return false;
	}

}
