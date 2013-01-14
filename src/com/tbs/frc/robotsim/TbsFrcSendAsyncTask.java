/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

/**
 * NOT CURRENTLY USED
 */
public class TbsFrcSendAsyncTask extends AsyncTask<String, Integer, String> {

	private static final String FILENAME = "TbsFrcEvent.txt";
	private static final String TOKEN_NETWORKTABLE = "[NetworkTable]";

	private transient final Context context;
	private ProgressDialog progressDialog;

	protected TbsFrcSendAsyncTask(final Context pContext,
			final ProgressDialog pProgressDialog) {
		super();

		if (null == pContext) {
			throw new IllegalArgumentException("pContext is null");
		}

		this.context = pContext;
		this.progressDialog = pProgressDialog;
	}

	@Override
	protected String doInBackground(final String... pArgs) {
		String aMessage = "";

		try {

			this.debug("doInBackground()", "Started.");

			aMessage = this.sendFile();

			this.debug("doInBackground()", "Ended.");

		} catch (Exception anEx) {
			this.error("doInBackground()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}

		return aMessage;
	}

	protected void onProgressUpdate(final Integer p) {
		if (null != this.progressDialog) {
			this.progressDialog.incrementProgressBy(1);
		}
	}

	@Override
	protected void onPostExecute(final String pMessage) {
		if (null != this.progressDialog) {
			this.progressDialog.dismiss();
		}
		Toast.makeText(this.context, pMessage, Toast.LENGTH_LONG).show();
	}

	private String sendFile() {

		String aMessage = "Nothing to send.";
		BufferedReader aReader = null;
		String aLine = null;

		try {

			final File aFilePath = this.context
					.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

			final File anIndexFile = new File(aFilePath, "index.txt");

			if (!aFilePath.exists()) {

				if (anIndexFile.createNewFile()) {

					MediaScannerConnection
							.scanFile(
									this.context,
									new String[] { aFilePath.getAbsolutePath() },
									null,
									new MediaScannerConnection.OnScanCompletedListener() {

										@Override
										public void onScanCompleted(
												final String pPath,
												final Uri pUri) {
											TbsFrcSendAsyncTask.this.debug(
													"onScanCompleted()",
													"Dir pPath=" + pPath
															+ ", pUri=" + pUri
															+ ".");
										}
									});
					MediaScannerConnection
							.scanFile(
									this.context,
									new String[] { anIndexFile
											.getAbsolutePath() },
									null,
									new MediaScannerConnection.OnScanCompletedListener() {

										@Override
										public void onScanCompleted(
												final String pPath,
												final Uri pUri) {
											TbsFrcSendAsyncTask.this.debug(
													"onScanCompleted()",
													"Dir pPath=" + pPath
															+ ", pUri=" + pUri
															+ ".");
										}
									});
				}

			} else if (!anIndexFile.exists()) {

				if (anIndexFile.createNewFile()) {

					MediaScannerConnection
							.scanFile(
									this.context,
									new String[] { anIndexFile
											.getAbsolutePath() },
									null,
									new MediaScannerConnection.OnScanCompletedListener() {

										@Override
										public void onScanCompleted(
												final String pPath,
												final Uri pUri) {
											TbsFrcSendAsyncTask.this.debug(
													"onScanCompleted()",
													"Dir pPath=" + pPath
															+ ", pUri=" + pUri
															+ ".");
										}
									});
				}

			}

			final File aFile = new File(aFilePath, TbsFrcSendAsyncTask.FILENAME);

			if ((null != aFile) && (aFile.exists())) {

				this.debug("sendFile()", "File=" + aFilePath + "/"
						+ TbsFrcSendAsyncTask.FILENAME + ".");

				aReader = new BufferedReader(new FileReader(aFile));

				String[] aLineArray = null;

				while ((aLine = aReader.readLine()) != null) {

					this.debug("sendFile()", "aLine=" + aLine + ".");

					aLineArray = aLine.split("[,]", 2);

					if ((null != aLineArray) && (aLineArray.length > 1)) {

						if (!"Service started.".equalsIgnoreCase(aLineArray[1])) {

							this.parseDateValue(aLineArray[0], aLineArray[1]);

						}
					}

				}

				aMessage = "Sent items.";

			}

		} catch (IOException anIoEx) {

			this.error("sendFile()",
					"IOException message=" + anIoEx.getMessage() + ".", anIoEx);

		} catch (Exception anEx) {

			this.error("sendFile()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);

		} finally {
			if (null != aReader) {
				try {
					aReader.close();
				} catch (IOException e) {
					// nothing
				}
			}
		}

		return aMessage;
	}

	private void parseDateValue(final String pDate, final String pValue) {

		String[] aNameValueArray = pValue.split("[,]");

		if ((null != aNameValueArray) && (aNameValueArray.length > 1)) {

			String aName = aNameValueArray[0];
			Object aValue = this.parseValue(aNameValueArray[1]);

			int aNetworkTablePos = aName
					.indexOf(TbsFrcSendAsyncTask.TOKEN_NETWORKTABLE);

			if (aNetworkTablePos > -1) {

				String aNextName = aName.substring(0, aNetworkTablePos
						+ TbsFrcSendAsyncTask.TOKEN_NETWORKTABLE.length());

				String aNextPart = aName.substring(aNetworkTablePos
						+ TbsFrcSendAsyncTask.TOKEN_NETWORKTABLE.length() + 1);

				this.debug("parseDateValue()", "aNextName=" + aNextName
						+ ", aNextPart=" + aNextPart + ".");

				this.parseNetworkTable(NetworkTable.getTable(aNextName),
						aNextPart, aValue);

			} else {

				this.sendNameValue(aName, aValue);

			}
		}
	}

	private void parseNetworkTable(final ITable pNetworkTable,
			final String pNextPart, final Object aValue) {

		String aNextName = null;
		ITable aNetworkTable = null;

		int aNetworkTablePos = pNextPart
				.indexOf(TbsFrcSendAsyncTask.TOKEN_NETWORKTABLE);

		if (aNetworkTablePos > -1) {

			aNextName = pNextPart.substring(0, aNetworkTablePos
					+ TbsFrcSendAsyncTask.TOKEN_NETWORKTABLE.length());

			String aNextPart = pNextPart.substring(aNetworkTablePos
					+ TbsFrcSendAsyncTask.TOKEN_NETWORKTABLE.length() + 1);

			aNetworkTable = pNetworkTable.getSubTable(aNextName); // NetworkTable.getTable(aNextName);

			aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_DATA,
					aNextName);

			if (SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_SCHEDULER
					.equalsIgnoreCase(aNextName)) {

				aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
						SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_SCHEDULER);

			} else if (aNextName.contains("Button")) {

				aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
						SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_BUTTON);

			} else if (aNextName.contains("System")) {

				aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
						SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_SUBSYSTEM);

			} else {

				aNetworkTable.putString(SubSystemBase.NETWORKTABLE_KEY_TYPE,
						SubSystemBase.NETWORKTABLE_NAME_TYPE_VALUE_COMMAND);

			}

			this.parseNetworkTable(aNetworkTable, aNextPart, aValue);

			// pNetworkTable.putSubTable(aNextName, aNetworkTable);

		} else {

			this.debug("parseNetworkTable()", "Putting aName=" + pNextPart
					+ ", aValue=" + aValue + ".");

			if (aValue instanceof Boolean) {

				pNetworkTable.putBoolean(pNextPart, (Boolean) aValue);

			} else if (aValue instanceof Double) {

				pNetworkTable.putNumber(pNextPart, (Double) aValue);

			} else if (aValue instanceof Integer) {

				pNetworkTable.putNumber(pNextPart, (Integer) aValue);

			} else {

				pNetworkTable.putString(pNextPart, aValue.toString());

			}

		}

	}

	private void sendNameValue(final String aName, final Object aValue) {

		if ((null != aName) && (null != aValue)) {

			NetworkTable aNetworkTable = NetworkTable
					.getTable(TbsFrcRobotMainActivity.PREF_NAME_NETWORKTABLE_NAME_DEFAULT);

			if (null != aNetworkTable) {

				if (aValue instanceof Boolean) {

					this.debug("sendNameValue()", "Putting boolean aName="
							+ aName + ", aValue=" + aValue.toString() + ".");
					aNetworkTable.putBoolean(aName, (Boolean) aValue);

				} else if (aValue instanceof Double) {

					this.debug("sendNameValue()", "Putting double aName="
							+ aName + ", aValue=" + aValue.toString() + ".");
					aNetworkTable.putNumber(aName, (Double) aValue);

				} else if (aValue instanceof Integer) {

					this.debug("sendNameValue()", "Putting integer aName="
							+ aName + ", aValue=" + aValue.toString() + ".");
					aNetworkTable.putNumber(aName, (Integer) aValue);

				} else {

					this.debug("sendNameValue()", "Putting string aName="
							+ aName + ", aValue=" + aValue + ".");
					aNetworkTable.putString(aName, aValue.toString());

				}

			}
		}
	}

	private String parseDataType(final String pDataType) {
		String aDataType = null;

		// input " (java.lang.Boolean)"

		if (null != pDataType) {

			String aTrimedDataType = pDataType.trim();

			int aPos = aTrimedDataType.indexOf('(');

			if (aPos > -1) {

				aDataType = aTrimedDataType.substring(aPos + 1,
						aTrimedDataType.length() - 1);

			}
		}

		return aDataType;
	}

	private Object parseValue(final String pDataTypeValue) {
		Object aValue = null;

		// input " (java.lang.Boolean)=false"

		if ((null != pDataTypeValue) && (pDataTypeValue.length() > 0)) {

			String[] aDataTypeValueArray = pDataTypeValue.split("[=]");

			if ((null != aDataTypeValueArray)
					&& (aDataTypeValueArray.length > 1)) {

				String aDataType = this.parseDataType(aDataTypeValueArray[0]);

				if (null != aDataType) {

					// this.debug("parseValue()", "aDataType=" + aDataType +
					// ", value="
					// + aDataTypeValueArray[1] + ".");

					if ("java.lang.Boolean".equals(aDataType)) {

						aValue = Boolean.parseBoolean(aDataTypeValueArray[1]);

					} else if ("java.lang.Double".equals(aDataType)) {

						try {

							aValue = Double.parseDouble(aDataTypeValueArray[1]);

						} catch (NumberFormatException NfEx) {
							this.error("parseValue()", "Value="
									+ aDataTypeValueArray[1]
									+ ", is not an double.", null);
						}

					} else if ("java.lang.Integer".equals(aDataType)) {
						try {

							aValue = Integer.parseInt(aDataTypeValueArray[1]);

						} catch (NumberFormatException NfEx) {
							this.error("parseValue()", "Value="
									+ aDataTypeValueArray[1]
									+ ", is not an integer.", null);
						}

					} else if ("java.lang.String".equals(aDataType)) {

						aValue = aDataTypeValueArray[1];

					} else {
						this.error("parseValue()", "aDataType '" + aDataType
								+ "' is not expected.", null);
					}
				}
			}
		}

		return aValue;
	}

	protected boolean isNullOrBlank(final String pValue) {
		return ((null == pValue) || (pValue.length() == 0));
	}

	protected boolean isNotNullAndNotBlank(final String pValue) {
		return ((null != pValue) && (pValue.length() > 0));
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
		final String aClassName = this.getClass().getName();
		return aClassName.substring(aClassName.lastIndexOf('.') + 1);
	}

}
