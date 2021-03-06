package dbscan;

public class NamedDoublePoint extends ClusterablePoint {
	public String name = "";

	public NamedDoublePoint( String name, double[] point ) {
		this.name	= name;
		this.point	= point;
	}

	public String toString() {
		return String.format( "\"%s\": %s", name, super.toString() );
	}
}