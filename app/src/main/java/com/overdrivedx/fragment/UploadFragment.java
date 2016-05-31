package com.overdrivedx.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.overdrivedx.lwkmd2.R;
import com.overdrivedx.utils.Constants;
import com.overdrivedx.utils.Utils;

import java.io.File;
import java.net.URISyntaxException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UploadFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UploadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UploadFragment extends Fragment implements Animation.AnimationListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Activity activity;
    private OnFragmentInteractionListener mListener;
    private static String fileName;
    private String ImgPath;
    private static final int FILE_SELECTED = 1;
    private static final String MY_BUCKET ="boxment";
    private TransferObserver observer;
    private TransferUtility transferUtility;
    Animation myAnimation;

    private TextView upload;
    private TextView wordcount;
    private TextView upload_text;
    private String caption_string;

    private Utils util;


    private Socket mSocket;{
        try {
            mSocket = IO.socket(Constants.SOCKETIO_HOST);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Dialog dialog_waiting;
    private Dialog popup_caption;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UploadFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UploadFragment newInstance(String param1, String param2) {
        UploadFragment fragment = new UploadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public UploadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        util = new Utils();
        transferUtility = util.getTransferUtility(activity);

        mSocket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //error
                UploadEnded();
                util.onError("Unable to connect to server.", activity);
            }

        });
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //upload to bucket
                //Log.e(Constants.TAG, "socket connected");
                new UploadToS3Bucket().execute();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_upload, container, false);

        TextView home = (TextView) v.findViewById(R.id.home);
        TextView  camera = (TextView) v.findViewById(R.id.camera);
        upload = (TextView)v.findViewById(R.id.uploadPic);
        upload_text =  (TextView)v.findViewById(R.id.upload_text);
        home.setText(String.valueOf((char) 0xe601));
        camera.setText(String.valueOf((char) 0xe604));
        upload.setText(String.valueOf((char) 0xe600));

        home.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));
        camera.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));
        upload.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));

        myAnimation = AnimationUtils.loadAnimation(activity, R.anim.upload_click_anim);
        myAnimation.setAnimationListener(this);
        upload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                upload.startAnimation(myAnimation);
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onUploadFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        OpenGallery();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onUploadFragmentInteraction(Uri uri);
    }

    private void OpenGallery(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), FILE_SELECTED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FILE_SELECTED) {

                try {
                    Uri originalUri = data.getData();
                    String pathsegment[] = originalUri.getLastPathSegment().split(":");
                    String id = pathsegment[0];
                    final String[] imageColumns = { MediaStore.Images.Media.DATA };

                    Uri uri = getUri();
                    Cursor imageCursor = activity.getContentResolver().query(uri, imageColumns,
                            MediaStore.Images.Media._ID + "=" + id, null, null);

                    if (imageCursor.moveToFirst()) {
                        fileName = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                        PopUp();
                    }

                } catch (Exception e) {
                    util.onError("Failed to get image", activity);
                    UploadEnded();
                }

            }
        }

    }

    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    private void PopUp(){

        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            popup_caption = new Dialog(activity);
            popup_caption.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
                popup_caption.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                popup_caption.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                popup_caption.setContentView(R.layout.upload_popup);
                popup_caption.setCancelable(false);

            final EditText caption =  (EditText)popup_caption.findViewById(R.id.caption);
            caption.addTextChangedListener(mTextEditorWatcher);
            wordcount = (TextView)popup_caption.findViewById(R.id.wordcount);

            TextView close =  (TextView)popup_caption.findViewById(R.id.close);
            close.setText(String.valueOf((char) 0xe608));
            close.setTypeface(Typeface.createFromAsset(activity.getAssets(), "icomoon.ttf"));

            Button uploadButton = (Button) popup_caption.findViewById(R.id.dialogButtonOK);
            // if button is clicked, close the custom dialog
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if(util.isConnectedToInternet(activity)){
                    dialog_waiting = util.Waiting(activity);
                    upload.setVisibility(View.INVISIBLE);
                    upload_text.setVisibility(View.INVISIBLE);
                    caption_string = null;
                    caption_string = caption.getText().toString();
                    mSocket.connect();
                }
                else{
                    util.onError("No internet connection.", activity);
                    UploadEnded();
                }

                }
            });

            close.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                    popup_caption.dismiss();
                    upload.setVisibility(View.VISIBLE);
                    upload_text.setVisibility(View.VISIBLE);
                 }
            });

            upload.setVisibility(View.INVISIBLE);
            upload_text.setVisibility(View.INVISIBLE);
            popup_caption.show();
            }
        });
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int counts = s.length();
            int desc = 140 - counts;

            if(desc== 0){

            }
            else {
                wordcount.setText(String.valueOf(desc));
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };


    private class UploadToS3Bucket extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            if (isCancelled()){
                cancel(true);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            final File file = new File(fileName);

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Log.e(Constants.TAG, "running");
                    observer = transferUtility.upload(MY_BUCKET, file.getName(), file);
                    observer.setTransferListener(new TransferListener() {
                        @Override
                        public void onStateChanged(int i, TransferState transferState) {

                            if (transferState.COMPLETED.equals(observer.getState())){
                                mSocket.emit("add_picture", caption_string, file.getName());
                                UploadEnded();
                            }

                        }

                        @Override
                        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                        }

                        @Override
                        public void onError(int id, Exception e) {
                            UploadEnded();
                            new Utils().onError("Unable to upload. Network error", activity);
                            cancel(true);
                        }
                    });
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

    private void UploadEnded(){
        if(mSocket != null) {
            caption_string = null;
            mSocket.disconnect();
        }

        upload.setVisibility(View.VISIBLE);
        upload_text.setVisibility(View.VISIBLE);

        if(dialog_waiting != null) {
            dialog_waiting.dismiss();
        }

        if(popup_caption != null) {
            caption_string = null;
            mSocket.disconnect();
            popup_caption.dismiss();
        }

        if(observer != null){
            caption_string = null;
            mSocket.disconnect();
            transferUtility.cancel(observer.getId());
        }
    }



}
