/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public abstract class SubSystemBase {

	public static final String NETWORKTABLE_KEY_DATA = "Data";
	public static final String NETWORKTABLE_KEY_TYPE = "~TYPE~";

	public static final String NETWORKTABLE_NAME_TYPE_VALUE_SUBSYSTEM = "Subsystem";
	public static final String NETWORKTABLE_NAME_TYPE_VALUE_SCHEDULER = "Scheduler";
	public static final String NETWORKTABLE_NAME_TYPE_VALUE_COMMAND = "Command";
	public static final String NETWORKTABLE_NAME_TYPE_VALUE_BUTTON = "Button";

	private static final String HASDEFAULT = "hasDefault";
	private static final String RUNNING = "running";
	private static final String ISPARENTED = "isParented";
	private static final String NAME = "name";
	private static final String COMMAND = "command";
	private static final String HASCOMMAND = "hasCommand";
	private static final String COUNT = "count";
	private static final String PRESSED = "pressed";

	private Context context = null;

	public SubSystemBase(final Context pContext) {
		super();
		if (null == pContext) {
			throw new IllegalArgumentException("Context is null");
		}
		this.context = pContext;
	}

	protected SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this.context);
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

	protected void setupSubSystem(ITable aNetworkTable,
			final String pDefaultCommand, final String pCurrentCommand) {

		aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
				SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_SUBSYSTEM);

		ITable aDataTable = aNetworkTable
				.getSubTable(SubSystemBase.NETWORKTABLE_KEY_DATA);

		if (null == pDefaultCommand) {
			aDataTable.putBoolean(SubSystemBase.HASDEFAULT, false);
		} else {
			aDataTable.putBoolean(SubSystemBase.HASDEFAULT, true);

			ITable aCommandTable = aDataTable
					.getSubTable(SubSystemBase.COMMAND);
			aCommandTable.putBoolean(SubSystemBase.RUNNING, true);
			aCommandTable.putBoolean(SubSystemBase.ISPARENTED, false);
			aCommandTable.putString(SubSystemBase.NAME, pDefaultCommand);

		}

		if (null == pCurrentCommand) {
			aDataTable.putBoolean(SubSystemBase.HASCOMMAND, false);
		} else {
			aDataTable.putBoolean(SubSystemBase.HASCOMMAND, true);

			ITable aCommandTable = aDataTable
					.getSubTable(SubSystemBase.COMMAND);
			aCommandTable.putBoolean(SubSystemBase.RUNNING, true);
			aCommandTable.putBoolean(SubSystemBase.ISPARENTED, false);
			aCommandTable.putString(SubSystemBase.NAME, pCurrentCommand);

		}

	}

	protected void setupScheduler(ITable aNetworkTable,
			final String[] pCommandArray) {

		aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
				SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_SCHEDULER);

		if (null != pCommandArray) {

			ITable aDataTable = aNetworkTable
					.getSubTable(SubSystemBase.NETWORKTABLE_KEY_DATA);

			aDataTable.putNumber(SubSystemBase.COUNT, pCommandArray.length);

			for (int idx = 0; idx < pCommandArray.length; idx++) {

				ITable aCommandTable = aDataTable.getSubTable(Integer
						.toString(idx + 1));
				aCommandTable.putBoolean(SubSystemBase.RUNNING, true);
				aCommandTable.putBoolean(SubSystemBase.ISPARENTED, false);
				aCommandTable.putString(SubSystemBase.NAME, pCommandArray[idx]);

			}
		}
	}

	protected void setupButton(ITable aNetworkTable, final Boolean pPressed) {

		aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
				SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_BUTTON);

		ITable aDataTable = aNetworkTable
				.getSubTable(SubSystemBase.NETWORKTABLE_KEY_DATA);

		aDataTable.putBoolean(SubSystemBase.PRESSED, pPressed);

	}

}
