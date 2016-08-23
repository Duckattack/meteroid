package de.chaosdorf.meteroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.EditText;

/**
 * Created by n-te on 29.03.15.
 */




public class MoneyDialogFragment extends DialogFragment {
    protected MoneyDialogType type;

    public MoneyDialogFragment(MoneyDialogType type) {
        super();

        this.type = type;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        int labelId, buttonLabelId;

        if (this.type == MoneyDialogType.DEPOSIT) {
            labelId = R.string.deposit_money_amount_label;
            buttonLabelId = R.string.button_deposit_money;
        }
        else {
            labelId = R.string.transfer_money_amount_label;
            buttonLabelId = R.string.button_transfer_money;
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_deposit_money, null));
        builder.setMessage(labelId)
                .setPositiveButton(buttonLabelId, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BookingActivity callingActivity = (BookingActivity) getActivity();
                        EditText input = (EditText) ((AlertDialog) dialog).findViewById(R.id.input_deposit_amount);
                        double amount = 0.0;
                        try {
                            amount = Double.parseDouble(input.getText().toString());
                        }
                        catch (NumberFormatException e) {
                            // Well yeah. This was not a valid amount
                            // TODO: Implement warning
                            amount = 0.0;
                        }
                        callingActivity.onUserSelectAmount(amount);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
