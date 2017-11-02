package mhealth.mvax.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import mhealth.mvax.R;
import mhealth.mvax.model.Record;
import mhealth.mvax.model.Vaccine;
import mhealth.mvax.search.SearchResultAdapter;

/**
 * Created by AlisonHuang on 11/1/17.
 */

public class VaccineCardAdapter extends BaseAdapter {

    //================================================================================
    // Properties
    //================================================================================

    private Context mContext;

    private LayoutInflater mInflater;

    private List<Vaccine> mDataSource;


    //================================================================================
    // Constructors
    //================================================================================

    VaccineCardAdapter(Context context, Collection<Vaccine> vaccines) {
        mContext = context;
        mDataSource = new ArrayList<>(vaccines);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    void refresh(Collection<Vaccine> values) {
        mDataSource = new ArrayList<>(values);
        notifyDataSetChanged();
    }

    String getPatientIdFromDataSource(int position) {
        return mDataSource.get(position).getId();
    }

    @Override
    public View getView(int position, View rowView, ViewGroup parent) {

        if (rowView == null) {
            rowView = mInflater.inflate(R.layout.list_item_vaccine_card, parent, false);

        }


        Vaccine vaccine = (Vaccine) getItem(position);


        return rowView;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


}
