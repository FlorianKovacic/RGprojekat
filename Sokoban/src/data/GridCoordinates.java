package data;

public class GridCoordinates {

	private int x;
	private int y;
	private int z;

	public GridCoordinates(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public GridCoordinates add(GridCoordinates addend) {
		return new GridCoordinates(x + addend.x, y + addend.y, z + addend.z);
	}

	public GridCoordinates subtract(GridCoordinates substrahend) {
		return new GridCoordinates(x - substrahend.x, y - substrahend.y, z - substrahend.z);
	}

	@Override
	public boolean equals(Object o) {
		GridCoordinates other = (GridCoordinates) o;
		return x == other.x && y == other.y && z == other.z;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public GridCoordinates copy() {
		return new GridCoordinates(x, y, z);
	}

	public GridCoordinates projectX() {
		return new GridCoordinates(x, 0, 0);
	}

	public GridCoordinates projectY() {
		return new GridCoordinates(0, y, 0);
	}

	public GridCoordinates projectZ() {
		return new GridCoordinates(0, 0, z);
	}

	public GridCoordinates projectPlane() {
		return new GridCoordinates(x, 0, z);
	}

	public int numberOfDimensions() {
		int result = 0;
		if(x != 0) {
			result++;
		}
		if(y != 0) {
			result++;
		}
		if(z != 0) {
			result++;
		}
		return result;
	}

	public boolean oneDimensional() {
		return numberOfDimensions() == 1;
	}

}
