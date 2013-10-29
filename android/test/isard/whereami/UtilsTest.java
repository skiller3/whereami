package isard.whereami;

import junit.framework.TestCase;

import org.junit.Test;

public class UtilsTest extends TestCase {
	
	@Test
	public void testNorthToSouthDistance() {
		assertEquals(Utils.EARTH_RADIUS_METERS * Math.PI, Utils.calcEarthSurfaceDistance(90d, 0d, -90d, 0d));
	}
	
	@Test
	public void testEastToWestDistance() {
		assertEquals(Utils.EARTH_RADIUS_METERS * Math.PI, Utils.calcEarthSurfaceDistance(0d, 0d, 0d, 180d));
	}
	
	@Test
	public void testDistance1() {
		assertEquals(349373.1352448918d, Utils.calcEarthSurfaceDistance(42.3581, -71.0636, 40.67, -73.94));
	}
	
	@Test
	public void testDistance2() {
		assertEquals(1.444679622338518E7d, Utils.calcEarthSurfaceDistance(30.48, 70.85, 10.80, -55.35));
	}
	
	@Test
	public void testDistance3() {
		assertEquals(1.6266660956306716E7d, Utils.calcEarthSurfaceDistance(30.48, 70.85, -10.80, -55.35));
	}
}
