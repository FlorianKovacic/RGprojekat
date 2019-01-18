package controls;

import data.GridCoordinates;
import data.State;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import main.Animations;
import main.Main;

public class PlayerMovement implements EventHandler<KeyEvent>{

	public static final double EDGE_WIDTH = 1E-3;

	private static final double STEP = 0.2;
	private static final int SLEEP_TIME = 10;

	private double translationX;
	private double translationY;
	private double translationZ;

	private boolean listeningToKeyboardInput;

	private boolean movingForwards;
	private boolean movingBackwards;
	private boolean movingLeft;
	private boolean movingRight;

	private GridCoordinates cameraPositionGrid;
	private Translate translation;
	private PlayerView view;
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

	private void setListeningToKeyboardInput(boolean listeningToKeyboardInput) {
		this.listeningToKeyboardInput = listeningToKeyboardInput;
	}

	private void haltMovement() {
		movingForwards = false;
		movingBackwards = false;
		movingLeft = false;
		movingRight = false;
	}

	public void stopListeningToKeyboardInput() {
		setListeningToKeyboardInput(false);
		haltMovement();
	}

	public void continueListeningToKeyboardInput() {
		setListeningToKeyboardInput(true);
	}

	public PlayerMovement(Camera camera, GridCoordinates cameraPositionGrid, Point3D cameraPositionReal, PlayerView view, State state, Animations animations) {
		this.cameraPositionGrid = cameraPositionGrid;
		this.view = view;
		this.state = state;
		this.animations = animations;
		translationX = cameraPositionReal.getX();
		translationY = cameraPositionReal.getY();
		translationZ = cameraPositionReal.getZ();
		translation = new Translate(translationX, translationY, translationZ);
		camera.getTransforms().add(translation);
		listeningToKeyboardInput = true;
		Thread movementGenerator = new Thread(this::generateMovement);
		movementGenerator.setDaemon(true);
		movementGenerator.start();
	}

	public void updateTranslation() {
		translation.setX(translationX);
		translation.setY(translationY);
		translation.setZ(translationZ);
	}

	private GridCoordinates determineActualDirection(GridCoordinates direction, Point3D step) {
		int diffX = direction.getX();
		int diffZ = direction.getZ();
		GridCoordinates directionX = direction.projectX();
		GridCoordinates directionZ = direction.projectZ();
		GridCoordinates directionDiagonal = direction.projectPlane();
		GridCoordinates neighborX = cameraPositionGrid.add(directionX);
		GridCoordinates neighborZ = cameraPositionGrid.add(directionZ);
		GridCoordinates neighborDiagonal = cameraPositionGrid.add(directionDiagonal);
		boolean blockedX = state.blocking(neighborX, directionX);
		boolean blockedZ = state.blocking(neighborZ, directionZ);
		boolean blockedDiagonally = state.blocking(neighborDiagonal, directionDiagonal);
		int x = 0;
		int y = 0;
		int z = 0;
		if(diffX != 0) {
			if(diffZ != 0) {
				if(blockedX) {
					if(!blockedZ) {
						z = diffZ;
					}
				}else if(blockedZ) {
					x = diffX;
				}else {
					if(blockedDiagonally) {
						if(step.getX() > step.getZ()) {
							x = diffX;
						}else {
							z = diffZ;
						}
					}else if(state.empty(neighborX) && state.empty(neighborZ)) {
						x = diffX;
						z = diffZ;
					}
				}
			}else {
				if(!blockedX) {
					x = diffX;
				}
			}
		}else {
			if(!blockedZ) {
				z = diffZ;
			}
		}
		GridCoordinates result = new GridCoordinates(x, y, z);
		if(result.oneDimensional() && !state.empty(cameraPositionGrid.add(result))) {
			animations.pushCrate(cameraPositionGrid, result);
			return null;
		}
		return result;
	}

	private Point3D calculateCoordinatesForEdgePoint(GridCoordinates startingPosition, GridCoordinates intention, GridCoordinates direction, Point3D intendedNewPosition) {
		double x = intendedNewPosition.getX();
		double y = intendedNewPosition.getY();
		double z = intendedNewPosition.getZ();
		int dirX = direction.getX();
		int dirZ = direction.getZ();
		int intX = intention.getX();
		int intZ = intention.getZ();
		if(intX != 0) {
			if(dirX == 0) {
				x = state.gridToRealCoordinates(startingPosition).getX() + intX * (0.5 - EDGE_WIDTH) * Main.SIZE;
			}
		}
		if(intZ != 0) {
			if(dirZ == 0) {
				z = state.gridToRealCoordinates(startingPosition).getZ() + intZ * -(0.5 - EDGE_WIDTH) * Main.SIZE;
			}
		}
		return new Point3D(x, y, z);
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

	private boolean withinCurrentSpace(Point3D newPosition) {
		return withinCurrentSpaceX(newPosition) && withinCurrentSpaceZ(newPosition);
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

	private void move(Point3D step) {
		//System.out.println("Position before: (" + translationX + ", " + translationY + ", " + translationZ + ")");
		//System.out.println("Grid position before: " + cameraPositionGrid);
		//System.out.println("Step: " + step);
		/*translationX += step.getX();
		translationY += step.getY();
		translationZ += step.getZ();*/
		Point3D newPositionReal = new Point3D(translationX + step.getX(), translationY + step.getY(), translationZ + step.getZ());
		//System.out.println("New camera position real: " + newPositionReal);
		if(!withinCurrentSpace(newPositionReal)) {
			//System.out.println("Previous camera position grid: " + cameraPositionGrid);
			GridCoordinates newPositionGrid = state.realToGridCoordinates(newPositionReal);
			//System.out.println("New camera position grid: " + newPositionGrid);
			GridCoordinates difference = newPositionGrid.subtract(cameraPositionGrid);
			//System.out.println("Difference: " + difference);
			GridCoordinates actualDirection = determineActualDirection(difference, step);
			//System.out.println("Actual direction: " + actualDirection);
			if(actualDirection != null) {
				Point3D newActualPosition = calculateCoordinatesForEdgePoint(cameraPositionGrid, difference, actualDirection, newPositionReal);
				//System.out.println("New actual position: " + newActualPosition);
				translationX = newActualPosition.getX();
				translationZ = newActualPosition.getZ();
				cameraPositionGrid = cameraPositionGrid(newActualPosition);
			}
		}else {
			translationX = newPositionReal.getX();
			translationY = newPositionReal.getY();
			translationZ = newPositionReal.getZ();
		}
		//System.out.println("Position after: (" + translationX + ", " + translationY + ", " + translationZ + ")");
		//System.out.println("Grid position after: " + cameraPositionGrid);
		//System.out.println();
	}

	private void generateMovement() {
		while(true) {
			//System.out.println("Movement generated.");
			Point3D step = new Point3D(0.0, 0.0, 0.0);
			if(movingForwards) {
				step = step.add(new Point3D(0.0, 0.0, 1.0));
			}
			if(movingLeft) {
				step = step.add(new Point3D(-1.0, 0.0, 0.0));
			}
			if(movingBackwards) {
				step = step.add(new Point3D(0.0, 0.0, -1.0));
			}
			if(movingRight) {
				step = step.add(new Point3D(1.0, 0.0, 0.0));
			}
			//System.out.println("Absolute step: " + step);
			Transform combined = view.getRotationX().createConcatenation(view.getRotationY());
			step = combined.deltaTransform(step);
			step = new Point3D(step.getX(), 0.0, step.getZ());
			step = step.normalize();
			step = step.multiply(STEP);
			move(step);
			updateTranslation();
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void handle(KeyEvent event) {
		//System.out.println("Key event captured.");
		if(!listeningToKeyboardInput) {
			//System.out.println("Not listening.");
			return;
		}
		boolean pressed = event.getEventType().getName().equals("KEY_PRESSED");
		KeyCode key = event.getCode();
		switch(key) {
			case W: {
				movingForwards = pressed;
			}
			break;
			case S: {
				movingBackwards = pressed;
			}
			break;
			case A: {
				movingLeft = pressed;
			}
			break;
			case D: {
				movingRight = pressed;
			}
			break;
			default: {
			}
		}
	}

}
