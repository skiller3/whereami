package isard.whereami;

public class Utils {
	public static final Double EARTH_RADIUS_METERS = 6371000d;
	public static final Double EARTH_CIRCUM_METERS = 2d * EARTH_RADIUS_METERS * Math.PI;
	
	public static Double calcEarthSurfaceDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
		// Standardize angles to comply with the unit circle
		if (lng1 < 0d) lng1 += 360d;
		if (lng2 < 0d) lng2 += 360d;
		
		if (lng1 > 90d && lng1 < 270d) {
			if (lat1 >= 0d) lat1 = 180d - lat1;
			else lat1 = 180d + Math.abs(lat1);
		}
		else {
			if (lat1 < 0d) lat1 += 360d;
		}
		
		if (lng2 > 90d && lng2 < 270d) {
			if (lat2 >= 0d) lat2 = 180d - lat2;
			else lat2 = 180d + Math.abs(lat2);
		}
		else {
			if (lat2 < 0d) lat2 += 360d;
		}
		
		// Convert to Euclidian coordinates
		Double x1 = EARTH_RADIUS_METERS * Math.sin(toRadians(lng1));
		Double y1 = EARTH_RADIUS_METERS * Math.cos(toRadians(lng1));
		Double z1 = EARTH_RADIUS_METERS * Math.sin(toRadians(lat1));
		Double x2 = EARTH_RADIUS_METERS * Math.sin(toRadians(lng2));
		Double y2 = EARTH_RADIUS_METERS * Math.cos(toRadians(lng2));
		Double z2 = EARTH_RADIUS_METERS * Math.sin(toRadians(lat2));
		
		// Compute direct Euclidian shortest-path distance
		Double euclidianDist = Math.sqrt (
			(x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) + (z2 - z1) * (z2 - z1)
		);
		
		// Compute the magnitude of the angle formed by lines to the two points from
		// the Earth's center
		Double angleAtOrigin = 2d * Math.asin(0.5 * euclidianDist / EARTH_RADIUS_METERS);
		
		return (angleAtOrigin / (2d * Math.PI)) * EARTH_CIRCUM_METERS;
	}
	
	public static Double toRadians(Double degrees) {
		return 2 * Math.PI * (degrees / 360d);
	}
	
}
