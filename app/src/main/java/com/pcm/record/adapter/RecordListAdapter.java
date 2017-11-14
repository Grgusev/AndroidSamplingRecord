package com.pcm.record.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pcm.record.R;

import java.util.ArrayList;

/**
 * Created by almond on 6/2/2017.
 */

public class RecordListAdapter extends BaseAdapter {

    Context mContext;
    private ArrayList<String> mFileLists;

    public RecordListAdapter(Context context, ArrayList<String> mFiles)
    {
        super();
        mContext    = context;
        mFileLists  = mFiles;
    }

    @Override
    public int getCount() {
        return mFileLists.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView titleView;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_record, parent, false);
        }

        titleView   = (TextView)convertView.findViewById(R.id.title);
        titleView.setText(mFileLists.get(position));

        return convertView;
    }
}
