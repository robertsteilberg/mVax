/*
Copyright (C) 2018 Duke University

This file is part of mVax.

mVax is free software: you can redistribute it and/or
modify it under the terms of the GNU Affero General Public License
as published by the Free Software Foundation, either version 3,
or (at your option) any later version.

mVax is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU General Public
License along with mVax; see the file LICENSE. If not, see
<http://www.gnu.org/licenses/>.
*/
package mhealth.mvax.auth.modals;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import mhealth.mvax.R;
import mhealth.mvax.auth.utilities.AuthInputValidator;

/**
 * @author Robert Steilberg
 * <p>
 * Modal and functionality for resetting an mVax user's password
 */
public class PasswordResetModal extends CustomModal {

    private AlertDialog mBuilder;

    private ProgressBar mSpinner;
    private List<View> mViews;

    public PasswordResetModal(View view) {
        super(view);
        mViews = new ArrayList<>();
    }

    @Override
    AlertDialog createDialog() {
        mBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.modal_reset_title))
                .setView(getActivity().getLayoutInflater().inflate(R.layout.modal_forgot_password, (ViewGroup) getView().getParent(), false))
                .setPositiveButton(getString(R.string.button_reset_password_submit), null)
                .setNegativeButton(getString(R.string.button_reset_password_cancel), null)
                .create();

        mBuilder.setOnShowListener(dialogInterface -> {

            mSpinner = mBuilder.findViewById(R.id.reset_spinner);

            mViews.add(mBuilder.findViewById(R.id.reset_fields));
            mViews.add(mBuilder.getButton(AlertDialog.BUTTON_NEGATIVE));

            final TextView emailTextView = mBuilder.findViewById(R.id.textview_email_reset);
            emailTextView.setOnEditorActionListener((v, actionId, event) -> {
                if (event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // enter on hardware keyboard submits reset request
                    attemptPasswordReset(emailTextView);
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // "Done" button submits reset request
                    attemptPasswordReset(emailTextView);
                    return true;
                }
                return false;
            });

            final Button positiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> attemptPasswordReset(emailTextView));
            mViews.add(positiveButton);
        });
        return mBuilder;
    }

    private void attemptPasswordReset(final TextView emailTextView) {
        final String emailAddress = emailTextView.getText().toString();
        if (TextUtils.isEmpty(emailAddress)) { // trying to submit with no email
            emailTextView.setError(getString(R.string.error_empty_field));
            emailTextView.requestFocus();
        } else if (!AuthInputValidator.emailValid(emailAddress)) { // invalid email
            emailTextView.setError(getString(R.string.error_invalid_email));
            emailTextView.requestFocus();
        } else {
            showSpinner(mSpinner, mViews);
            sendResetEmail(emailAddress);
        }
    }

    private void sendResetEmail(String emailAddress) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(emailAddress).addOnCompleteListener(task -> {
            if (task.getException() instanceof FirebaseNetworkException) {
                // only show error for no internet; don't let user know if email
                // isn't associated with an account
                Toast.makeText(getActivity(), R.string.firebase_fail_no_connection, Toast.LENGTH_LONG).show();
            } else { // success
                mBuilder.dismiss();
                Toast.makeText(getActivity(), getString(R.string.reset_email_confirm), Toast.LENGTH_LONG).show();
            }
            hideSpinner(mSpinner, mViews);
        });
    }

}
