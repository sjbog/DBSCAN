package dbscan;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DBSCANClusterer< T extends ClusterablePoint > {
	public double eps, eps2;
	public int minPts, numDimensions = 0, sortDimensionIndex = 0;
	public boolean autoSelectDimension = true;

	public DBSCANClusterer( final double eps, final int minPts ) {
		this.eps	= eps;
		this.eps2	= eps * eps;
		this.minPts	= minPts;
	}

	/**
	 step 1: sort data by a dimension
	 step 2: slide through sorted data (in parallel), and compute all points in range of eps (everything above eps is definitely isn't directly reachable)
	 step 3: build neighborhood map & proceed DFS
	 **/
	public List< List<T> > cluster( final Collection<T> data ) throws NullPointerException {
		if( data == null )
			throw new NullPointerException( "Empty clustering data" );

		int dataSize = data.size( );
		this.numDimensions = data.iterator().next().getPoint().length;

		if ( this.autoSelectDimension )
			this.sortDimensionIndex = predictDimensionByMaxVariance( data );

		ArrayList< T > dataSortedByDimension = data.stream( ).parallel( ).sorted( Comparator.comparing( ( T p ) -> p.getPoint( )[ sortDimensionIndex ] ) ).collect( Collectors.toCollection( ArrayList::new ) );

		final LinkedList< List< T > > clusters = new LinkedList<>( );
		final boolean[] visitedMap = new boolean[ dataSize ];
		final Queue< Integer >[] neighborhoodMap = buildNeighborhoodMap( dataSortedByDimension );

		ArrayList<T> cluster = new ArrayList<>( );

//		Early exit - 1 huge cluster
		if ( neighborhoodMap[ 0 ].size() == dataSize ) {
			for ( final int i : neighborhoodMap[ 0 ] )
				cluster.add( dataSortedByDimension.get( i ) );
			clusters.add( cluster );
			return clusters;
		}

		LinkedList<Integer> queue = new LinkedList<>( );

		for( int pointIndex = 0, tmpIndex; pointIndex < dataSize ; pointIndex++ ) {
			if ( visitedMap[ pointIndex ] )
				continue;

//			Expand cluster
			queue.add( pointIndex );

			while ( ! queue.isEmpty() ) {
//				DFS
				tmpIndex = queue.pollLast();
				if ( visitedMap[ tmpIndex ])
					continue;

				cluster.add( dataSortedByDimension.get( tmpIndex ) );
				visitedMap[ tmpIndex ] = true;

				queue.addAll( neighborhoodMap[ tmpIndex ] );
			}

			if ( cluster.size( ) >= minPts )
				clusters.add( cluster );

			cluster = new ArrayList<>( );
		}
		return clusters;
	}

	public double calcDistance(double[] aPoint, double[] bPoint) {
		double sum = 0.0;
		for ( int i = 0, size = numDimensions; i < size; i++ )
			sum += ( aPoint[ i ] - bPoint[ i ] )*( aPoint[ i ] - bPoint[ i ] );
		return sum;
	}

	public double calcDistance(T a, T b) {
		double sum = 0.0;
		double[] aPoint = a.getPoint(), bPoint = b.getPoint();
		for ( int i = 0, size = numDimensions; i < size; i++ )
			sum += ( aPoint[ i ] - bPoint[ i ] )*( aPoint[ i ] - bPoint[ i ] );
		return sum;
	}

	public Queue< Integer >[] buildNeighborhoodMap( final ArrayList< T > data ) {
		int dataSize = data.size();
		Queue< Integer >[] result = ( Queue< Integer >[] ) Array.newInstance( Queue.class, dataSize );

		IntConsumer fn = start -> {
				T x, head = data.get( start );
				double[] headV = head.getPoint( );
				double headDimV = headV[ sortDimensionIndex ] + eps;

				if ( result[ start ] == null )
					result[ start ] = new ConcurrentLinkedQueue<>( );
				result[ start ].add( start );

				for ( int i = start + 1 ; i < dataSize && data.get( i ).getPoint( )[ sortDimensionIndex ] <= headDimV ; i++ ) {
					x = data.get( i );

					if ( calcDistance( headV, x.getPoint( ) ) <= eps2 ) {
						result[ start ].add( i );
						if ( result[ i ] == null )
							result[ i ] = new ConcurrentLinkedQueue<>( );
						result[ i ].add( start );
					}
				}
			};

//		Early exit - 1 huge cluster
		fn.accept( 0 );

		if ( result[ 0 ].size() == dataSize )
			return result;

		IntStream.range( 1, dataSize )
				.parallel( )
				.forEach( fn );

		return result;
	}

	/**
	 * Calculate variance for each dimension (in parallel), returns dimension index with max variance
	 */
	public int predictDimensionByMaxVariance( final Collection< T > data ) {
		return ( int ) IntStream.range( 0, numDimensions )
				.parallel()
				.mapToObj( dim -> new Object[]{ dim, variance( data, dim ) } )
				.reduce( ( a, b ) -> ( double ) a[ 1 ] > ( double ) b[ 1 ] ? a : b )
				.get()[ 0 ];
	}

	public static double variance( double[] data ) {
		double avg = 0.0, sum = 0.0, delta;
		for( int i = 0, size = data.length; i < size; i++ ) {
			delta = data[i] - avg;
			avg += delta / ( i+1 );
			sum += delta * ( data[i] - avg );
		}
		if ( data.length < 2 )
			return 0.0;
		return sum / ( data.length - 1 );
	}

	public double variance( final Collection< T > data, final int dimension ) {
		double avg = 0.0, sum = 0.0, delta, v;
		int i = 0;
		for( final T point : data ) {
			v = point.getPoint()[ dimension ];
			delta = v - avg;
			avg += delta / ++i;
			sum += delta * ( v - avg );
		}
		int size = data.size( );
		if ( size < 2 )
			return 0.0;
		return sum / ( size - 1 );
	}
}