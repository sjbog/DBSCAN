package dbscan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRunner {
	public static void main( String[] args ) {
		List< NamedDoublePoint > data = Arrays.asList(
				new NamedDoublePoint( "0", new double[] { 2, 4 } )
				, new NamedDoublePoint( "1", new double[] { 7, 3 } )
				, new NamedDoublePoint( "2", new double[] { 3, 5 } )
				, new NamedDoublePoint( "3", new double[] { 5, 3 } )
				, new NamedDoublePoint( "4", new double[] { 7, 4 } )
				// Noise point
				, new NamedDoublePoint( "5", new double[] { 6, 8 } )
				, new NamedDoublePoint( "6", new double[] { 6, 5 } )
				, new NamedDoublePoint( "7", new double[] { 8, 4 } )
				, new NamedDoublePoint( "8", new double[] { 2, 5 } )
				, new NamedDoublePoint( "9", new double[] { 3, 7 } )
		);

		DBSCANClusterer< NamedDoublePoint > clusterer = new DBSCANClusterer<>( 2.0, 2 );
		List< List< NamedDoublePoint > > res = clusterer.cluster( data );

		List< List< NamedDoublePoint > > expectedClusters = Arrays.asList(
				Arrays.asList( data.get( 0 )
						, data.get( 2 )
						, data.get( 8 )
						, data.get( 9 )
				)
				, Arrays.asList( data.get( 1 )
						, data.get( 3 )
						, data.get( 4 )
//						, data.get( 5 ) // Noise point
						, data.get( 6 )
						, data.get( 7 )
				)
		);

		boolean testResult = true;

		for ( final List< NamedDoublePoint > cluster : res ) {
			System.out.println( "Cluster size: " + cluster.size() );
			cluster.forEach( p ->
				System.out.println( String.format( " Point: %s, %s", p.name, Arrays.toString( p.getPoint( ) ) ) )
			);

			Set< NamedDoublePoint > set = new HashSet<>(  );
			set.addAll( cluster );
			testResult &= set.containsAll( expectedClusters.get( ( cluster.size() == 4 ) ? 0 : 1 ) );
		}

		System.out.println( String.format( "\nExpected test passed: %b", testResult ) );
	}
}