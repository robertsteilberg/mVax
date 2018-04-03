package mhealth.mvax.auth;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import mhealth.mvax.R;
import mhealth.mvax.model.user.User;
import mhealth.mvax.model.user.UserWithUID;

/**
 * @author Matthew Tribby
 *         <p>
 *         Description Here
 */

public class CurrentUsersAdapter extends BaseAdapter {
    private Activity mActivity;
    private List<UserWithUID> users;

    public CurrentUsersAdapter(Activity activity, List<UserWithUID> users){
        this.mActivity = activity;
        this.users = users;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater= mActivity.getLayoutInflater();

        view=inflater.inflate(R.layout.current_user_row, null);

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView email = (TextView) view.findViewById(R.id.edittext_email);
        TextView role = (TextView) view.findViewById(R.id.role);

        UserWithUID user = users.get(i);
        name.setText(user.getFirstName() + " " + user.getLastName());
        email.setText(user.getEmail());
        role.setText(user.getRole());
        return view;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public User getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


}
