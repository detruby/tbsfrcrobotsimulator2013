/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public class BeachingSubSystem extends SubSystemBase implements
		OnCheckedChangeListener {

	private static final String SUBSYSTEM_NAME = "BeachingSystem";
	private static final String BUTTON_EXTEND = "Beaching Motor Start Extend Button";
	private static final String BUTTON_RETRACT = "Beaching Motor Start Retract Button";
	private static final String BEACH_EXTENDED = "Beach Extended";
	private static final String BEACH_RETRACTED = "Beach Retracted";
	private static final String BEACH_EXTENDING = "Beach Extending";
	private static final String BEACH_RETRACTING = "Beach Retracting";
	private static final String CMD_BEACHING_EXTEND = "BeachingExtendCmd";
	private static final String CMD_BEACHING_RETRACT = "BeachingRetractCmd";
	private static final int STOP_INTERVAL = 8000;

	private boolean retractedSwitch = true;
	private boolean extendedSwitch = false;
	private boolean extending = false;
	private boolean retracting = false;
	// internal timer when to stop after started to extend|retract
	private long stopTime = 0;
	private Scheduler scheduler = null;
	private NetworkTable networkTable = null;
	private CompoundButton extendButton = null;
	private CompoundButton retractButton = null;

	public BeachingSubSystem(final Context pContext,
			final Scheduler pScheduler, final NetworkTable pNetworkTable,
			final CompoundButton pExtendButton,
			final CompoundButton pRetractButton) {

		super(pContext);

		if (null == pScheduler) {
			throw new IllegalArgumentException("pScheduler is null");
		}
		if (null == pNetworkTable) {
			throw new IllegalArgumentException("pNetworkTable is null");
		}
		if (null == pExtendButton) {
			throw new IllegalArgumentException("pExtendButton is null");
		}
		if (null == pRetractButton) {
			throw new IllegalArgumentException("pRetractButton is null");
		}

		this.networkTable = pNetworkTable;
		this.scheduler = pScheduler;
		this.extendButton = pExtendButton;
		this.retractButton = pRetractButton;
	}

	public void start(final boolean pStart) {

		this.retractedSwitch = true;
		this.extendedSwitch = false;
		this.extending = false;
		this.retracting = false;
		this.stopTime = 0;

		ITable aSubSystemTable = this.networkTable
				.getSubTable(BeachingSubSystem.SUBSYSTEM_NAME);
		this.setupSubSystem(aSubSystemTable, null, null);

		ITable anExtendButtonTable = this.networkTable
				.getSubTable(BeachingSubSystem.BUTTON_EXTEND);
		this.setupButton(anExtendButtonTable, false);

		ITable aRetractButtonTable = this.networkTable
				.getSubTable(BeachingSubSystem.BUTTON_RETRACT);
		this.setupButton(aRetractButtonTable, false);

		this.sendState();
	}

	public double powerDrain() {
		return ((this.extending || this.retracting) ? 1.1 : 1.0);
	}

	public void sendState() {

		this.networkTable.putBoolean(BeachingSubSystem.BEACH_EXTENDED,
				this.extendedSwitch);
		this.networkTable.putBoolean(BeachingSubSystem.BEACH_RETRACTED,
				this.retractedSwitch);
		this.networkTable.putBoolean(BeachingSubSystem.BEACH_EXTENDING,
				this.extending);
		this.networkTable.putBoolean(BeachingSubSystem.BEACH_RETRACTING,
				this.retracting);

	}

	@Override
	public void onCheckedChanged(final CompoundButton pButtonView,
			final boolean pIsChecked) {

		try {

			if (pIsChecked && (R.id.tbsbeachExtend == pButtonView.getId())) {

				final ITable aExtendButtonTable = this.networkTable
						.getSubTable(BeachingSubSystem.BUTTON_EXTEND);
				this.setupButton(aExtendButtonTable, true);

				this.retracting = false;
				this.extendedSwitch = false;
				this.extending = true;
				this.retracting = false;
				this.stopTime = System.currentTimeMillis()
						+ BeachingSubSystem.STOP_INTERVAL;

				this.scheduler
						.addCommand(BeachingSubSystem.CMD_BEACHING_EXTEND);

				ITable aBeachingSubSystemTable = this.networkTable
						.getSubTable(BeachingSubSystem.SUBSYSTEM_NAME);
				this.setupSubSystem(aBeachingSubSystemTable, null,
						BeachingSubSystem.CMD_BEACHING_EXTEND);

				this.sendState();

				final ITable aRetractButtonTable = this.networkTable
						.getSubTable(BeachingSubSystem.BUTTON_EXTEND);
				this.setupButton(aRetractButtonTable, false);

			} else if (pIsChecked
					&& (R.id.tbsbeachRetract == pButtonView.getId())) {

				ITable aExtendButtonTable = this.networkTable
						.getSubTable(BeachingSubSystem.BUTTON_RETRACT);
				this.setupButton(aExtendButtonTable, true);

				this.retracting = true;
				this.extendedSwitch = false;
				this.extending = false;
				this.retractedSwitch = false;
				this.stopTime = System.currentTimeMillis()
						+ BeachingSubSystem.STOP_INTERVAL;

				this.scheduler
						.addCommand(BeachingSubSystem.CMD_BEACHING_RETRACT);

				ITable aBeachingSubSystemTable = this.networkTable
						.getSubTable(BeachingSubSystem.SUBSYSTEM_NAME);
				this.setupSubSystem(aBeachingSubSystemTable, null,
						BeachingSubSystem.CMD_BEACHING_RETRACT);

				this.sendState();

				ITable aRetractButtonTable = this.networkTable
						.getSubTable(BeachingSubSystem.BUTTON_RETRACT);
				this.setupButton(aRetractButtonTable, false);

			}

		} catch (Exception anEx) {
			this.error("onCheckedChanged()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	public void checkTime(final Context pContext) {
		if ((this.stopTime != 0)
				&& (System.currentTimeMillis() > this.stopTime)) {

			this.stopTime = 0;

			if (this.extending) {

				this.extendedSwitch = true;
				this.extending = false;
				this.extendButton.setChecked(false);
				this.scheduler
						.removeCommand(BeachingSubSystem.CMD_BEACHING_EXTEND);

				ITable aBeachingSubSystemTable = this.networkTable
						.getSubTable(BeachingSubSystem.SUBSYSTEM_NAME);
				this.setupSubSystem(aBeachingSubSystemTable, null, null);

				Toast.makeText(pContext, "Beach Extended.", Toast.LENGTH_SHORT)
						.show();

			} else if (this.retracting) {

				this.retracting = false;
				this.retractedSwitch = true;
				this.retractButton.setChecked(false);
				this.scheduler
						.removeCommand(BeachingSubSystem.CMD_BEACHING_RETRACT);

				ITable aTable = this.networkTable
						.getSubTable(BeachingSubSystem.SUBSYSTEM_NAME);
				this.setupSubSystem(aTable, null, null);

				Toast.makeText(pContext, "Beach Retracted.", Toast.LENGTH_SHORT)
						.show();

			}

			this.sendState();
		}
	}

}
