/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.Context;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * 
 */
public class CameraSubSystem extends SubSystemBase {

	private static final String SUBSYSTEM_NAME = "CameraSystem";
	private static final String CAMERA_PAN_ANGLE = "Camera Pan Angle";
	private static final String CAMERA_TILT_ANGLE = "Camera Tilt Angle";

	private double pan = 89.0;
	private double tilt = 45.0;
	private NetworkTable networkTable = null;

	public CameraSubSystem(final Context pContext,
			final NetworkTable pNetworkTable) {

		super(pContext);

		if (null == pNetworkTable) {
			throw new IllegalArgumentException("pNetworkTable is null");
		}

		this.networkTable = pNetworkTable;
	}

	public void start(final boolean pStart) {

		ITable aTable = this.networkTable
				.getSubTable(CameraSubSystem.SUBSYSTEM_NAME);
		this.setupSubSystem(aTable, null, null);

		this.sendState();
	}

	public void sendState() {

		this.networkTable.putNumber(CameraSubSystem.CAMERA_PAN_ANGLE, this.pan);

		this.networkTable.putNumber(CameraSubSystem.CAMERA_TILT_ANGLE,
				this.tilt);

	}

}
