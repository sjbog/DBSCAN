package dbscan;

import java.util.Arrays;

public abstract class ClusterablePoint {
	public double[] point;

	public double[] getPoint() {
		return this.point;
	}

	public String toString() {
		return Arrays.toString( this.point );
	}
}