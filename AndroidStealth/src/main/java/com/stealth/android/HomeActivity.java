package com.stealth.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ProgressBar;
import com.stealth.files.FileIndex;
import com.stealth.visibility.VisibilityManager;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import content.ContentFragment;
import pin.PinManager;
import sharing.APSharing.APSharing;
import sharing.SharingUtils;

public class HomeActivity extends ActionBarActivity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavDrawer;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	private APSharing mSharing;
	private int mActiveNavigationOption = 0;
	private ProgressDialog mProgress = null;

	/**
	 * Launch the HomeActivity by providing a pin
	 *
	 * @param context the context to use for the launch
	 * @param pin     the actual pin code that is used to launch us
	 * @return whether activity could launch
	 */
	public static boolean launch(Context context, String pin) {
		if (!PinManager.get().isPin(pin)) {
			return false;
		}

		VisibilityManager.showApplication(context);

		try {
			Intent stealthCall = new Intent(context, HomeActivity.class);
			stealthCall.addCategory(Intent.CATEGORY_LAUNCHER);
			stealthCall.putExtra(PinManager.EXTRA_PIN, pin.trim());
			stealthCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(stealthCall);

			return true;
		}
		catch (Exception e) {
			VisibilityManager.hideApplication(context);
			Utils.d("Could not launch stealth app");
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_loading);

		String pin = getIntent().getStringExtra(PinManager.EXTRA_PIN);
		BootManager.Boot(this, pin, new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean succeeded) {

				// TODO: will be moved soon? should be in onStop/onStart?
				VisibilityManager.hideApplication(HomeActivity.this);
				if (succeeded) {
					Utils.toast(R.string.pin_description_unlocked); // welcome, Mr. Bond
					ConstructInterface(); // yay, we booted
				} else {
					finish(); // something went wrong. Incorrect pin maybe.
				}
			}
		});
	}

	/**
	 * Initializes the interface. Happens when booting is done.
	 */
	private void ConstructInterface() {
		setContentView(R.layout.activity_home);
		mNavDrawer = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mNavDrawer.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		mTitle = getTitle();

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.replace(R.id.container, new ContentFragment())
				.commit();
	}

	/**
	 * This method is meant to fill the content fragment based on the navigation drawer's selected page
	 *
	 * @param position the item that is now active
	 */
	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Utils.setContext(this); // onCreate is called later... so let's call this now :)
		mActiveNavigationOption = position;

		// The old pure sequence
//		if (BuildConfig.DEBUG || PinManager.get().isPin(pin) || pin == null) { // STEP 1
//			if (PinManager.get().isPin(pin)) Utils.d("Pin was correct"); // it was indeed the pin
//			FileIndex.create(false, new IOnResult<FileIndex>() { // STEP 2
//				@Override
//				public void onResult(FileIndex result) {
//					if (result != null) { // STEP 3
//						Utils.d("Created file index");
//						BootSucceeded(callback);
//					} else { // STEP 2
//						Utils.d("Failed to create file index");
//						callback.onResult(false);
//					}
//				}
//			});
//		} else { // STEP 1
//			Utils.d("Incorrect pin");
//			callback.onResult(false);
//		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mNavDrawer != null && !mNavDrawer.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.home, menu);

			checkHotspotAvailability(menu);

			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Check if the device has AP Wifi support. If not, disable the 'Share Application' menu entry.
	 *
	 * @param menu that contains the 'Share Application' menu entry.
	 */
	private void checkHotspotAvailability(Menu menu) {
		MenuItem appSharingItem = menu.findItem(R.id.app_sharing);

		if (!SharingUtils.hasAPWifiSupport(this)) {
			appSharingItem.setEnabled(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.app_sharing:
				mSharing.shareApk();
				return true;
			case R.id.action_settings:
				Intent settingsIntent = new Intent(this, StealthSettingActivity.class);
				startActivity(settingsIntent);
				return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
