/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * 
 */
public class Robot implements OnChronometerTickListener,
		OnCheckedChangeListener, OnClickListener {

	private static final String BATTERY_VOLTS = "Battery Volts";
	private static final String INIT_AT = "Robot Initialized At";
	private static final String VERSION = "Robot Program Version";
	private static final String ROBOT_DISABLE_STATE = "Robot Disabled State";
	private static final String ROBOT_TELEOP_STATE = "Robot Teleop State";

	private String version = "1.0.1";
	private Date initAt = null;
	private boolean teleop = false;
	private double batteryVolts = 13.0;
	private NetworkTable networkTable = null;
	private Scheduler scheduler = null;
	private DriveSubSystem driveSubSystem = null;
	private BeachingSubSystem beachingSubSystem = null;
	private GathererSubSystem gathererSubSystem = null;
	private ShooterSubSystem shooterSubSystem = null;
	private CameraSubSystem cameraSubSystem = null;

	public Robot(final Context pContext, final NetworkTable pNetworkTable,
			final ToggleButton pExtendButton,
			final ToggleButton pRetractButton, final TextView pLeftDriveLabel,
			final TextView pRightDriveLabel, final Button pFireButton) {

		super();

		if (null == pNetworkTable) {
			throw new IllegalArgumentException("pNetworkTable is null");
		}

		try {

			this.networkTable = pNetworkTable;
			this.initAt = new Date();

			final SimpleDateFormat aFormat = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss");

			this.networkTable.putString(Robot.INIT_AT,
					aFormat.format(this.initAt));
			this.networkTable.putString(Robot.VERSION, this.version);

			this.scheduler = new Scheduler(pContext, this.networkTable);

			this.driveSubSystem = new DriveSubSystem(pContext, this.scheduler,
					this.networkTable, pLeftDriveLabel, pRightDriveLabel);

			this.beachingSubSystem = new BeachingSubSystem(pContext,
					this.scheduler, this.networkTable, pExtendButton,
					pRetractButton);

			this.gathererSubSystem = new GathererSubSystem(pContext,
					this.scheduler, this.networkTable, pFireButton);

			this.shooterSubSystem = new ShooterSubSystem(pContext,
					this.scheduler, this.networkTable);

			this.cameraSubSystem = new CameraSubSystem(pContext,
					this.networkTable);

			this.start(false);

		} catch (Exception anEx) {
			this.error("Robot()", "Exception name=" + anEx.getClass().getName()
					+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	private void start(final boolean pStart) {

		this.teleop = pStart;

		this.networkTable.putBoolean(Robot.ROBOT_DISABLE_STATE,
				Boolean.valueOf(!this.teleop));
		this.networkTable.putBoolean(Robot.ROBOT_TELEOP_STATE,
				Boolean.valueOf(this.teleop));
		this.networkTable.putNumber(Robot.BATTERY_VOLTS,
				Double.valueOf(this.batteryVolts));

		this.scheduler.start(pStart);
		this.driveSubSystem.start(pStart);
		this.beachingSubSystem.start(pStart);
		this.gathererSubSystem.start(pStart);
		this.shooterSubSystem.start(pStart);
		this.cameraSubSystem.start(pStart);
	}

	@Override
	public void onCheckedChanged(final CompoundButton pButtonView,
			final boolean pIsChecked) {

		try {

			if (R.id.tbsrobotteleop == pButtonView.getId()) {

				this.start(pIsChecked);

			}

		} catch (Exception anEx) {
			this.error("onCheckedChanged()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	@Override
	public void onClick(final View pView) {

		try {

			if (R.id.tbsshoot == pView.getId()) {

				this.shooterSubSystem.fireBall();
				this.gathererSubSystem.ballFired(pView.getContext());

			}

		} catch (Exception anEx) {
			this.error("onClick()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	@Override
	public void onChronometerTick(final Chronometer pChronometer) {

		try {

			// drain the battery
			double aChange = 0.001 * this.driveSubSystem.powerDrain()
					* this.beachingSubSystem.powerDrain()
					* this.gathererSubSystem.powerDrain()
					* this.shooterSubSystem.powerDrain();

			this.batteryVolts -= aChange;

			if (this.teleop) {
				this.beachingSubSystem.checkTime(pChronometer.getContext());
				this.gathererSubSystem.checkTime(pChronometer.getContext());
				this.shooterSubSystem.checkTime(pChronometer.getContext());
			}

			this.sendState();

		} catch (Exception anEx) {
			this.error("onChronometerTick()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	private void sendState() {

		this.networkTable.putNumber(Robot.BATTERY_VOLTS,
				Double.valueOf(this.batteryVolts));

		this.driveSubSystem.sendState();
		this.beachingSubSystem.sendState();
		this.gathererSubSystem.sendState();
		this.shooterSubSystem.sendState();
		this.cameraSubSystem.sendState();
	}

	public DriveSubSystem getDriveSubSystem() {
		return this.driveSubSystem;
	}

	public BeachingSubSystem getBeachingSubSystem() {
		return this.beachingSubSystem;
	}

	public GathererSubSystem getGathererSubSystem() {
		return this.gathererSubSystem;
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
