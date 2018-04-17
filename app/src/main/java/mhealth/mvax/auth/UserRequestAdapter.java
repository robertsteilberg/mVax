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
package mhealth.mvax.auth;

import android.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mhealth.mvax.R;
import mhealth.mvax.auth.modals.ApproveUserModal;
import mhealth.mvax.auth.modals.DenyUserModal;
import mhealth.mvax.model.user.User;
import mhealth.mvax.model.user.UserRole;

/**
 * @author Matthew Tribby, Robert Steilberg
 * <p>
 * Adapter for rendering user requests, which can be approved or denied by
 * an administrator
 */
public class UserRequestAdapter extends RecyclerView.Adapter<UserRequestAdapter.ViewHolder> {

    final private List<User> mUserRequests;

    UserRequestAdapter() {
        mUserRequests = new ArrayList<>();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, email;
        final RadioGroup roles;
        final Button approveButton, denyButton;
        final ImageView infoButton;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.user_name);
            email = view.findViewById(R.id.user_email);
            roles = view.findViewById(R.id.role_radio_group);
            infoButton = view.findViewById(R.id.info_button);
            approveButton = view.findViewById(R.id.approve_button);
            denyButton = view.findViewById(R.id.deny_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_user_request, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final User request = mUserRequests.get(position);
        holder.name.setText(request.getDisplayName());
        holder.email.setText(request.getEmail());

        holder.infoButton.setOnClickListener(v ->
                new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.role_info_title)
                        .setMessage(R.string.role_info_desc)
                        .setPositiveButton(R.string.ok, null)
                        .show());

        holder.approveButton.setOnClickListener(v -> {
            int selectedId = holder.roles.getCheckedRadioButtonId();
            switch (selectedId) {
                case -1:
                    Toast.makeText(v.getContext(), R.string.no_role_selected, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.admin_radio_button:
                    new ApproveUserModal(v, request, UserRole.ADMIN).show();
                    break;
                case R.id.reader_radio_button:
                    new ApproveUserModal(v, request, UserRole.READER).show();
            }
        });
        holder.denyButton.setOnClickListener(v -> new DenyUserModal(v, request).show());
    }

    @Override
    public int getItemCount() {
        return mUserRequests.size();
    }

    /**
     * Add a user request to the RecyclerView
     *
     * @param newRequest new user request to be added to
     *                   the adapter
     */
    public void addRequest(User newRequest) {
        mUserRequests.add(newRequest);
        notifyDataSetChanged();
    }

    /**
     * Remove a user request from the RecyclerView; nothing
     * happens if the request isn't found
     *
     * @param request existing user request to be removed
     *                from the adapter
     */
    public void removeRequest(User request) {
        mUserRequests.removeIf(ur -> ur.getDatabaseKey().equals(request.getDatabaseKey()));
        notifyDataSetChanged();
    }

}
