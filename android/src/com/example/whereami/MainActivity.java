package com.example.whereami;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.example.whereami.Locator.Location;
import com.example.whereami.Locator.LocatorException;
import com.example.whereami.Publisher.PublisherException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static final long FIVE_MIN_IN_MILLISEC = 5l * 60l * 1000l;
	public static final String DATE_FORMAT = "MM/dd/yy kk:mm:ss";
	
	private boolean trackingEnabled = false;
	private Locator locator;
	private Publisher publisher;
	
	private Timer timer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void toggleEnabled(View toggleView) {
		Button toggleButton = ((Button)toggleView);
		String text = toggleButton.getText().toString();
		
		trackingEnabled = "Enable!".equals(text);
		if (trackingEnabled) {
			startTracking();
			toggleButton.setText("Disable!");
		}
		else {
			cancelTracking();
			toggleButton.setText("Enable!");
		}
	}
	
	private void startTracking() {
		Status failureStatus = null;
		Throwable exc = null;
		try {
			switchStatus(Status.INITIALIZING);
			this.locator = new Locator(this);
			this.locator.startListening();
		} catch (Throwable t) {
			exc = t;
			failureStatus = Status.LOCATOR_ERROR;
		}
		
		try {
			this.publisher = new Publisher();
		} catch (Throwable t) {
			exc = t;
			failureStatus = Status.PUBLISHER_ERROR;
		}
		
		try {
			this.timer = new Timer();
			this.timer.schedule(new TrackerTask(), 0l, FIVE_MIN_IN_MILLISEC);
		} catch (Throwable t) {
			exc = t;
			failureStatus = Status.UNKNOWN_ERROR;
		}
		
		if (failureStatus != null) {
			String msg = "Unable to start tracking location.";
			Log.error(msg, exc);
			cancelTracking();
			switchStatus(failureStatus);
		}
	}
	
	private void cancelTracking() {
		try {
			this.locator.stopListening();
		} catch (Throwable t) {
			Log.error("Unable to force locator to stop listening.", t);
		}
		try {
			this.timer.cancel();
		} catch (Throwable t) {
			Log.error("Unable to cancel publication timer.", t);
		}
		try {
			this.timer.purge();
		} catch (Throwable t) {
			Log.error("Unable to purge publication timer.", t);
		}
		switchStatus(Status.OFF);
	}
	
	private void switchStatus(final Status status) {
		final TextView statusView = (TextView)findViewById(R.id.statusText);
		
		if (isUiThread()) {
			statusView.setText("Status: " + status.label);
		}
		else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					statusView.setText("Status: " + status.label);
				}
			});
		}
		
	}
	
	private void setLastTransmissionAttempt(final String dateStr) {
		final TextView lastTransmissionAttempt = (TextView)findViewById(R.id.lastTransmissionAttempt);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lastTransmissionAttempt.setText("Last Transmission Attempt: " + dateStr);
			}
		});
	}
	
	private void setLastTransmission(final String dateStr) {
		final TextView lastTransmission = (TextView)findViewById(R.id.lastTransmission);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				lastTransmission.setText("Last Transmission: " + dateStr);
			}
		});
	}
	
	private static boolean isUiThread() {
		return Looper.getMainLooper() == Looper.myLooper();
	}
	
	private class TrackerTask extends TimerTask {
		@Override
		public void run() {
			String nowString = null;
			try {
				Date now = new Date();
				nowString = DateFormat.format(DATE_FORMAT, now).toString();
				
				Location location = locator.locate();
				location.time = nowString;
				
				publisher.publish(location);
				
				setLastTransmission(nowString);
				
				switchStatus(Status.SYSTEM_HEALTHY);
			} catch (PublisherException pe) { 
				switchStatus(Status.PUBLISHER_ERROR);
				Log.error("Failed to publish location.", pe);
			} catch (LocatorException le) {
				switchStatus(Status.LOCATOR_ERROR);
				Log.error("Failed to determine location.", le);
			} catch (Throwable t) {
				switchStatus(Status.UNKNOWN_ERROR);
				Log.error("Encountered unknown error.", t);
			} finally {
				setLastTransmissionAttempt(nowString);
			}
		}
	}
	
	private static enum Status {
		OFF("Off"),
		INITIALIZING("Initializing..."),
		LOCATOR_ERROR("Locator Error"),
		PUBLISHER_ERROR("Publisher Error"),
		UNKNOWN_ERROR("Unknown Error"),
		SYSTEM_HEALTHY("System Healthy");
		
		private String label;
		
		private Status(String label) {
			this.label = label;
		}
	}
	
}
