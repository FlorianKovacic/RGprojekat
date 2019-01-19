package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Translate;
import main.Main;

public class State {

	public static Point3D gridVectorToReal(GridCoordinates vector) {
		return new Point3D(vector.getX(), - vector.getY(), - vector.getZ()).multiply(Main.SIZE);
	}

	private int width;
	private int height;
	private int length;
	private Block[][][] blocks;
	private GridCoordinates initialPlayerPosition;
	private Group blocksGroup;

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

	public Point3D gridToRealCoordinates(GridCoordinates gc) {
		return new Point3D(gc.getX() - (width - 1) / 2.0, -(gc.getY() + 0.5), (length - 1) / 2.0 - gc.getZ()).multiply(Main.SIZE);
	}

	public GridCoordinates realToGridCoordinates(Point3D rc) {
		int x = (int) (rc.getX() / Main.SIZE + width / 2.0 + 1.0) - 1;
		int y = (int) (- rc.getY() / Main.SIZE);
		int z = (int) (- rc.getZ() / Main.SIZE + length / 2.0 + 1.0) - 1;
		return new GridCoordinates(x, y, z);
	}

	private Block block(GridCoordinates gc) {
		return blocks[gc.getX()][gc.getY()][gc.getZ()];
	}

	private void setBlock(GridCoordinates gc, Block block) {
		blocks[gc.getX()][gc.getY()][gc.getZ()] = block;
	}

	public Node getBlock(GridCoordinates gc) {
		Block block = block(gc);
		if(block == null) {
			return null;
		}
		return block.getModel();
	}

	public Point3D getInitialPlayerPositionReal() {
		return gridToRealCoordinates(initialPlayerPosition);
	}

	public GridCoordinates getInitialPlayerPositionGrid() {
		return initialPlayerPosition;
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
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < length; j++) {
					String line = br.readLine();
					for(int k = 0; k < width; k++) {
						GridCoordinates current = new GridCoordinates(k, i, j);
						char type = line.charAt(k);
						Block currentBlock = null;
						switch(type) {
							case 'w': {
								currentBlock = new Wall(current, createTranslate(current));
							}
							break;
							case 'c': {
								currentBlock = new Crate(current, createTranslate(current));
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
		return block(gc) == null;
	}

	public boolean blocking(GridCoordinates gc, GridCoordinates direction) {
		return !empty(gc) && !pushable(gc, direction);
	}

	public boolean pushable(GridCoordinates gc, GridCoordinates direction) {
		return direction.oneDimensional() && !empty(gc) && block(gc).pushable() && empty(gc.add(direction));
	}

	public void updatePush(GridCoordinates from, GridCoordinates to) {
		Block crate = block(from);
		setBlock(from, null);
		setBlock(to, crate);
	}

}
