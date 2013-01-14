/* Copyright @ 2013 by Dave Truby. All rights reserved. */
package com.tbs.frc.robotsim;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * 
 */
public class TbsFrcHelpActivity extends TbsFrcActivityBase {

	public TbsFrcHelpActivity() {
		super();
	}

	@Override
	public void onCreate(final Bundle pSavedInstanceState) {

		try {

			super.onCreate(pSavedInstanceState);

			this.setContentView(R.layout.help);

			final TextView aHelpView = (TextView) this
					.findViewById(R.id.tbshelptv);

			if (null != aHelpView) {

				final StringBuffer aMsg = new StringBuffer();

				aMsg.append(this.getResources().getString(R.string.help_prefix));
				aMsg.append(this.getVersion());

				aHelpView.setText(aMsg.toString());

			}

			final WebView aWebView = (WebView) this
					.findViewById(R.id.tbshelpwv);

			if (null != aWebView) {

				aWebView.loadUrl("http://davetruby.com/android/TbsFrcRobotSimulator.html");

			}

		} catch (Exception anEx) {
			this.error("onCreate()",
					"Exception name=" + anEx.getClass().getName()
							+ ", message=" + anEx.getMessage() + ".", anEx);
		}
	}

	private String getVersion() {
		PackageInfo aPackageInfo = null;

		try {

			aPackageInfo = this.getPackageManager().getPackageInfo(
					this.getPackageName(), PackageManager.GET_CONFIGURATIONS);

		} catch (NameNotFoundException aNnfEx) {
			this.error("getVersion()", "NameNotFoundException message="
					+ aNnfEx.getMessage() + ".", aNnfEx);
		}

		return (null == aPackageInfo ? "" : aPackageInfo.versionName);
	}

}
