/* Copyright @ 201 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Context;
import android.widget.TextView;

import com.tbs.widgit.VerticalSeekBar;
import com.tbs.widgit.VerticalSeekBar.OnSeekBarChangeListener;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public class DriveSubSystem extends SubSystemBase implements
		OnSeekBarChangeListener {

	private static final String SUBSYSTEM_NAME = "DriveSystem";
	public static final String COMMAND_DRIVE = "DriveWithJoysticksCmd";
	private static final String DRIVE_LEFT_JOYX = "Drive Left Joystick X";
	private static final String DRIVE_RIGHT_JOYX = "Drive Right Joystick X";

	private static final double FACTOR = 100.000;

	private boolean running = false;
	private double leftSpeed = 0;
	private double rightSpeed = 0;
	private NetworkTable networkTable = null;
	private TextView leftDriveLabel = null;
	private TextView rightDriveLabel = null;
	private Scheduler scheduler = null;

	public DriveSubSystem(final Context pContext, final Scheduler pScheduler,
			final NetworkTable pNetworkTable, final TextView pLeftDriveLabel,
			final TextView pRightDriveLabel) {

		super(pContext);

		if (null == pScheduler) {
			throw new IllegalArgumentException("pScheduler is null");
		}
		if (null == pNetworkTable) {
			throw new IllegalArgumentException("pNetworkTable is null");
		}
		if (null == pLeftDriveLabel) {
			throw new IllegalArgumentException("pLeftDriveLabel is null");
		}
		if (null == pRightDriveLabel) {
			throw new IllegalArgumentException("pRightDriveLabel is null");
		}

		this.scheduler = pScheduler;
		this.networkTable = pNetworkTable;
		this.leftDriveLabel = pLeftDriveLabel;
		this.rightDriveLabel = pRightDriveLabel;
	}

	public void start(final boolean pStart) {

		if (pStart) {

			this.running = true;
			this.scheduler.addCommand(DriveSubSystem.COMMAND_DRIVE);

			ITable aTable = this.networkTable
					.getSubTable(DriveSubSystem.SUBSYSTEM_NAME);
			this.setupSubSystem(aTable, DriveSubSystem.COMMAND_DRIVE,
					DriveSubSystem.COMMAND_DRIVE);

			this.sendState();

		} else {

			this.running = true;
			this.sendState();
			this.running = false;
			this.scheduler.removeCommand(DriveSubSystem.COMMAND_DRIVE);

			ITable aTable = this.networkTable
					.getSubTable(DriveSubSystem.SUBSYSTEM_NAME);
			this.setupSubSystem(aTable, DriveSubSystem.COMMAND_DRIVE, null);

		}
	}

	public double powerDrain() {
		return ((this.leftSpeed == 0 && this.rightSpeed == 0) ? 1.0 : 1.2);
	}

	public void sendState() {
		if (this.running) {

			this.networkTable.putNumber(DriveSubSystem.DRIVE_LEFT_JOYX,
					this.leftSpeed);
			this.networkTable.putNumber(this.getLeftSpeedNetworkName(),
					this.leftSpeed);
			this.networkTable.putNumber(DriveSubSystem.DRIVE_RIGHT_JOYX,
					this.rightSpeed);
			this.networkTable.putNumber(this.getRightSpeedNetworkName(),
					this.rightSpeed);

		}
	}

	private String getLeftSpeedNetworkName() {
		return this.getSharedPreferences().getString(
				TbsFrcRobotMainActivity.PREF_NAME_LEFT_DRIVE,
				TbsFrcRobotMainActivity.DRIVE_LEFT_SPEED);
	}

	private String getRightSpeedNetworkName() {
		return this.getSharedPreferences().getString(
				TbsFrcRobotMainActivity.PREF_NAME_RIGHT_DRIVE,
				TbsFrcRobotMainActivity.DRIVE_RIGHT_SPEED);
	}

	private void setLeft(final int pSpeed) {
		this.leftSpeed = this.convertSpeed(pSpeed);
		this.leftDriveLabel.setText("Left\n" + Double.toString(this.leftSpeed));
		this.sendState();
	}

	private double convertSpeed(final int pSpeed) {
		return (pSpeed - DriveSubSystem.FACTOR) / DriveSubSystem.FACTOR;
	}

	private void setRight(final int pSpeed) {
		this.rightSpeed = this.convertSpeed(pSpeed);
		this.rightDriveLabel.setText("Right\n"
				+ Double.toString(this.rightSpeed));
		this.sendState();
	}

	@Override
	public void onProgressChanged(final VerticalSeekBar pSeekBar,
			final int pProgress, final boolean pFromUser) {

		try {

			if (this.running) {

				if (R.id.tbsleftdrive == pSeekBar.getId()) {

					this.setLeft(pProgress);

				} else if (R.id.tbsrightdrive == pSeekBar.getId()) {

					this.setRight(pProgress);

				}
			}

		} catch (Exception anEx) {
			this.error("onProgressChanged()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	@Override
	public void onStartTrackingTouch(final VerticalSeekBar pSeekBar) {
	}

	@Override
	public void onStopTrackingTouch(final VerticalSeekBar pSeekBar) {

		try {

			if (R.id.tbsleftdrive == pSeekBar.getId()) {

				pSeekBar.setProgress(100);
				this.setLeft(100);

			} else if (R.id.tbsrightdrive == pSeekBar.getId()) {

				pSeekBar.setProgress(100);
				this.setRight(100);

			}

		} catch (Exception anEx) {
			this.error("onStopTrackingTouch()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

}
