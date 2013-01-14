/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import android.app.Activity;
import android.util.Log;

/**
 * 
 */
public abstract class TbsFrcActivityBase extends Activity {

	/**
	 * 
	 */
	public TbsFrcActivityBase() {
		super();
	}

	protected String formatLocalIpAddress() {
		String aLocalIpAddres = "";

		try {

			InetAddress anInetAddress = null;
			NetworkInterface aNetworkInterface = null;

			for (Enumeration<NetworkInterface> aNetworkInterfaceEnumeration = NetworkInterface
					.getNetworkInterfaces(); aNetworkInterfaceEnumeration
					.hasMoreElements();) {

				aNetworkInterface = aNetworkInterfaceEnumeration.nextElement();

				for (Enumeration<InetAddress> aInetAddressEnum = aNetworkInterface
						.getInetAddresses(); aInetAddressEnum.hasMoreElements();) {

					anInetAddress = aInetAddressEnum.nextElement();

					if (anInetAddress instanceof Inet4Address) {

						if (!anInetAddress.isLoopbackAddress()) {

							aLocalIpAddres = anInetAddress.getHostAddress();

						}

					}
				}

			}

		} catch (SocketException ex) {
			this.debug("formatLocalIpAddress()", "SocketException message="
					+ ex.getMessage() + ".");
		}

		return "This phone's IP Address:" + aLocalIpAddres;
	}

	protected String formatNow() {
		final SimpleDateFormat aFormat = new SimpleDateFormat(
				"yyyy/MM/dd_HH:mm:ss.SSS");
		return aFormat.format(new Date());
	}

	protected boolean isNullOrBlank(final String pValue) {
		return ((null == pValue) || (pValue.length() == 0));
	}

	protected void debug(final String pMethod, final String pMessage) {
		Log.d(this.getClassName() + " " + pMethod, pMessage);
	}

	protected void error(final String pMethod, final String pMessage,
			final Exception anEx) {
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
