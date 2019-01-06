package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Translate;
import main.Main;

public class State {

	private int width;
	private int height;
	private int length;
	private Block[][][] blocks;
	private Point3D initialPlayerPosition;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getLength() {
		return length;
	}

	private Point3D gridToRealCoordinates(GridCoordinates gc) {
		return new Point3D(gc.getX() - (width - 1)/2.0, -(gc.getY() + 0.5), (length - 1)/2.0 - gc.getZ()).multiply(Main.SIZE);
	}

	public Node getBlock(int x, int y, int z) {
		Block block = blocks[x][y][z];
		if(block == null) {
			return null;
		}
		return block.getModel();
	}

	public Point3D getInitialPlayerPosition() {
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
			String[] dimensions = dimensionsString.split(" ");
			width = Integer.parseInt(dimensions[0]);
			height = Integer.parseInt(dimensions[1]);
			length = Integer.parseInt(dimensions[2]);
			blocks = new Block[width][height][length];
			for(int i = 0; i < height; i++) {
				for(int j = 0; j < length; j++) {
					String line = br.readLine();
					for(int k = 0; k < width; k++) {
						GridCoordinates current = new GridCoordinates(k, i, j);
						char type = line.charAt(k);
						switch(type) {
							case 'w': {
								blocks[k][i][j] = new Wall(current, createTranslate(current));
							}
							break;
							case 's': {
								blocks[k][i][j] = null;
							}
							break;
							case 'b': {
								initialPlayerPosition = gridToRealCoordinates(current);
							}
							break;
							default: {
								System.out.println("Input file is not of the correct format.");
								blocks = null;
								return;
							}
						}
					}
				}
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

}
