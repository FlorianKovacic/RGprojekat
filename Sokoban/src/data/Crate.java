package data;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Translate;
import main.Main;

public class Crate extends Block {

	private static Color DEFAULT_COLOR = new Color(0.6, 0.225, 0.225, 1.0);
	//private static Color DEFAULT_COLOR = Color.WHITE;
	private static Image diffuseMap = new Image("file:textures/crate_diffuse.png");
	private static Image bumpMap = new Image("file:textures/crate_bump.png");
	private static Material DEFAULT_MATERIAL = new PhongMaterial(DEFAULT_COLOR, diffuseMap, null, bumpMap, null);
	private static Color COLOR_FOR_WHEN_ITS_IN_POSITION = new Color(0.45, 0.75, 0.15, 1.0);
	private static Material MATERIAL_FOR_WHEN_ITS_IN_POSITION = new PhongMaterial(COLOR_FOR_WHEN_ITS_IN_POSITION, diffuseMap, null, bumpMap, null);

	public Crate(GridCoordinates gridCoordinates, Translate translate, boolean atDestination) {
		super(gridCoordinates);
		model = new Box(Main.SIZE, Main.SIZE, Main.SIZE);
		model.setMaterial(atDestination ? MATERIAL_FOR_WHEN_ITS_IN_POSITION : DEFAULT_MATERIAL);
		position(translate);
	}

	@Override
	public boolean pushable() {
		return true;
	}

	@Override
	public boolean immaterial() {
		return false;
	}

	public void inPosition() {
		model.setMaterial(MATERIAL_FOR_WHEN_ITS_IN_POSITION);
	}

	public void outOfPosition() {
		model.setMaterial(DEFAULT_MATERIAL);
	}

}
