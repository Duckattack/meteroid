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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;

import de.chaosdorf.meteroid.longrunningio.LongRunningIOGet;
import de.chaosdorf.meteroid.longrunningio.LongRunningIOTask;
import de.chaosdorf.meteroid.model.BuyableItem;
import de.chaosdorf.meteroid.model.Money;
import de.chaosdorf.meteroid.util.Utility;

public class BuyDrink extends BookingActivity
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
        activity = this;
        setContentView(R.layout.activity_buy_drink);

		super.onCreate(savedInstanceState);

        final Button depositButton = (Button) findViewById(R.id.button_deposit_money);
        depositButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Utility.startActivity(activity, DepositMoney.class);
            }
        });


        final Button transferButton = (Button) findViewById(R.id.button_transfer_money);
        transferButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                FragmentManager fm = getSupportFragmentManager();
                DialogFragment dialogFragment = new MoneyDialogFragment(MoneyDialogType.TRANSFER);
                dialogFragment.show(fm, "Transfer money");
            }
        });

		new LongRunningIOGet(this, LongRunningIOTask.GET_DRINKS, hostname + "drinks.json").execute();
	}

    @Override
    protected void backButtonOnClick() {
        Utility.resetUsername(activity);
        Utility.startActivity(activity, PickUsername.class);
    }

    @Override
    public void onUserSelectAmount(double amount) {
        BuyableItem buyableItem = new Money(amount + " Euro", "euro_" + amount, amount);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putFloat("amount", (float) amount).apply();
        prefs.edit().putInt("src_user_id", user.getId()).apply();
        // doBooking(buyableItem);
        Utility.resetUsername(activity);
        Utility.startActivity(activity, PickUsernameTransfer.class);
    }
}
