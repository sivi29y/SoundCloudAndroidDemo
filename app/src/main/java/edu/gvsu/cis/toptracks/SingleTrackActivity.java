package edu.gvsu.cis.toptracks;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by CircuitCity on 17/04/2015.
 *
 * @author: Sivan Langer
 * all right reserved
 */
public class SingleTrackActivity extends Activity implements MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl {


    private static final String TAG = "SingleTrackActivity";
    private static final int IO_BUFFER_SIZE = 1024;
    ImageButton img;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;
    private MediaController mediaController;
    DownloadImgFileAsync d = new DownloadImgFileAsync();
    private Handler handler = new Handler();
    private ImageView Artwork;
    private String trackUriString;
    private Intent intent;
    private MediaPlayer mediaPlayer;
    private Integer index = 0;
    Map<Integer,String> songLib = new HashMap<Integer,String>();
    private String path;
    private String trackTitle;
    private TextView Title;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.track);

        Title = (TextView) findViewById(R.id.now_playing_text);
        Title.setPadding(30,30,30,30);
        Title.setText(trackTitle);



//        index +=1;
//        path = Environment.getExternalStorageDirectory().getPath()+"/Toptracks/song"+ index.toString() +".mp3";
        //streaming audio on other thread
        //startDownload(trackUriString);


        intent = getIntent();
        String imgUriString = intent.getStringExtra("image_url");
        //streaming img on another thread
        imgUriString.replace("-large", "-t500x500");
        startDownloadImg(imgUriString);
        //DownloadImgFileAsync.THREAD_POOL_EXECUTOR.execute();


        trackTitle = intent.getStringExtra("title");
        trackUriString = intent.getStringExtra("stream_url");

        String auth = "?client_id=d652006c469530a4a7d6184b18e16c81";
        trackUriString += auth;
        Log.d(TAG, trackUriString);
        //img download
        Artwork = (ImageView) findViewById(R.id.image);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setLooping(false);



    }

    @Override
    public void onResume() {
        super.onResume();

        mediaController = new MediaController(this);

        songLib.put(index,path);



        try {

            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //Log.d(TAG, path);
            mediaPlayer.setDataSource(trackUriString);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            Log.e(TAG, "Could not open file " + trackUriString + " for playback.", e);
        }


    }


    @Override
    public void onPause() {
        super.onPause();
        mediaController.hide();
        //MediaPlayer mediaPlayer;
        mediaPlayer.pause();
        //mediaPlayer.stop();
        mediaPlayer.release();
       // mediaPlayer = null;

       // trackUriString = null;


    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        mediaController.hide();
//       if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
//           mediaPlayer.stop();
//           mediaPlayer.release();
//       }
//        trackUriString = null;
//
//    }




    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //the MediaController will hide after 3 seconds
        // - tap the screen to make it appear again
        mediaController.show();
        return false;
    }


    //media player methods


    //--MediaPlayerControl methods----------------------------------------------------
    public void start() {
        mediaPlayer.start();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getBufferPercentage() {
        return 0;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    //--------------------------------------------------------------------------------

    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");

        mediaController.setMediaPlayer(this);
        mediaController.setAnchorView(findViewById(R.id.main_audio_view));
        mediaPlayer.start();

        handler.post(new Runnable() {
            public void run() {
                mediaController.setEnabled(true);
                mediaController.show();
            }
        });
    }

    private void startDownload(String url) {

        new DownloadFileAsync().execute(url);
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;
            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(path);


                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
//            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            //  dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

        }
    }


    private void startDownloadImg(String url) {
        //String url = "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
       d.execute(url);
    }


    private class DownloadImgFileAsync extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... param) {


            try {
                InputStream s = (InputStream) new URL(param[0]).getContent();
                if (s != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(s);

                    return bitmap;
                }

                //i.setImageBitmap(bitmap);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Log.i("Async-Example", "onPostExecute Called");
            if (result!=null)
            Artwork.setImageBitmap(Bitmap.createScaledBitmap(result, 300, 300, true));
            //Artwork.setImageBitmap(result);
        }
    }
}


