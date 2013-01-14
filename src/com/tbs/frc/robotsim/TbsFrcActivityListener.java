/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import java.util.Map;
import java.util.TreeMap;

import edu.wpi.first.wpilibj.tables.IRemote;
import edu.wpi.first.wpilibj.tables.IRemoteConnectionListener;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * 
 */
public abstract class TbsFrcActivityListener extends TbsFrcActivityBase
		implements IRemoteConnectionListener, ITableListener {

	public static final String CONNECTION_NOTCONNECTED = "No Connections";
	public static final String CONNECTION_CONNECTED = "Connected";

	private Map<Integer, String> connectionsMap = new TreeMap<Integer, String>();
	private String connectionMessage = TbsFrcActivityListener.CONNECTION_NOTCONNECTED;

	/**
	 * default null constructor.
	 */
	public TbsFrcActivityListener() {
		super();
	}

	@Override
	protected void onResume() {
		this.connectionsMap = new TreeMap<Integer, String>();
		super.onResume();
	}

	// protected boolean isConnected() {
	// return TbsFrcActivityListener.CONNECTION_CONNECTED
	// .equals(this.connectionMessage);
	// }

	/**
	 * ITableListener method.
	 */
	public void valueChanged(ITable pTable, String pKey, Object pValue,
			boolean pIsNew) {

		this.debug("valueChanged()", "Called, pKey=" + pKey + ", pValue="
				+ pValue + ", pIsNew=" + pIsNew + ".");

	}

	protected abstract void displayConnected();

	/**
	 * IRemoteConnectionListener method.
	 */
	public void connected(IRemote pRemote) {

		this.debug("connected()", "Called pRemote="
				+ (null == pRemote ? "null" : pRemote.getClass().getName())
				+ ".");

		final boolean isMe = pRemote.isServer();

		this.debug("connected()", "isMe=" + isMe + ".");

		if (!isMe) {

			final Integer aHashCode = Integer.valueOf(pRemote.hashCode());

			this.debug("connected()", "aHashCode=" + aHashCode + ".");

			this.connectionsMap.put(aHashCode, "");

			this.connectionMessage = (this.connectionsMap.size() > 0 ? TbsFrcActivityListener.CONNECTION_CONNECTED
					: TbsFrcActivityListener.CONNECTION_NOTCONNECTED);

			this.displayConnected();
		}
	}

	/**
	 * IRemoteConnectionListener method.
	 */
	public void disconnected(IRemote pRemote) {

		this.debug("disconnected()", "Called.");

		final Integer aHashCode = Integer.valueOf(pRemote.hashCode());

		this.connectionsMap.remove(aHashCode);

		this.connectionMessage = (this.connectionsMap.size() > 0 ? TbsFrcActivityListener.CONNECTION_CONNECTED
				: TbsFrcActivityListener.CONNECTION_NOTCONNECTED);

		this.displayConnected();
	}

	protected String getConnectionMessage() {
		return this.connectionMessage;
	}

	protected String getErrorMessage() {
		return "";
	}

}
