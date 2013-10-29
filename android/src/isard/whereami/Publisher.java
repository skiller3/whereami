package isard.whereami;

import isard.whereami.Locator.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;

public class Publisher {

	private static final long TIMEOUT = 30;
	private static final int MAX_LOCATIONS = 500;
	private static final String LATITUDE_KEY = "latitude";
	private static final String LONGITUDE_KEY = "longitude";
	private static final float MIN_UPDATE_DISTANCE = 700f;
	
	public void publish(Location location) throws PublisherException {
		try {
			final SynchronousQueue<Boolean> sync = new SynchronousQueue<Boolean>();
			new Thread(new PublisherRunnable(sync, location)).start();
			Boolean published = sync.poll(TIMEOUT, TimeUnit.SECONDS);
			
			if (published == null) {
				String msg = "Unable to publish location data within " + TIMEOUT + 
						" seconds.";
				throw new PublisherException(msg);
			}
		} catch (PublisherException pe) { 
			throw pe;
		} catch (Throwable t) {
			String msg = "Unable to publish location data.";
			throw new PublisherException(msg, t);
		}
	}
	
	@SuppressWarnings("serial")
	public static class PublisherException extends Exception {
		public PublisherException(String msg, Throwable t) {
			super(msg, t);
		}
		public PublisherException(String msg) {
			super(msg);
		}
	}
	
	private class PublisherRunnable implements Runnable {

		private SynchronousQueue<Boolean> sync;
		private Location location;
		
		public PublisherRunnable(SynchronousQueue<Boolean> sync, Location location) {
			this.sync = sync;
			this.location = location;
		}
		
		@Override
		public void run() {
			final Firebase locations = new Firebase("https://touch.firebaseio.com/locations");
			locations.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onCancelled() {
					// NO-OP
				}
				@Override
				public void onDataChange(DataSnapshot arg0) {
					@SuppressWarnings("unchecked")
					List<Object> locationValues = (List<Object>)arg0.getValue();
					if (locationValues == null) locationValues = new ArrayList<Object>();
					
					Double distanceOfChange = null;
					if (locationValues.size() > 0) {
						@SuppressWarnings("unchecked")
						Map<String,Object> prevLocation = (Map<String,Object>)locationValues.get (
								locationValues.size() - 1
						);
						Double prevLat = (Double)prevLocation.get(LATITUDE_KEY);
						Double prevLng = (Double)prevLocation.get(LONGITUDE_KEY);
						distanceOfChange = Utils.calcEarthSurfaceDistance (
							location.latitude, location.longitude, prevLat, prevLng
						);
					}

					if (distanceOfChange == null || distanceOfChange > MIN_UPDATE_DISTANCE) {
						locationValues.add(location);
						if (locationValues.size() > MAX_LOCATIONS) {
							locationValues.remove(0);
						}
						locations.setValue(locationValues);
					}

					try {
						sync.offer(true, TIMEOUT, TimeUnit.SECONDS);
					} catch (Throwable t) {
						// NO-OP
					}
				}
			});
		}
		
	}
}
