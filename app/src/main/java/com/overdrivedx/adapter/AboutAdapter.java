package com.overdrivedx.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.overdrivedx.lwkmd2.R;

/**
 * Created by babatundedennis on 8/24/15.
 */
public class AboutAdapter  extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] names;
    private final String[] description;
    private
    static class ViewHolder {
        public TextView text;
        public TextView desc;

    }

    public AboutAdapter(Activity context, String[] names, String[] desc) {
        super(context, R.layout.fragment_about, names);
        this.context = context;
        this.names = names;
        this.description = desc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.about_item, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.aboutListTitle);
            viewHolder.desc = (TextView) rowView.findViewById(R.id.aboutListDesc);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.text.setText(names[position]);
        holder.desc.setText(description[position]);

        return rowView;
    }
}
