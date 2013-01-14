/**
 * 
 */
package com.tbs.frc.robotsim;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * @author dtruby
 * 
 */
public class TbsPreferenceActivity extends PreferenceActivity implements
		Thread.UncaughtExceptionHandler {

	public TbsPreferenceActivity() {
		super();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle pSavedInstanceState) {

		try {

			// insert ourself into the vm crash (force close) handler
			Thread.setDefaultUncaughtExceptionHandler(this);

			super.onCreate(pSavedInstanceState);

			this.addPreferencesFromResource(R.xml.tbspreferences);

		} catch (Exception anEx) {
			this.error("onCreate()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);

		}
	}

	@Override
	public void uncaughtException(final Thread pThread, Throwable pThrowable) {

		try {

			this.error("uncaughtException()", "Exception name="
					+ pThrowable.getClass().getName() + ", message="
					+ pThrowable.getMessage() + ".", pThrowable);

		} catch (Exception anEx) {
			this.error("uncaughtException()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);

		} finally {

			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}
	}

	protected void debug(final String pMethod, final String pMessage) {
		Log.d(this.getClassName() + " " + pMethod, pMessage);
	}

	protected void info(final String pMethod, final String pMessage) {
		Log.i(this.getClassName() + " " + pMethod, pMessage);
	}

	protected void error(final String pMethod, final String pMessage,
			final Exception anEx) {
		Log.e(this.getClassName() + " " + pMethod, pMessage);
		if (null != anEx) {
			anEx.printStackTrace();
		}
	}

	protected void error(final String pMethod, final String pMessage,
			final Throwable anEx) {
		Log.e(this.getClassName() + " " + pMethod, pMessage);
		if (null != anEx) {
			anEx.printStackTrace();
		}
	}

	private String getClassName() {
		String aClassName = this.getClass().getName();
		return aClassName.substring(aClassName.lastIndexOf('.') + 1);
	}

}
