/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.tbs.widgit.VerticalSeekBar;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * Main activity.
 */
public class TbsFrcRobotMainActivity extends TbsFrcActivityListener implements
		OnClickListener {

	public static final String PREF_NAME_NETWORKTABLE_NAME = "TbsNetworktableName";
	public static final String PREF_NAME_NETWORKTABLE_NAME_DEFAULT = "LiveWindow"; // "SmartDashboard";

	public static final String PREF_NAME_LEFT_DRIVE = "TbsControlLeftDrive";
	public static final String DRIVE_LEFT_SPEED = "Drive Left Motor Speed";

	public static final String PREF_NAME_RIGHT_DRIVE = "TbsControlRightDrive";
	public static final String DRIVE_RIGHT_SPEED = "Drive Right Motor Speed";

	public static final String PREF_NAME_EXTEND_BEACH = "TbsControlExtendBeach";
	public static final String PREF_NAME_RETRACT_BEACH = "TbsControlRetractBeach";
	public static final String PREF_NAME_GATHERER = "TbsControlGatherer";
	public static final String PREF_NAME_SHOOT = "TbsControlShoot";
	public static final String BUTTON_FIRE = "Fire Button";

	public static final String PREF_NAME_TELEOP = "TbsControlTeleop";

	public static final String NETWORK_TABLE_NAME_PREFERENCES_NAME = "Preferences";
	public static final String NETWORK_TABLE_NAME_LOGGER = "WkwFrcLogger";
	public static final String TABLE_DATA = "Data";

	private NetworkTable dashboardNetworkTable = null;
	private TextView messageTextView = null;
	private TextView connectedTextView = null;
	private ToggleButton teleopButton = null;
	private ToggleButton beachExtendButton = null;
	private ToggleButton beachRetractButton = null;
	private ToggleButton gathererOnButton = null;
	private Button shootButton = null;
	private VerticalSeekBar leftDrive = null;
	private TextView leftDriveLabel = null;
	private VerticalSeekBar rightDrive = null;
	private TextView rightDriveLabel = null;
	private Chronometer timer = null;
	private Robot robot = null;

	@Override
	public void onCreate(final Bundle pSavedInstanceState) {

		try {

			super.onCreate(pSavedInstanceState);

			this.setContentView(R.layout.main);

			this.initPreferences();

			this.setupBinding();

		} catch (Exception anEx) {
			this.error("onCreate()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	@Override
	protected void onResume() {

		try {

			super.onResume();

			this.debug("onResume()", "Called.");

			this.startServing();

			this.bindValues();

			if (null != this.timer) {
				this.timer.start();
			}

			if (null != this.messageTextView) {
				this.messageTextView.setText(this.formatLocalIpAddress());
			}

			this.displayConnected();

		} catch (Exception anEx) {
			this.error("onResume()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	private void startServing() {

		if (null == this.dashboardNetworkTable) {

			final String aNetworkTableName = this
					.getSharedPreferences()
					.getString(
							TbsFrcRobotMainActivity.PREF_NAME_NETWORKTABLE_NAME,
							TbsFrcRobotMainActivity.PREF_NAME_NETWORKTABLE_NAME_DEFAULT);

			this.dashboardNetworkTable = NetworkTable
					.getTable(aNetworkTableName);

			this.robot = new Robot(this.getApplicationContext(),
					this.dashboardNetworkTable, this.beachExtendButton,
					this.beachRetractButton, this.leftDriveLabel,
					this.rightDriveLabel, this.shootButton);

			try {

				this.dashboardNetworkTable.addConnectionListener(this, true);

				this.dashboardNetworkTable.addTableListener(this, false);

				this.debug("startServing()", "Listener started.");

			} catch (Exception anEx) {
				this.debug("startServing()",
						"Exception message=" + anEx.getMessage() + ".");
			}

		}

	}

	@Override
	protected void onPause() {

		try {

			this.debug("onPause()", "Called.");

			if (null != this.timer) {
				this.timer.stop();
			}

			if (null != this.dashboardNetworkTable) {

				this.dashboardNetworkTable.removeTableListener(this);

				this.dashboardNetworkTable.removeConnectionListener(this);

				this.dashboardNetworkTable = null;

				this.debug("onPause()", "Listener stopped.");
			}

			super.onPause();

		} catch (Exception anEx) {
			this.error("onPause()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}

	}

	@Override
	public void onClick(final View pView) {

		try {

			switch (pView.getId()) {

			case R.id.tbsrefresh:

				this.debug("onClick()", "Refresh clicked.");
				this.displayConnected();
				break;

			case R.id.tbssenddata:

				this.debug("onClick()", "Send data clicked.");
				this.displayConnected();

				if (null != this.dashboardNetworkTable) {
					// nothing
				}
				break;

			}

		} catch (Exception anEx) {
			this.error("onClick()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {

		this.getMenuInflater().inflate(R.menu.tbsmainoptionmenu, pMenu);

		// Calling super after populating the menu is necessary here to ensure
		// that the
		// action bar helpers have a chance to handle this event.
		return super.onCreateOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem pMenuItem) {
		boolean isHandled = false;

		try {

			switch (pMenuItem.getItemId()) {

			case R.id.thsmainmenuhelp:

				this.startActivity(new Intent(this, TbsFrcHelpActivity.class));
				isHandled = true;
				break;

			case R.id.thsmainmenupref:

				this.startActivity(new Intent(this, TbsPreferenceActivity.class));
				isHandled = true;
				break;

			default:
				isHandled = super.onOptionsItemSelected(pMenuItem);
				break;

			}

		} catch (Exception anEx) {
			this.error("onOptionsItemSelected()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}

		return isHandled;
	}

	protected void displayConnected() {

		if (null != this.connectedTextView) {

			this.connectedTextView.post(new Runnable() {

				@Override
				public void run() {

					if (TbsFrcRobotMainActivity.this.dashboardNetworkTable
							.isConnected()) {

						TbsFrcRobotMainActivity.this.connectedTextView
								.setText(TbsFrcActivityListener.CONNECTION_CONNECTED);
						TbsFrcRobotMainActivity.this.connectedTextView
								.setBackgroundColor(0xff33ff00);

					} else {

						TbsFrcRobotMainActivity.this.connectedTextView
								.setText(TbsFrcActivityListener.CONNECTION_NOTCONNECTED);
						TbsFrcRobotMainActivity.this.connectedTextView
								.setBackgroundColor(0xffcc0000);

					}
				}
			});

		}
	}

	private void setupBinding() {

		this.messageTextView = (TextView) this.findViewById(R.id.tbsmsg);

		this.connectedTextView = (TextView) this
				.findViewById(R.id.tbsconnected);

		this.connectedTextView.setTextColor(0xff000000);
		this.connectedTextView.setVisibility(View.INVISIBLE);

		this.beachExtendButton = (ToggleButton) this
				.findViewById(R.id.tbsbeachExtend);

		this.beachRetractButton = (ToggleButton) this
				.findViewById(R.id.tbsbeachRetract);

		this.leftDriveLabel = (TextView) this
				.findViewById(R.id.tbsleftdrivelbl);

		this.rightDriveLabel = (TextView) this
				.findViewById(R.id.tbsrightdrivelbl);

		this.shootButton = (Button) this.findViewById(R.id.tbsshoot);

		this.timer = (Chronometer) this.findViewById(R.id.tbstimer);
		if (null != this.timer) {
			this.timer.setBase(SystemClock.elapsedRealtime());
		}

		this.teleopButton = (ToggleButton) this
				.findViewById(R.id.tbsrobotteleop);

		this.gathererOnButton = (ToggleButton) this
				.findViewById(R.id.tbsgathererOn);

		this.leftDrive = (VerticalSeekBar) this.findViewById(R.id.tbsleftdrive);
		if (null != this.leftDrive) {
			this.leftDrive.setMax(200);
			this.leftDrive.setProgress(100);
		}

		this.rightDrive = (VerticalSeekBar) this
				.findViewById(R.id.tbsrightdrive);
		if (null != this.rightDrive) {
			this.rightDrive.setMax(200);
			this.rightDrive.setProgress(100);
		}

		((Button) this.findViewById(R.id.tbsrefresh)).setOnClickListener(this);
		((Button) this.findViewById(R.id.tbssenddata)).setOnClickListener(this);

	}

	private void bindValues() {

		if (null != this.timer) {
			this.timer.setOnChronometerTickListener(this.robot);
		}

		if (null != this.teleopButton) {
			this.teleopButton.setOnCheckedChangeListener(this.robot);
		}

		if (null != this.beachExtendButton) {
			this.beachExtendButton.setOnCheckedChangeListener(this.robot
					.getBeachingSubSystem());
		}

		if (null != this.beachRetractButton) {
			this.beachRetractButton.setOnCheckedChangeListener(this.robot
					.getBeachingSubSystem());
		}

		if (null != this.gathererOnButton) {
			this.gathererOnButton.setOnCheckedChangeListener(this.robot
					.getGathererSubSystem());
		}

		if (null != this.shootButton) {
			this.shootButton.setOnClickListener(this.robot);
		}

		if (null != this.leftDrive) {
			this.leftDrive.setOnSeekBarChangeListener(this.robot
					.getDriveSubSystem());
		}

		if (null != this.rightDrive) {
			this.rightDrive.setOnSeekBarChangeListener(this.robot
					.getDriveSubSystem());
		}
	}

	private void initPreferences() {

		this.debug("onCreate()", "Called.");

		final SharedPreferences aSharedPreferences = this
				.getSharedPreferences();

		String aNetworktableName = aSharedPreferences.getString(
				TbsFrcRobotMainActivity.PREF_NAME_NETWORKTABLE_NAME, null);

		if (null == aNetworktableName) {

			final Editor anEditor = aSharedPreferences.edit();

			anEditor.putString(
					TbsFrcRobotMainActivity.PREF_NAME_NETWORKTABLE_NAME,
					TbsFrcRobotMainActivity.PREF_NAME_NETWORKTABLE_NAME_DEFAULT);

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_LEFT_DRIVE,
					TbsFrcRobotMainActivity.DRIVE_LEFT_SPEED);

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_RIGHT_DRIVE,
					TbsFrcRobotMainActivity.DRIVE_RIGHT_SPEED);

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_EXTEND_BEACH,
					"TBD");

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_RETRACT_BEACH,
					"TBD");

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_GATHERER,
					"TBD");

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_SHOOT,
					TbsFrcRobotMainActivity.BUTTON_FIRE);

			anEditor.putString(TbsFrcRobotMainActivity.PREF_NAME_TELEOP, "TBD");

			anEditor.commit(); // version 8 can't do apply().

		}

	}

	private SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

}