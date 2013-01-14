/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Context;
import android.widget.Toast;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public class ShooterSubSystem extends SubSystemBase {

	private static final String SUBSYSTEM_NAME = "ShooterSystem";
	private static final String CMD_SHOOT_BALL = "ShootBallCmd";
	private static final String SHOOTER_SPEED = "Shooter Speed";
	private static final String SHOOT_DESIRED_SPEED = "Shooter Desired Speed";
	private static final String SHOOTER_PLUNGER_READY = "Shooter Plunger Ready";
	private static final String SHOOTER_PLUNGER_SWITCH = "Shooter Plunger Switch";
	private static final String SHOOTER_TOTAL_FIRES = "Shooter Total Fires";
	private static final int SHOOT_BALL_INTERVAL = 1500;

	private Scheduler scheduler = null;
	private double desiredSpeed = 0.8;
	private double speed = 0;
	private int fires = 0;
	private boolean plungerRotating = false;
	private boolean plungerReady = true;
	private boolean plungerSwitch = false;
	private long stopTime = 0;
	private NetworkTable networkTable = null;

	public ShooterSubSystem(final Context pContext, final Scheduler pScheduler,
			final NetworkTable pNetworkTable) {

		super(pContext);

		if (null == pScheduler) {
			throw new IllegalArgumentException("pScheduler is null");
		}
		if (null == pNetworkTable) {
			throw new IllegalArgumentException("pNetworkTable is null");
		}

		this.scheduler = pScheduler;
		this.networkTable = pNetworkTable;
	}

	public void start(final boolean pStart) {
		if (pStart) {
			this.fires = 0;
		}
		this.speed = 0;
		this.plungerRotating = false;
		this.plungerReady = true;
		this.plungerSwitch = false;
		this.stopTime = 0;

		ITable aSubSystemTable = this.networkTable
				.getSubTable(ShooterSubSystem.SUBSYSTEM_NAME);
		this.setupSubSystem(aSubSystemTable, null, null);

		ITable aFireButtonTable = this.networkTable.getSubTable(this
				.getFireButtonNetworktableName());
		this.setupButton(aFireButtonTable, false);

		this.sendState();
	}

	public void fireBall() {

		ITable aFireButtonTable = this.networkTable.getSubTable(this
				.getFireButtonNetworktableName());
		this.setupButton(aFireButtonTable, true);

		this.stopTime = System.currentTimeMillis()
				+ ShooterSubSystem.SHOOT_BALL_INTERVAL;
		this.plungerRotating = true;
		this.plungerReady = false;
		this.speed = this.desiredSpeed;
		this.fires++;

		this.scheduler.addCommand(ShooterSubSystem.CMD_SHOOT_BALL);

		ITable aSubSystemTable = this.networkTable
				.getSubTable(ShooterSubSystem.SUBSYSTEM_NAME);
		this.setupSubSystem(aSubSystemTable, null,
				ShooterSubSystem.CMD_SHOOT_BALL);

		this.sendState();

		ITable aFireBButtonTable = this.networkTable.getSubTable(this
				.getFireButtonNetworktableName());
		this.setupButton(aFireBButtonTable, false);

	}

	private String getFireButtonNetworktableName() {
		return this.getSharedPreferences().getString(
				TbsFrcRobotMainActivity.PREF_NAME_SHOOT,
				TbsFrcRobotMainActivity.BUTTON_FIRE);
	}

	public void checkTime(final Context pContext) {

		// this.debug("checkTime()", "this.stopTime=" + this.stopTime + ".");

		if ((this.stopTime != 0)
				&& (System.currentTimeMillis() > this.stopTime)) {

			this.debug("checkTime()", "Ball shot.");

			this.stopTime = 0;
			this.plungerRotating = false;
			this.plungerReady = true;
			this.speed = 0;

			this.scheduler.removeCommand(ShooterSubSystem.CMD_SHOOT_BALL);

			ITable aTable = this.networkTable
					.getSubTable(ShooterSubSystem.SUBSYSTEM_NAME);
			this.setupSubSystem(aTable, null, null);

			this.sendState();

			Toast.makeText(pContext, "Ball Gone.", Toast.LENGTH_SHORT).show();

		}

	}

	public double powerDrain() {
		return ((this.speed == 0 && !this.plungerRotating) ? 1.0 : 1.1);
	}

	public void sendState() {

		this.networkTable.putNumber(ShooterSubSystem.SHOOT_DESIRED_SPEED,
				this.desiredSpeed);

		this.networkTable.putNumber(ShooterSubSystem.SHOOTER_SPEED, this.speed);
		this.networkTable.putBoolean(ShooterSubSystem.SHOOTER_PLUNGER_READY,
				this.plungerReady);
		this.networkTable.putBoolean(ShooterSubSystem.SHOOTER_PLUNGER_SWITCH,
				this.plungerSwitch);

		this.networkTable.putNumber(ShooterSubSystem.SHOOTER_TOTAL_FIRES,
				this.fires);

	}

}
