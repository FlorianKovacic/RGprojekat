package controls;

import java.awt.AWTException;
import java.awt.Robot;

import javafx.event.EventHandler;
import javafx.scene.Camera;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class PlayerView implements EventHandler<MouseEvent>{

	public static final double UNIT = 0.1;
	public static final double RIGHT_ANGLE = 90.0;
	public static final double STRAIGHT_ANGLE = 180.0;
	public static final double FULL_ANGLE = 360.0;

	private Stage stage;

	private int centerX;
	private int centerY;

	private double angleX;
	private double angleY;
	private Rotate rotationX;
	private Rotate rotationY;

	private boolean on;
	private Robot robot;

	public Rotate getRotationX() {
		return rotationX;
	}

	public Rotate getRotationY() {
		return rotationY;
	}

	private void calculateCenter() {
		centerX = (int) (stage.getX() + stage.getWidth() / 2.0);
		centerY = (int) (stage.getY() + stage.getHeight() / 2.0);
	}

	private void centerCursor() {
		calculateCenter();
		on = false;
		robot.mouseMove(centerX, centerY);
		on = true;
	}

	public void addRotations(Camera camera) {
		camera.getTransforms().addAll(rotationX, rotationY);
	}

	public PlayerView(Stage stage) {
		this.stage = stage;
		rotationX = new Rotate(angleX, Rotate.Y_AXIS);
		rotationY = new Rotate(angleY, Rotate.X_AXIS);
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
			System.err.println("Fatal error.");
		}
		centerCursor();
		on = true;
	}

	private void updateAngleX(double diffX) {
		angleX += diffX * UNIT;
		if(angleX < - STRAIGHT_ANGLE) {
			angleX += FULL_ANGLE;
		}else if(angleX >= STRAIGHT_ANGLE) {
			angleX -= FULL_ANGLE;
		}
	}

	private void updateAngleY(double diffY) {
		angleY -= diffY * UNIT;
		if(angleY < - RIGHT_ANGLE) {
			angleY = - RIGHT_ANGLE;
		}else if(angleY > RIGHT_ANGLE) {
			angleY = RIGHT_ANGLE;
		}
	}

	@Override
	public void handle(MouseEvent event) {
		if(!on) {
			return;
		}
		double diffX = event.getSceneX() - stage.getWidth() / 2.0;
		double diffY = event.getSceneY() - stage.getHeight() / 2.0;
		updateAngleX(diffX);
		updateAngleY(diffY);
		rotationX.setAngle(angleX);
		rotationY.setAngle(angleY);
		centerCursor();
	}

}
