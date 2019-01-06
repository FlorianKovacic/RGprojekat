package controls;

import data.State;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import main.Main;

public class PlayerMovement implements EventHandler<KeyEvent>{

	public static final double UNIT = 1.0;

	private double translationX;
	private double translationY;
	private double translationZ;

	private double minX;
	private double maxX;
	private double minY;
	private double maxY;
	private double minZ;
	private double maxZ;

	private Translate translation;
	private PlayerView view;
	private State state;

	private void calculateBounds() {
		minX = - state.getWidth() / 2.0 * Main.SIZE;
		maxX = - minX;
		minY = - state.getHeight() * Main.SIZE;
		maxY = 0.0;
		minZ = - state.getLength() / 2.0 * Main.SIZE;
		maxZ = - minZ;
	}

	public PlayerMovement(Camera camera, Point3D position, PlayerView view, State state) {
		this.view = view;
		this.state = state;
		translationX = position.getX();
		translationY = position.getY();
		translationZ = position.getZ();
		translation = new Translate(translationX, translationY, translationZ);
		camera.getTransforms().add(translation);
		view.addRotations(camera);
		calculateBounds();
	}

	private void updateTranslation() {
		translation.setX(translationX);
		translation.setY(translationY);
		translation.setZ(translationZ);
	}

	private void move(Point3D step) {
		translationX += step.getX();
		if(translationX < minX) {
			translationX = minX;
		}else if(translationX > maxX) {
			translationX = maxX;
		}
		translationY += step.getY();
		if(translationY < minY) {
			translationY = minY;
		}else if(translationY > maxY) {
			translationY = maxY;
		}
		translationZ += step.getZ();
		if(translationZ < minZ) {
			translationZ = minZ;
		}else if(translationZ > maxZ) {
			translationZ = maxZ;
		}
	}

	@Override
	public void handle(KeyEvent event) {
		String s = event.getText();
		if(s == null || s.length() == 0) {
			//System.err.println("Fatal error.");
			return;
		}
		Point3D direction = null;
		char c = event.getText().charAt(0);
		switch(c) {
			case 'w': {
				direction = new Point3D(0.0, 0.0, 1.0);
			}
			break;
			case 's': {
				direction = new Point3D(0.0, 0.0, - 1.0);
			}
			break;
			case 'a': {
				direction = new Point3D(- 1.0, 0.0, 0.0);
			}
			break;
			case 'd': {
				direction = new Point3D(1.0, 0.0, 0.0);
			}
			break;
			default: {
				//System.out.println("Fatal error: " + c);
			}
		}
		if(direction != null) {
			Transform combined = view.getRotationX().createConcatenation(view.getRotationY());
			direction = combined.deltaTransform(direction);
			direction = new Point3D(direction.getX(), 0.0, direction.getZ());
			direction = direction.normalize();
			direction = direction.multiply(UNIT);
			move(direction);
			updateTranslation();
		}
	}

}
