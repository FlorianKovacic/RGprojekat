package controls;

import data.GridCoordinates;
import data.State;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Translate;
import main.Main;

public class PlayerMovement implements EventHandler<KeyEvent>{

	public static final double EDGE_WIDTH = 1E-1;

	private static int signum(double value) {
		if(value > 0) {
			return 1;
		}
		if(value < 0) {
			return - 1;
		}
		return 0;
	}

	private double translationX;
	private double translationY;
	private double translationZ;

	private boolean listeningToKeyboardInput;

	private GridCoordinates cameraPositionGrid;
	private Translate translation;
	private State state;
	private Animations animations;

	public Point3D currentPositionReal() {
		return new Point3D(translationX, translationY, translationZ);
	}

	public void setTranslationX(double translationX) {
		this.translationX = translationX;
	}

	public void setTranslationY(double translationY) {
		this.translationY = translationY;
	}

	public void setTranslationZ(double translationZ) {
		this.translationZ = translationZ;
	}

	public void setPosition(GridCoordinates gc) {
		cameraPositionGrid = gc;
	}

	public void setListeningToKeyboardInput(boolean listeningToKeyboardInput) {
		this.listeningToKeyboardInput = listeningToKeyboardInput;
	}

	public PlayerMovement(Camera camera, GridCoordinates cameraPositionGrid, Point3D cameraPositionReal, State state, Animations animations) {
		this.cameraPositionGrid = cameraPositionGrid;
		this.state = state;
		this.animations = animations;
		translationX = cameraPositionReal.getX();
		translationY = cameraPositionReal.getY();
		translationZ = cameraPositionReal.getZ();
		translation = new Translate(translationX, translationY, translationZ);
		camera.getTransforms().add(translation);
		listeningToKeyboardInput = true;
	}

	public void updateTranslation() {
		translation.setX(translationX);
		translation.setY(translationY);
		translation.setZ(translationZ);
	}

	private boolean withinCurrentSpaceX(Point3D newPosition) {
		Point3D center = state.gridToRealCoordinates(cameraPositionGrid);
		double x = newPosition.getX();
		double cX = center.getX();
		double half = Main.SIZE / 2.0;
		return (cX - half) <= x && x <= (cX + half);
	}

	private boolean withinCurrentSpaceZ(Point3D newPosition) {
		Point3D center = state.gridToRealCoordinates(cameraPositionGrid);
		double z = newPosition.getZ();
		double cZ = center.getZ();
		double half = Main.SIZE / 2.0;
		return (cZ - half) <= z && z <= (cZ + half);
	}

	private GridCoordinates cameraPositionGrid(Point3D cameraPositionReal) {
		int x = cameraPositionGrid.getX();
		int y = cameraPositionGrid.getY();
		int z = cameraPositionGrid.getZ();
		if(!withinCurrentSpaceX(cameraPositionReal)) {
			x = state.realToGridCoordinates(cameraPositionReal).getX();
		}
		if(!withinCurrentSpaceZ(cameraPositionReal)) {
			z = state.realToGridCoordinates(cameraPositionReal).getZ();
		}
		return new GridCoordinates(x, y, z);
	}

	private double determineActualRelativeX(double newRelativeX, double centerX, int directionX, GridCoordinates directionXVector) {
		double absBorderLine = Main.SIZE * (0.5 - EDGE_WIDTH);
		double borderLine = absBorderLine * directionX;
		double absRelativeX = newRelativeX / directionX;
		if(absRelativeX < absBorderLine) {
			return newRelativeX;
		}
		GridCoordinates neighborX = cameraPositionGrid.add(directionXVector);
		if(state.empty(neighborX)) {
			return newRelativeX;
		}
		if(translationX == centerX + borderLine) {
			if(state.pushable(neighborX, directionXVector)) {
				return Double.NaN;
			}
		}
		return borderLine;
	}

	private double determineActualRelativeZ(double newRelativeZ, double centerZ, int directionZ, GridCoordinates directionZVector) {
		double absBorderLine = Main.SIZE * (0.5 - EDGE_WIDTH);
		double borderLine = absBorderLine * directionZ;
		double absRelativeZ = newRelativeZ / directionZ;
		if(absRelativeZ < absBorderLine) {
			return newRelativeZ;
		}
		GridCoordinates neighborZ = cameraPositionGrid.add(directionZVector); 
		if(state.empty(neighborZ)) {
			return newRelativeZ;
		}
		if(translationZ == centerZ + borderLine) {
			if(state.pushable(neighborZ, directionZVector)) {
				return Double.NaN;
			}
		}
		return borderLine;
	}

	public void move(Point3D step) {
		double newX = translationX + step.getX();
		double newZ = translationZ + step.getZ();
		Point3D centerOfPreviousPosition = state.gridToRealCoordinates(cameraPositionGrid);
		double centerX = centerOfPreviousPosition.getX();
		double centerZ = centerOfPreviousPosition.getZ();
		double newRelativeX = newX - centerX;
		double newRelativeZ = newZ - centerZ;
		int directionX = signum(newRelativeX);
		int directionZ = signum(newRelativeZ);
		GridCoordinates directionXVector = new GridCoordinates(directionX, 0, 0);
		GridCoordinates directionZVector = new GridCoordinates(0, 0, - directionZ);
		double newActualRelativeX = determineActualRelativeX(newRelativeX, centerX, directionX, directionXVector);
		double newActualRelativeZ = determineActualRelativeZ(newRelativeZ, centerZ, directionZ, directionZVector);
		GridCoordinates directionOfThePush = null;
		if(Double.isNaN(newActualRelativeX)) {
			if(Double.isNaN(newActualRelativeZ)) {
				if(newRelativeX > newRelativeZ) {
					directionOfThePush = directionXVector;
				}else {
					directionOfThePush = directionZVector;
				}
			}else {
				directionOfThePush = directionXVector;
			}
		}else {
			if(Double.isNaN(newActualRelativeZ)) {
				directionOfThePush = directionZVector;
			}else {
				double half = Main.SIZE / 2.0;
				if(Math.abs(newActualRelativeX) > half && Math.abs(newActualRelativeZ) > half) {
					if(state.blocking(cameraPositionGrid, directionXVector.add(directionZVector))) {
						if(newActualRelativeX > newActualRelativeZ) {
							newActualRelativeZ = 0.0;
						}else {
							newActualRelativeX = 0.0;
						}
					}
				}
			}
		}
		if(directionOfThePush != null) {
			animations.pushCrate(cameraPositionGrid, directionOfThePush);
			return;
		}
		translationX = centerX + newActualRelativeX;
		translationZ = centerZ + newActualRelativeZ;
		cameraPositionGrid = cameraPositionGrid(new Point3D(translationX, translationY, translationZ));
		updateTranslation();
	}

	@Override
	public void handle(KeyEvent event) {
		if(listeningToKeyboardInput) {
			boolean pressed = event.getEventType().getName().equals("KEY_PRESSED");
			KeyCode key = event.getCode();
			switch(key) {
				case W: {
					animations.setMovingForwards(pressed);
				}
				break;
				case S: {
					animations.setMovingBackwards(pressed);
				}
				break;
				case A: {
					animations.setMovingLeft(pressed);
				}
				break;
				case D: {
					animations.setMovingRight(pressed);
				}
				break;
				default: {
				}
			}
		}
	}

}
