/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Chaosdorf e.V.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

package de.chaosdorf.meteroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import de.chaosdorf.meteroid.controller.UserController;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOCallback;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOPost;

import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.User;
import de.chaosdorf.meteroid.util.Utility;

public class PickUsernameTransfer extends Activity implements LongRunningIOCallback, AdapterView.OnItemClickListener
{
	private static final int NEW_USER_ID = -1;

	private Activity activity = null;
	private GridView gridView = null;

	private SharedPreferences prefs = null;

	private String hostname = null;
	private boolean multiUserMode = false;
	private boolean editHostnameOnBackButton = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		activity = this;
		setContentView(R.layout.activity_pick_username);

		gridView = (GridView) findViewById(R.id.grid_view);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		hostname = prefs.getString("hostname", null);
		multiUserMode = prefs.getBoolean("multi_user_mode", false);

		final ImageButton backButton = (ImageButton) findViewById(R.id.button_back);
		backButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, SetHostname.class);
			}
		});

		final ImageButton reloadButton = (ImageButton) findViewById(R.id.button_reload);
		reloadButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				Utility.startActivity(activity, PickUsernameTransfer.class);
			}
		});

		new LongRunningIOGet(this, LongRunningIOTask.GET_USERS, hostname + "users.json").execute();
	}

	@Override
	public void onDestroy()
	{
		if (gridView != null)
		{
			gridView.setAdapter(null);
		}
		super.onDestroy();
	}

	@Override
	public void displayErrorMessage(final LongRunningIOTask task, final String message)
	{
		runOnUiThread(new Runnable() {
			public void run() {
				Utility.displayToastMessage(activity, message);
				final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.pick_username_error);
				linearLayout.setVisibility(View.VISIBLE);
				gridView.setVisibility(View.GONE);
				editHostnameOnBackButton = true;
			}
		});
	}

	@Override
	public void processIOResult(final LongRunningIOTask task, final String json)
	{
		if (task == LongRunningIOTask.GET_USERS && json != null)
		{
			final List<User> itemList = UserController.parseAllUsersFromJSON(json);
			final UserAdapter userAdapter = new UserAdapter(itemList);

			gridView.setAdapter(userAdapter);
			gridView.setOnItemClickListener(this);
		}
		if (task == LongRunningIOTask.MAKE_TRANSFER) {
			// Check if transfer was successful
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			float amount = prefs.getFloat("amount",0);
			int srcUserId = prefs.getInt("src_user_id",0);

			// Inform user of successful transfer
			Utility.displayToastMessage(activity,
					String.format(
							getResources().getString(R.string.transfer_success),
							amount
					)
			);

			// Go back to user
			prefs.edit().putInt("userid", srcUserId).apply();
			Utility.startActivity(activity, BuyDrink.class);
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l)
	{

		final User dstUser = (User) gridView.getItemAtPosition(index);
		if (dstUser != null && dstUser.getName() != null)
		{
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			float amount = prefs.getFloat("amount",0);
			int srcUserId = prefs.getInt("src_user_id",0);

			// only transfer absolute values. Don't steal money.
			amount = Math.abs(amount);

			// Make POST payload
			final List<BasicNameValuePair> payload = new ArrayList<BasicNameValuePair>();
			payload.add(new BasicNameValuePair("dst_user_id", String.valueOf(dstUser.getId())));
			payload.add(new BasicNameValuePair("src_user_id", String.valueOf(srcUserId)));
			payload.add(new BasicNameValuePair("amount", String.valueOf(amount)));

			new LongRunningIOPost(
					this, LongRunningIOTask.MAKE_TRANSFER, hostname + "transfers.json",
					payload
			).execute();
		}
	}


	public class UserAdapter extends ArrayAdapter<User>
	{
		private final List<User> userList;
		private final LayoutInflater inflater;

		UserAdapter(final List<User> userList)
		{
			super(activity, R.layout.activity_pick_username, userList);
			this.userList = userList;
			this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, final View convertView, final ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.activity_pick_username_item, parent, false);
			}
			if (view == null)
			{
				return null;
			}

			final User user = userList.get(position);
			final ImageView icon = (ImageView) view.findViewById(R.id.icon);
			final TextView label = (TextView) view.findViewById(R.id.label);

			Utility.loadGravatarImage(activity, icon, user);
			icon.setContentDescription(user.getName());
			label.setText(user.getName());

			if (user.getId() == NEW_USER_ID)
			{
				icon.setImageDrawable(getResources().getDrawable(R.drawable.add_user));
			}

			return view;
		}
	}
}
