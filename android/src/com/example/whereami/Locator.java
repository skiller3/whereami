package com.example.whereami;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

public class Locator {

	private static final String PROVIDER = LocationManager.NETWORK_PROVIDER;
	private static final long MIN_UPDATE_TIME = 30l * 1000l;
	private static final float MIN_UPDATE_DISTANCE = 0f;
	private static final long TIMEOUT = 30l;
	
	private final LocationListener LOCATION_LISTENER = new LocationListener() {
		@Override
		public void onLocationChanged(android.location.Location location) {
			try {
				if (!firstLocationObtained()) {
					boolean timedOut = !initQueue.offer(new Location(location), TIMEOUT, TimeUnit.SECONDS);
					if (timedOut) {
						Log.info("Initialization queue offering timed out.");
					}
				}
			} catch (Throwable t) {
				Log.error("Failed to capture initial location", t);
			}
			Log.info("*****Received location changed signal: " + location);
		}
		@Override
		public void onProviderDisabled(String provider) {
			Log.info("*****Received disabled signal: " + provider);
		}
		@Override
		public void onProviderEnabled(String provider) {
			Log.info("*****Received enabled signal: " + provider);
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			String msg = "*****Received provider status changed signal: " + provider + 
					", status = " + status + ", extras = " + String.valueOf(extras);
			Log.info(msg);
		}
	};
	
	private Activity activity;
	private LocationManager manager;
	private SynchronousQueue<Location> initQueue = new SynchronousQueue<Location>();
	private Looper locationLooper;
	
	public Locator(Activity activity) {
		this.activity = activity;
	}
	
	public void startListening() throws LocatorException {
		try {
			manager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
			
			final SynchronousQueue<Looper> sync = new SynchronousQueue<Looper>();
			Thread looperThread = new Thread() {
				@Override
				public void run() {
					try {
						Looper.prepare();
						boolean received = sync.offer(Looper.myLooper(), TIMEOUT, TimeUnit.SECONDS);
						if (received) Looper.loop();
					} catch (Throwable t) {
						Log.error("Looper thread failure experienced.", t);
					}
				}
			};
			looperThread.start();
			
			try {
				locationLooper = sync.poll(TIMEOUT, TimeUnit.SECONDS);
			} catch (Throwable t) {
				String msg = "Unable to obtain reference to locator looper.";
				throw new LocatorException(msg, t);
			}
			
			manager.requestLocationUpdates (
					PROVIDER, 
					MIN_UPDATE_TIME, 
					MIN_UPDATE_DISTANCE,
					LOCATION_LISTENER,
					locationLooper
			);
		} catch (Throwable t) {
			String msg = "Failed to start listening to location data.";
			throw new LocatorException(msg, t);
		}
	}
	
	public void stopListening() throws LocatorException {
		try {
			manager.removeUpdates(LOCATION_LISTENER);
			locationLooper.quit();
		} catch (Throwable t) {
			String msg = "Failed to stop listening to location data.";
			throw new LocatorException(msg, t);
		}
	}
	
	public Location locate() throws LocatorException {
		try {
			Location location = null;
			
			if (firstLocationObtained()) {
				initQueue.poll(); // just in case there is something trapped in the queue
				location = getLastKnownLocation();
			}
			else {
				location = initQueue.poll(TIMEOUT, TimeUnit.SECONDS);
				if (location == null) {
					throw new LocatorException("Location service timed out: " + TIMEOUT + " seconds");
				}
			}
					
			return location;
		} catch (LocatorException le) {
			throw le;
		} catch (Throwable t) {
			String msg = "Unable to determine device location.";
			throw new LocatorException(msg, t);
		}
	}
	
	private boolean firstLocationObtained() {
		return getLastKnownLocation() != null;
	}
	
	private Location getLastKnownLocation() {
		android.location.Location loc = manager.getLastKnownLocation (
				PROVIDER
		);
		return loc == null ? null : new Location(loc);
	}
	
	@SuppressWarnings("serial")
	public static class LocatorException extends Exception {
		public LocatorException(String msg, Throwable t) {
			super(msg, t);
		}
		public LocatorException(String msg) {
			super(msg);
		}
	}
	
	public static class Location {
		public Double latitude;
		public Double longitude;
		public Double altitude;
		public String time;
		
		public Location(android.location.Location location) {
			this.latitude = location.getLatitude();
			this.longitude = location.getLongitude();
			this.altitude = location.getAltitude();
		}
	}
	
	
}
