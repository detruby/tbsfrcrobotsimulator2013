/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Context;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public class GathererSubSystem extends SubSystemBase implements
		OnCheckedChangeListener {

	private static final String SUBSYSTEM_NAME = "GathererSystem";
	private static final String GATHERER_ON_CMD = "GathererTurnOnCmd";
	private static final String GATHERER_OFF_CMD = "GathererTurnOffCmd";
	private static final String GATHERER_ON_BUTTON = "Gatherer Motor Turn On Button";
	private static final String GATHERER_BALL_PRESENT = "Gatherer Ball Present";
	private static final String GATHERER_ON = "Gatherer On";
	private static final String SHOOTER_BALL_PRESENT = "Shooter Ball Present";
	private static final int GATHERER_BALL_STOP_INTERVAL = 4000;
	private static final int SHOOTER_BALL_STOP_INTERVAL = 3000;

	private boolean gathererBallPresent = false;
	private boolean shooterBallPresent = false;
	private boolean gatherOn = false;
	private boolean gatherOff = false;
	private long stopTime = 0;
	private Scheduler scheduler = null;
	private NetworkTable networkTable = null;
	private Button fireButton = null;

	public GathererSubSystem(final Context pContext,
			final Scheduler pScheduler, final NetworkTable pNetworkTable,
			final Button pFireButton) {

		super(pContext);

		if (null == pScheduler) {
			throw new IllegalArgumentException("pScheduler is null");
		}
		if (null == pNetworkTable) {
			throw new IllegalArgumentException("pNetworkTable is null");
		}
		if (null == pFireButton) {
			throw new IllegalArgumentException("pFireButton is null");
		}

		this.scheduler = pScheduler;
		this.networkTable = pNetworkTable;
		this.fireButton = pFireButton;
	}

	public void start(final boolean pStart) {

		if (pStart) {
			this.fireButton.setEnabled(true);
		} else {
			this.fireButton.setEnabled(false);
		}

		this.gathererBallPresent = false;
		this.shooterBallPresent = false;
		this.gatherOn = false;
		this.gatherOff = false;
		this.stopTime = 0;

		ITable aGathererSubSystem = this.networkTable
				.getSubTable(GathererSubSystem.SUBSYSTEM_NAME);
		this.setupSubSystem(aGathererSubSystem, null, null);

		ITable aGathererOnButtonTable = this.networkTable
				.getSubTable(GathererSubSystem.GATHERER_ON_BUTTON);
		this.setupButton(aGathererOnButtonTable, false);

		this.sendState();
	}

	public double powerDrain() {
		return (this.gatherOn ? 1.1 : 1.0);
	}

	public void ballFired(final Context pContext) {
		this.shooterBallPresent = false;

		this.fireButton.setEnabled(false);

		this.stopTime = System.currentTimeMillis()
				+ GathererSubSystem.SHOOTER_BALL_STOP_INTERVAL;

		this.gatherOff = false;
		this.gatherOn = true;

		this.scheduler.removeCommand(GathererSubSystem.GATHERER_OFF_CMD);
		this.scheduler.addCommand(GathererSubSystem.GATHERER_ON_CMD);

		ITable aGathererSubSystemTable = this.networkTable
				.getSubTable(GathererSubSystem.SUBSYSTEM_NAME);
		this.setupSubSystem(aGathererSubSystemTable, null,
				GathererSubSystem.GATHERER_ON_CMD);

		this.sendState();

		Toast.makeText(pContext, "Gatherer started.", Toast.LENGTH_SHORT)
				.show();
	}

	public void sendState() {

		this.networkTable.putBoolean(GathererSubSystem.GATHERER_BALL_PRESENT,
				this.gathererBallPresent);
		this.networkTable.putBoolean(GathererSubSystem.SHOOTER_BALL_PRESENT,
				this.shooterBallPresent);
		this.networkTable.putBoolean(GathererSubSystem.GATHERER_ON,
				this.gatherOn);

	}

	@Override
	public void onCheckedChanged(final CompoundButton pButtonView,
			final boolean pIsChecked) {

		try {

			if (R.id.tbsgathererOn == pButtonView.getId()) {

				if (pIsChecked) {

					if (!this.gatherOn && !this.gatherOff) {

						ITable aGathererOnButtonTable = this.networkTable
								.getSubTable(GathererSubSystem.GATHERER_ON_BUTTON);
						this.setupButton(aGathererOnButtonTable, true);

						this.gathererBallPresent = false;
						this.shooterBallPresent = false;
						this.fireButton.setEnabled(false);
						this.gatherOn = true;
						this.stopTime = System.currentTimeMillis()
								+ GathererSubSystem.GATHERER_BALL_STOP_INTERVAL;

						this.scheduler
								.addCommand(GathererSubSystem.GATHERER_ON_CMD);

						ITable aGatherOnTable = this.networkTable
								.getSubTable(GathererSubSystem.SUBSYSTEM_NAME);
						this.setupSubSystem(aGatherOnTable, null,
								GathererSubSystem.GATHERER_ON_CMD);

						this.sendState();

						ITable aTable = this.networkTable
								.getSubTable(GathererSubSystem.GATHERER_ON_BUTTON);
						this.setupButton(aTable, false);

					}

				} else {

					if (this.gatherOn) {

						this.gatherOn = false;
						this.scheduler
								.removeCommand(GathererSubSystem.GATHERER_ON_CMD);

						ITable aTable = this.networkTable
								.getSubTable(GathererSubSystem.SUBSYSTEM_NAME);
						this.setupSubSystem(aTable, null, null);

					} else if (this.gatherOff) {

						this.gatherOff = false;
						this.scheduler
								.removeCommand(GathererSubSystem.GATHERER_OFF_CMD);

						ITable aTable = this.networkTable
								.getSubTable(GathererSubSystem.SUBSYSTEM_NAME);
						this.setupSubSystem(aTable, null, null);

					}

				}
			}

		} catch (Exception anEx) {
			this.error("onCheckedChanged()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	public void checkTime(final Context pContext) {

		if ((this.gatherOn || this.gatherOff) && (this.stopTime != 0)
				&& (System.currentTimeMillis() > this.stopTime)) {

			if (this.gatherOn && !this.shooterBallPresent
					&& !this.gathererBallPresent) {

				// no ball in shooter and no ball in gatherer
				// set ball in gatherer
				this.gathererBallPresent = true;
				this.stopTime = System.currentTimeMillis()
						+ GathererSubSystem.SHOOTER_BALL_STOP_INTERVAL;
				this.sendState();
				Toast.makeText(pContext, "Ball in gatherer.",
						Toast.LENGTH_SHORT).show();

			} else if (this.gatherOn && !this.shooterBallPresent
					&& this.gathererBallPresent) {

				// no ball in shooter and have ball in gatherer
				// set ball in shooter
				this.shooterBallPresent = true;
				this.fireButton.setEnabled(true);
				this.gathererBallPresent = false;
				this.stopTime = System.currentTimeMillis()
						+ GathererSubSystem.SHOOTER_BALL_STOP_INTERVAL;
				this.sendState();
				Toast.makeText(pContext, "Ball in shooter.", Toast.LENGTH_SHORT)
						.show();

			} else if (this.gatherOn && this.shooterBallPresent
					&& !this.gathererBallPresent) {

				// ball in shooter and no ball in gather
				// set ball in gather
				this.gathererBallPresent = true;
				this.gatherOn = false;
				this.gatherOff = true;
				this.stopTime = 0;
				this.scheduler.removeCommand(GathererSubSystem.GATHERER_ON_CMD);
				this.scheduler.addCommand(GathererSubSystem.GATHERER_OFF_CMD);

				ITable aTable = this.networkTable
						.getSubTable(GathererSubSystem.SUBSYSTEM_NAME);
				this.setupSubSystem(aTable, null,
						GathererSubSystem.GATHERER_OFF_CMD);

				this.sendState();
				Toast.makeText(pContext, "Gatherer pausing.",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

}
