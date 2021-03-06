package data;

import javafx.scene.Node;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Translate;

public abstract class Block {

	private GridCoordinates gridCoordinates;

	protected Block(GridCoordinates gridCoordinates) {
		this.gridCoordinates = gridCoordinates;
	}

	protected Shape3D model;

	public Node getModel() {
		return model;
	}

	public GridCoordinates getGridCoordinates() {
		return gridCoordinates;
	}

	public void setGridCoordinates(GridCoordinates gridCoordinates) {
		this.gridCoordinates = gridCoordinates;
	}

	protected void position(Translate translate) {
		model.getTransforms().add(translate);
	}

	public abstract boolean pushable();

	//unused
	public abstract boolean immaterial();

}
