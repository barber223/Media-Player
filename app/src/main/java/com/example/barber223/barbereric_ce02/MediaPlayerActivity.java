//EricBarber
//MDF3-1804
//MediaPlayerActivity.java

package com.example.barber223.barbereric_ce02;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.barber223.barbereric_ce02.Fragments.MediaPlayerFragment;

public class MediaPlayerActivity extends AppCompatActivity implements MediaPlayerFragment.PlaybackCommandListener, ServiceConnection {

    private boolean mBound = false;
    private MediaServiceClass mService;
    private BroadCastReciever mReciver;
    private ToFragmentInterface mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        MediaPlayerFragment frag = new MediaPlayerFragment();
        mListener = frag;
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame,
                        frag).commit();
    }


    //activity overload methods for handling the starting and ending of
    //the background service
    @Override
    protected void onStop() {
        super.onStop();
        mBound = false;
        unbindService(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mBound = false;
        Intent service = new Intent(this, MediaServiceClass.class);
        //this will let you create usually used
        bindService(service, this, BIND_AUTO_CREATE);
    }


    //Set up the Receiver for the service
    @Override
    protected void onResume() {
        super.onResume();
        mReciver = new BroadCastReciever();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServiceHelper.KEY_ACTION_NEW_SONG);
        filter.addAction(ServiceHelper.KEY_ACTION_TRACK_UPDATE);
        filter.addAction(ServiceHelper.KEY_KILLPROGRESS);
        filter.addAction(ServiceHelper.KEY_STOPUI);
        registerReceiver(mReciver, filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReciver);
    }


    //Interface methods to communicate with the service
    @Override
    public void play() {
        if (mBound) {
            mService.play();
            Intent playIntent = new Intent(this, MediaServiceClass.class);
            startService(playIntent);
        }
    }
    @Override
    public void pause() {
        if (mBound){
            mService.pause();
            Intent servieIntent = new Intent(this, MediaServiceClass.class);
            startService(servieIntent);
        }
    }
    @Override
    public void stop() {
        if (mBound){
            mService.stop();
            Intent servieIntent = new Intent(this, MediaServiceClass.class);
            startService(servieIntent);
        }
    }
    @Override
    public void skip() {
        if (mBound){
            mService.skipForward();
            Intent servieIntent = new Intent(this, MediaServiceClass.class);
            startService(servieIntent);
        }
    }
    @Override
    public void previous() {
        if (mBound){
            mService.returnPrevious();
            Intent servieIntent = new Intent(this, MediaServiceClass.class);
            startService(servieIntent);
        }
    }
    @Override
    public void shuffle(boolean _shuffle) {
        if(mBound){
            mService.setShuffle(_shuffle);
            Intent servieIntent = new Intent(this, MediaServiceClass.class);
            startService(servieIntent);
        }
    }
    @Override
    public void loop(boolean _looping) {
        if(mBound){
            mService.setLooping(_looping);
            Intent servieIntent = new Intent(this, MediaServiceClass.class);
            startService(servieIntent);
        }
    }
    @Override
    public void seekBarWasAltered(int _newPosition) {
        if(mBound){
            mService.onSeekBarChanged(_newPosition);
            Intent serviceIntent = new Intent(this, MediaServiceClass.class);
            startService(serviceIntent);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListener.resetProgressBar();
    }

    //after these need to get the ability to set the track position based off the
    //the seek bar// That will transpire at a later time
    //this is used for when the apllication is closed while playing to reset the values of the seek bar
    private void setUI(){
        //reseting the status bar when it starts back up while running
        if (mBound) {
            int postion = mService.getTrackPosition();
            int duration = mService.getcurrentTrackLength();
            boolean playing = mService.getTrackPlaying();
            if (duration != 0) {
                mListener.passingSeekInformation(duration / 1000, postion/1000, playing);
                //this will tell the service it needs to send aanother broadcast with the song information
                mService.activityRebootAccessSongInfo();
            }
        }
    }

    //This is for when the service becomes active need to set the binding to true
    // and bind the service
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        //Need to know
        mBound = true;
        MediaServiceClass.MediaServiceBinder binder = (MediaServiceClass.MediaServiceBinder) iBinder;
        mService = binder.getService();
        mBound = true;
        setUI();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
    }

    //broadcast reciever to obtain track info
    public class BroadCastReciever extends BroadcastReceiver{
        BroadCastReciever(){super();}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ServiceHelper.KEY_ACTION_NEW_SONG)){
                //will need to get all of the infomration to repopulate UI
                String title = intent.getStringExtra(ServiceHelper.KEY_TITLE);
                String author = intent.getStringExtra(ServiceHelper.KEY_AUTHOR);
                int imageLocation = intent.getIntExtra(ServiceHelper.KEY_IMAGE_LOCATION, 0);
                boolean looping = intent.getBooleanExtra(ServiceHelper.KEY_LOOPING, false);
                boolean shuffling = intent.getBooleanExtra(ServiceHelper.KEY_SHUFFLNG, false);
                mListener.setUIForNewImage(title, author, imageLocation);
                mListener.setUpCheckBoxes(shuffling, looping);

            }else if (intent.getAction().equals(ServiceHelper.KEY_ACTION_TRACK_UPDATE)){
                //update the seek bar
                int duration = intent.getIntExtra(ServiceHelper.KEY_DURATION, 0);
                boolean playing = intent.getBooleanExtra(ServiceHelper.KEY_TRACK_PLAYING, false);
                int currentPosition = intent.getIntExtra(ServiceHelper.KEY_TRACK_CURRENTLOCATION, 0);
                //need to create an interface to send a message"" to the fragment UI
                mListener.passingSeekInformation(duration, currentPosition, playing);
            }else if (intent.getAction().equals(ServiceHelper.KEY_KILLPROGRESS)){
                mListener.resetProgressBar();
            }
            else if (intent.getAction().equals(ServiceHelper.KEY_STOPUI)){
                mListener.stopAllUI();
            }
        }
    }
}
