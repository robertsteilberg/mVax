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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import mhealth.mvax.R;
import mhealth.mvax.auth.utilities.AuthInputValidator;
import mhealth.mvax.auth.utilities.FirebaseUtilities;
import mhealth.mvax.auth.utilities.Mailer;
import mhealth.mvax.model.user.User;
import mhealth.mvax.utilities.StringFetcher;

/**
 * @author Robert Steilberg
 * <p>
 * Modal and functionality for requesting a new mVax account
 */
public class RequestAccountModal extends CustomModal {

    private TextView mDisplayName;
    private TextView mEmail;
    private TextView mConfirmEmail;
    private TextView mPassword;
    private TextView mConfirmPassword;

    public RequestAccountModal(View view) {
        super(view);
    }

    @Override
    AlertDialog createDialog() {
        mBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.register_modal_title))
                .setView(getActivity().getLayoutInflater().inflate(R.layout.modal_request_account, (ViewGroup) getView().getParent(), false))
                .setPositiveButton(getString(R.string.submit), null)
                .setNegativeButton(getString(R.string.cancel), null)
                .create();

        mBuilder.setOnShowListener(dialogInterface -> {
            mSpinner = mBuilder.findViewById(R.id.spinner);

            mViews.add(mBuilder.findViewById(R.id.request_subtitle));

            mDisplayName = mBuilder.findViewById(R.id.display_name);
            mViews.add(mDisplayName);

            mEmail = mBuilder.findViewById(R.id.email);
            mViews.add(mEmail);

            mConfirmEmail = mBuilder.findViewById(R.id.email_confirm);
            mViews.add(mConfirmEmail);

            mPassword = mBuilder.findViewById(R.id.password);
            mViews.add(mPassword);

            mConfirmPassword = mBuilder.findViewById(R.id.password_confirm);
            mConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
                if (event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN // debounce
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    // enter on hardware keyboard submits request
                    validateFields();
                    return true;
                }
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // "Done" button submits request
                    validateFields();
                    return true;
                }
                return false;
            });
            mViews.add(mConfirmPassword);

            mViews.add(mBuilder.getButton(AlertDialog.BUTTON_NEGATIVE));

            final Button positiveButton = mBuilder.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(view -> validateFields());
            mViews.add(positiveButton);
        });
        return mBuilder;
    }

    private void validateFields() {
        if (noEmptyFields() && authFieldsValid()) {
            showSpinner();
            final String displayName = mDisplayName.getText().toString();
            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();
            // validation complete, go for actual register request
            registerNewUser(displayName, email, password);
        }
    }

    private boolean noEmptyFields() {
        boolean noEmptyFields = true;
        if (TextUtils.isEmpty(mConfirmPassword.getText().toString())) {
            mConfirmPassword.setError(getString(R.string.empty_field));
            mConfirmPassword.requestFocus();
            noEmptyFields = false;
        }
        if (TextUtils.isEmpty(mPassword.getText().toString())) {
            mPassword.setError(getString(R.string.empty_field));
            mPassword.requestFocus();
            noEmptyFields = false;
        }
        if (TextUtils.isEmpty(mConfirmEmail.getText().toString())) {
            mConfirmEmail.setError(getString(R.string.empty_field));
            mConfirmEmail.requestFocus();
            noEmptyFields = false;
        }
        if (TextUtils.isEmpty(mEmail.getText().toString())) {
            mEmail.setError(getString(R.string.empty_field));
            mEmail.requestFocus();
            noEmptyFields = false;
        }
        if (TextUtils.isEmpty(mDisplayName.getText().toString())) {
            mDisplayName.setError(getString(R.string.empty_field));
            mDisplayName.requestFocus();
            noEmptyFields = false;
        }
        return noEmptyFields;
    }

    private boolean authFieldsValid() {
        boolean authFieldsValid = true;
        final String email = mEmail.getText().toString();
        final String confirmEmail = mConfirmEmail.getText().toString();

        if (!AuthInputValidator.emailValid(email)) {
            mEmail.setError(getString(R.string.invalid_email_error));
            mEmail.requestFocus();
            authFieldsValid = false;
        } else if (!TextUtils.equals(email, confirmEmail)) { // email fields don't match
            mEmail.setError(getString(R.string.email_field_mismatch));
            mConfirmEmail.setError(getString(R.string.email_field_mismatch));
            mEmail.requestFocus();
            authFieldsValid = false;
        }

        final String password = mPassword.getText().toString();
        final String confirmPassword = mConfirmPassword.getText().toString();
        if (!AuthInputValidator.passwordValid(password)) {
            mPassword.setError(getString(R.string.invalid_password_error));
            mPassword.requestFocus();
            authFieldsValid = false;
        } else if (!TextUtils.equals(password, confirmPassword)) { // password fields don't match
            mPassword.setError(getString(R.string.password_field_mismatch));
            mConfirmPassword.setError(getString(R.string.password_field_mismatch));
            mPassword.requestFocus();
            authFieldsValid = false;
        }
        return authFieldsValid;
    }

    private void registerNewUser(String displayName, String email, String password) {
        FirebaseUtilities.createDisabledUser(email, password, displayName)
                .addOnCompleteListener(createTask -> {
                    if (createTask.isSuccessful()) {
                        // get UID from result
                        addRequest(email, displayName, createTask.getResult());
                    } else {
                        Toast.makeText(getActivity(), R.string.request_submit_fail, Toast.LENGTH_LONG).show();
                        hideSpinner();
                    }
                });
    }

    private void addRequest(String email, String displayName, String uid) {
        final DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.userRequestsTable))
                .child(uid);

        final User newUser = new User(uid);
        newUser.setDisplayName(displayName);
        newUser.setEmail(email);

        requestsRef.setValue(newUser).addOnCompleteListener(addUserRequest -> {
            if (addUserRequest.isSuccessful()) {
                hideSpinner();
                mBuilder.dismiss();
                sendConfirmationEmail(newUser);
                Toast.makeText(getActivity(), R.string.request_submit_success, Toast.LENGTH_LONG).show();
            } else {
                hideSpinner();
                // unable to push request to UserRequest table, so attempt to delete the disabled
                // user out of the auth table
                FirebaseUtilities.deleteUser(uid);
                Toast.makeText(getActivity(), R.string.request_submit_unknown_fail, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendConfirmationEmail(User newUser) {
        final String subject = getString(R.string.confirm_email_subject);
        final String body = String.format(StringFetcher.fetchString(R.string.confirm_email_body),
                newUser.getDisplayName());
        new Mailer(getContext())
                .withMailTo(newUser.getEmail())
                .withSubject(subject)
                .withBody(body)
                .withProcessVisibility(false)
                .send();
    }

}