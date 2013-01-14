/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public class Scheduler extends SubSystemBase {

	private static final String SUBSYSTEM_NAME = "Scheduler";

	private List<String> commands = new ArrayList<String>();
	private NetworkTable networkTable;

	public Scheduler(final Context pContext, final NetworkTable pNetworkTable) {
		super(pContext);
		this.networkTable = pNetworkTable;
	}

	public void start(final boolean pStart) {
		if (pStart) {
			this.commands = new ArrayList<String>();
		} else {
			this.commands = new ArrayList<String>();
		}
		this.sendStatus();
	}

	public void addCommand(final String pCommand) {
		this.commands.add(pCommand);
		this.sendStatus();
	}

	public void removeCommand(final String pCommand) {
		this.commands.remove(pCommand);
		this.sendStatus();
	}

	public void sendStatus() {

		ITable aTable = this.networkTable.getSubTable(Scheduler.SUBSYSTEM_NAME);
		this.setupScheduler(aTable,
				this.commands.toArray(new String[this.commands.size()]));

	}

}
