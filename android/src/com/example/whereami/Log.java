package com.example.whereami;

public class Log {
	
	public static int info(String msg) {
		return android.util.Log.i("INFO", msg);
	}
	
	public static int error(String msg) {
		return android.util.Log.e("ERROR", msg);
	}
	
	public static int error(String msg, Throwable t) {
//		msg = msg + (t == null ? "" : "\n" + Throwables.getStackTraceAsString(t));
//		return android.util.Log.e("ERROR", msg);
		return Log.error(msg);
	}
}
