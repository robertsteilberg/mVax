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
package mhealth.mvax.records.record.patient.modify.edit;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import mhealth.mvax.R;
import mhealth.mvax.model.immunization.DueDate;
import mhealth.mvax.model.immunization.Vaccination;
import mhealth.mvax.model.record.Patient;
import mhealth.mvax.records.record.patient.modify.ModifiablePatientFragment;
import mhealth.mvax.records.search.SearchFragment;
import mhealth.mvax.records.utilities.AlgoliaUtilities;

/**
 * @author Robert Steilberg
 * <p>
 * Fragment for editing existing record patient data
 */
public class EditPatientFragment extends ModifiablePatientFragment {

    private View mView;
    private ChildEventListener mPatientListener;

    public static EditPatientFragment newInstance(Patient patient) {
        final EditPatientFragment newInstance = new EditPatientFragment();
        final Bundle args = new Bundle();
        args.putSerializable("patient", patient);
        newInstance.setArguments(args);
        return newInstance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = super.onCreateView(inflater, container, savedInstanceState);
        mPatient = (Patient) getArguments().getSerializable("patient");

        setTitle(mPatient.getName());
        initPatientListener();

        mSearchEngine = new AlgoliaUtilities(getActivity(), initSuccessful -> {
            mLoadingModal.dismiss();
            if (initSuccessful) initButtons();
        });
        renderListView(mView.findViewById(R.id.details_list));
        return mView;
    }

    @Override
    public void onDestroyView() {
        destroyChildListener();
        super.onDestroyView();
    }

    private void initPatientListener() {
        mPatientListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                // this should never happen
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                mPatient = dataSnapshot.getValue(Patient.class);
                if (mPatient != null) {
                    mAdapter.refresh(mPatient.getDetails(getContext()));
                    Toast.makeText(getActivity(), R.string.patient_update_notification, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(getActivity(), R.string.patient_delete_success, Toast.LENGTH_SHORT).show();
                exit();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                // this should never happen
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), R.string.patient_download_fail, Toast.LENGTH_SHORT).show();
            }
        };
        mPatientRef
                .orderByKey()
                .equalTo(mPatient.getDatabaseKey())
                .addChildEventListener(mPatientListener);
    }

    private void initButtons() {
        initSaveButton(mView.findViewById(R.id.header_button));
        initDeleteButton(mView.findViewById(R.id.footer_button));
    }

    private void initDeleteButton(Button deleteButton) {
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setBackgroundResource(R.drawable.button_delete_record);
        deleteButton.setText(R.string.delete_record_button);
        deleteButton.setOnClickListener(view -> promptForRecordDelete());
    }

    private void promptForRecordDelete() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.modal_record_delete_title);
        builder.setMessage(R.string.modal_record_delete_message);
        builder.setPositiveButton(getResources().getString(R.string.modal_confirm), (dialog, which) -> deleteCurrentRecord());
        builder.setNegativeButton(getResources().getString(R.string.modal_cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteCurrentRecord() {
        mLoadingModal.createAndShow();
        destroyChildListener(); // prevent onChildRemoved action from firing before listener
        mSearchEngine.deleteObject(mPatient.getDatabaseKey(), this::deleteRecordFromDatabase);
    }

    private void destroyChildListener() {
        mPatientRef
                .orderByKey()
                .equalTo(mPatient.getDatabaseKey())
                .removeEventListener(mPatientListener);
    }

    private void deleteRecordFromDatabase() {
        mPatientRef.child(mPatient.getDatabaseKey()).setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                deleteVaccinations();
            } else {
                Toast.makeText(getActivity(), R.string.patient_delete_fail, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteVaccinations() {
        final String masterTable = getResources().getString(R.string.data_table);
        final String vaccinationTable = getResources().getString(R.string.vaccination_table);
        final String patientField = getResources().getString(R.string.patient_database_key);

        DatabaseReference vaccinationRef = FirebaseDatabase.getInstance().getReference()
                .child(masterTable)
                .child(vaccinationTable);
        Query vaccinationQuery = vaccinationRef
                .orderByChild(patientField)
                .equalTo(mPatient.getDatabaseKey());

        vaccinationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot vaccinationSnap : dataSnapshot.getChildren()) {
                    Vaccination vaccination = vaccinationSnap.getValue(Vaccination.class);
                    if (vaccination != null) {
                        String vaccinationKey = vaccination.getDatabaseKey();
                        vaccinationRef.child(vaccinationKey).setValue(null);
                    }
                }
                deleteDueDates();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mView.getContext(), R.string.patient_delete_incomplete, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void deleteDueDates() {
        final String masterTable = getResources().getString(R.string.data_table);
        final String dueDateTable = getResources().getString(R.string.due_date_table);
        final String patientField = getResources().getString(R.string.patient_database_key);

        DatabaseReference dueDateRef = FirebaseDatabase.getInstance().getReference()
                .child(masterTable)
                .child(dueDateTable);
        Query vaccinationQuery = dueDateRef
                .orderByChild(patientField)
                .equalTo(mPatient.getDatabaseKey());

        vaccinationQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dueDateSnap : dataSnapshot.getChildren()) {
                    DueDate dueDate = dueDateSnap.getValue(DueDate.class);
                    if (dueDate != null) {
                        String dueDateKey = dueDate.getDatabaseKey();
                        dueDateRef.child(dueDateKey).setValue(null);
                    }
                }
                mLoadingModal.dismiss();
                Toast.makeText(getActivity(), R.string.patient_delete_success, Toast.LENGTH_SHORT).show();
                exit();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(mView.getContext(), R.string.patient_delete_incomplete, Toast.LENGTH_LONG).show();
            }
        });

    }

    private void exit() {
        // pop view -> edit
        getActivity().getSupportFragmentManager().popBackStack();
        // pop create/view -> view/edit
        getActivity().getSupportFragmentManager().popBackStack();
        // pop search -> create/view
        getActivity().getSupportFragmentManager().popBackStack();

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, SearchFragment.newInstance());
        transaction.commit();
    }

}
