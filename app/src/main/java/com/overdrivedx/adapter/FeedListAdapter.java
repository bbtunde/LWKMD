package com.overdrivedx.adapter;

/**
 * Created by babatundedennis on 8/24/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.overdrivedx.lwkmd2.AppController;
import com.overdrivedx.lwkmd2.R;
import com.overdrivedx.models.FeedItem;
import com.overdrivedx.view.BlurTransformation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class FeedListAdapter extends BaseAdapter{
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;

    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    BlurTransformation bt;

    private static final float BLUR_RADIUS = 25F;

    public FeedListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
        bt =new BlurTransformation(activity,BLUR_RADIUS);
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_item, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        TextView body = (TextView) convertView.findViewById(R.id.feed_body);
        final View overlay_text =  convertView.findViewById(R.id.overlay_text);
        final View layout = convertView.findViewById(R.id.layout);
        final ImageView feedImageView = (ImageView) convertView.findViewById(R.id.feedImage1);


        FeedItem item = feedItems.get(position);

        if(item.getBody().length() < 5 || item.getBody().equalsIgnoreCase("null")){
            overlay_text.setVisibility(View.GONE);
        }
        else{
            body.setText(item.getBody());
        }

        if (item.getImages() != null) {

            Picasso.with(activity)
                    .load(item.getImages())//.transform(bt)
                    .placeholder(R.drawable.feed_image_loading)
                    .error(R.drawable.feed_image_error)
                    .into(feedImageView);
            feedImageView.setVisibility(View.VISIBLE);

        }
        else {
            feedImageView.setVisibility(View.GONE);
        }

        feedImageView.setOnLongClickListener(new AdapterView.OnLongClickListener() {
            public boolean onLongClick(View v) {
               // layout.backgroundC
                Vibrator vib = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(100);

                File filepath = Environment.getExternalStorageDirectory();
                File dir = new File(filepath.getAbsolutePath() + "/LWKMD/");
                dir.mkdirs();
                File file = new File(dir, "lwkmd_for_android.png");

                OutputStream output;
                try {
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");

                    output = new FileOutputStream(file);
                    final Bitmap bitmap = ((BitmapDrawable)feedImageView.getDrawable()).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                    output.flush();
                    output.close();
                    Uri uri = Uri.fromFile(file);
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    activity.startActivity(Intent.createChooser(share, "Share Image Tutorial"));

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return false;
            }
        });

        return convertView;
    }



}
