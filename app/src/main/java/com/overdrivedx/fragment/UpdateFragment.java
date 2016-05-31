package com.overdrivedx.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.overdrivedx.lwkmd2.R;
import com.overdrivedx.utils.Constants;


public class UpdateFragment extends DialogFragment {
    int mNum;
    public static final String UPDATE_MESSAGE = "update_msg";
    private String msg;
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static UpdateFragment newInstance(String msg) {
        UpdateFragment f = new UpdateFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(UPDATE_MESSAGE, msg);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            msg = getArguments().getString(UPDATE_MESSAGE);
        }

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.update_app, container, false);
        TextView ms = (TextView)v.findViewById(R.id.update_txt);
        ms.setText(msg);
        Button up = (Button) v.findViewById(R.id.update_btn);

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.UPDATE_APP));
                startActivity(browserIntent);
            }

        });


        return v;
    }
}
