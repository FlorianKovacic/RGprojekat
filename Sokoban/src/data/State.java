package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import controls.PlayerControls;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import main.Main;

public class State {

	public static Point3D gridVectorToReal(GridCoordinates vector) {
		return new Point3D(vector.getX(), - vector.getY(), - vector.getZ()).multiply(Main.SIZE);
	}

	private Stage stage;

	private int width;
	private int height;
	private int length;
	private Block[][][] blocks;
	private GridCoordinates initialPlayerPosition;
	private Group blocksGroup;

	private boolean[][][] destinationsArray;
	private Group destinationsGroup;
	private List<GridCoordinates> remainingDestinations;

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return length;
	}

	public Group getBlocks() {
		return blocksGroup;
	}

	public Group getDestinations() {
		return destinationsGroup;
	}

	public Point3D gridToRealCoordinates(GridCoordinates gc) {
		return new Point3D(gc.getX() - (width - 1) / 2.0, -(gc.getY() + 0.5), (length - 1) / 2.0 - gc.getZ()).multiply(Main.SIZE);
	}

	public GridCoordinates realToGridCoordinates(Point3D rc) {
		int x = (int) (rc.getX() / Main.SIZE + width / 2.0 + 1.0) - 1;
		int y = (int) ((- rc.getY() + PlayerControls.CAMERA_OFFSET.getY()) / Main.SIZE);
		int z = (int) (- rc.getZ() / Main.SIZE + length / 2.0 + 1.0) - 1;
		return new GridCoordinates(x, y, z);
	}

	public Block getBlock(GridCoordinates gc) {
		return blocks[gc.getX()][gc.getY()][gc.getZ()];
	}

	private void setBlock(GridCoordinates gc, Block block) {
		blocks[gc.getX()][gc.getY()][gc.getZ()] = block;
	}

	public Point3D getInitialPlayerPositionReal() {
		return gridToRealCoordinates(initialPlayerPosition);
	}

	public GridCoordinates getInitialPlayerPositionGrid() {
		return initialPlayerPosition;
	}

	public boolean isDestination(GridCoordinates gc) {
		return destinationsArray[gc.getX()][gc.getY()][gc.getZ()];
	}

	private Translate createTranslate(GridCoordinates gc) {
		Point3D realCoordinates = gridToRealCoordinates(gc);
		return new Translate(realCoordinates.getX(), realCoordinates.getY(), realCoordinates.getZ());
	}

	public State(String inputFile) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			String dimensionsString = br.readLine();
			br.readLine();
			String[] dimensions = dimensionsString.split(" ");
			width = Integer.parseInt(dimensions[0]);
			height = Integer.parseInt(dimensions[1]);
			length = Integer.parseInt(dimensions[2]);
			blocks = new Block[width][height][length];
			blocksGroup = new Group();
			destinationsArray = new boolean[width][height][length];
			destinationsGroup = new Group();
			remainingDestinations = new LinkedList<GridCoordinates>();
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < length; j++) {
					String line = br.readLine();
					for(int k = 0; k < width; k++) {
						GridCoordinates current = new GridCoordinates(k, i, j);
						Translate currentTranslate = createTranslate(current);
						char type = line.charAt(k);
						Block currentBlock = null;
						switch(type) {
							case 'w': {
								currentBlock = new Wall(current, currentTranslate);
							}
							break;
							case 'c': {
								currentBlock = new Crate(current, currentTranslate, false);
							}
							break;
							case 's': {
								currentBlock = null;
							}
							break;
							case 'b': {
								initialPlayerPosition = current;
							}
							break;
							case 'p': {
								currentBlock = new Crate(current, currentTranslate, true);
								destinationsArray[k][i][j] = true;
								destinationsGroup.getChildren().add(new Destination(current, currentTranslate).getModel());
							}
							break;
							case 'r': {
								currentBlock = null;
								destinationsArray[k][i][j] = true;
								destinationsGroup.getChildren().add(new Destination(current, currentTranslate).getModel());
								remainingDestinations.add(current);
							}
							break;
							default: {
								System.out.println("Input file is not of the correct format.");
								blocks = null;
								return;
							}
						}
						if(currentBlock != null) {
							setBlock(current, currentBlock);
							blocksGroup.getChildren().add(currentBlock.getModel());
						}
					}
				}
				br.readLine();
			}
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			if(br != null) {
				try {
					br.close();
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean empty(GridCoordinates gc) {
		return getBlock(gc) == null;
	}

	public boolean blocking(GridCoordinates gc, GridCoordinates direction) {
		return !empty(gc) && !pushable(gc, direction);
	}

	public boolean pushable(GridCoordinates gc, GridCoordinates direction) {
		GridCoordinates upward = new GridCoordinates(0, 1, 0);
		GridCoordinates above = gc.add(upward);
		return direction.oneDimensional() && !empty(gc) && getBlock(gc).pushable() && empty(gc.add(direction)) && (empty(above) || !getBlock(above).pushable());
	}

	public boolean climbable(GridCoordinates gc) {
		return empty(gc.add(new GridCoordinates(0, 1, 0)));
	}

	private void updateAndCheckForWinCondition(GridCoordinates from, GridCoordinates to) {
		if(isDestination(from)) {
			remainingDestinations.add(from);
		}
		if(isDestination(to)) {
			remainingDestinations.remove(to);
			if(remainingDestinations.isEmpty()) {
				Alert alert = new Alert(AlertType.INFORMATION, "You have solved this puzzle!");
				alert.initOwner(stage);
				alert.setOnHidden((event) -> System.exit(0));
				alert.show();
			}
		}
	}

	public GridCoordinates fallsTo(GridCoordinates gc) {
		GridCoordinates down = new GridCoordinates(0, - 1, 0);
		if(!empty(gc.add(down))) {
			return null;
		}
		do{
			gc = gc.add(down);
		}while(empty(gc.add(down)));
		return gc;
	}

	public void startedMovingCrate(Crate crate, GridCoordinates from, GridCoordinates to) {
		setBlock(from, null);
		setBlock(to, crate);
		crate.setGridCoordinates(to);
		if(isDestination(from)) {
			crate.outOfPosition();
		}
	}

	public void finsishedMovingCrate(Crate crate, GridCoordinates from, GridCoordinates to) {
		if(isDestination(to)) {
			crate.inPosition();
		}
		updateAndCheckForWinCondition(from, to);
	}

}
