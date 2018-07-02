//EricBarber
//MDF3-1804
//MediaServiceClass.java

package com.example.barber223.barbereric_ce02;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Random;

public class MediaServiceClass extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    //need variables for mediaPlayer and stateOfMediaPlayer
    private MediaPlayer mediaPlayer;
    private int mState;
    //need an id for the notification
    //need to attach the service to the notification so it can't be killed
    public static final int NOTIFICATION_ID = 0x01010;

    //I only need shuffling to repopulate the Ui after when the app gets relaunched
    private boolean mLooping = false;
    private boolean mShuffling = false;

    //a list of the possible songs to play
    private ArrayList<MediaData> songsToPlay = new ArrayList<>();
    private int currentSongIndex = 0;

    private int mSongDuration = 0;

    private MediaDataPuller puller;

    private BroadCastServiceReciever reciver;


    //THESE are the variables for the state variables
    private static final int STATE_IDLE = 0;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_PREPARING = 2;
    private static final int STATE_PREPARED = 3;
    private static final int STATE_STARTED = 4;
    private static final int STATE_PAUSED = 5;
    private static final int STATE_STOPPED = 6;
    private static final int STATE_PLAYBACK_COMPLETED = 7;
    private static final int STATE_END = 8;


    public MediaServiceClass() {
        puller = new MediaDataPuller();
        songsToPlay = puller.getMediafiles();
        reciver = new BroadCastServiceReciever();
    }


    //need t set up the service lifecycle methods
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mState = STATE_IDLE;
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        //set up the reciver to be standalone for comunications with the notification
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ServiceHelper.KEY_SKIP_FORWARD);
        filter.addAction(ServiceHelper.KEY_PREVIOUS);
        this.registerReceiver(reciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //good bye
        mediaPlayer.release();
        mState = STATE_END;
        this.unregisterReceiver(reciver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MediaServiceBinder();
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_music_note_black_24dp);
        builder.setContentTitle("Audio Player");
        builder.setContentText("Click to reopen the app");
        //this will force the reopening on the app click stop then close
        builder.setOngoing(true);

        Intent activityIntent = new Intent(this, MediaPlayerActivity.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(activityPendingIntent);

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
        style.setBigContentTitle("Title: " + songsToPlay.get(currentSongIndex).getTitle());
        style.setSummaryText("   Artist: " + songsToPlay.get(currentSongIndex).getAuthor());
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), songsToPlay.get(currentSongIndex).getMusicArtInt());
        style.bigPicture(bmp);

        builder.setStyle(style);

        //Need an action button in the notification for the previous
        Intent backSkipIntent = new Intent();
        backSkipIntent.setAction(ServiceHelper.KEY_PREVIOUS);
        PendingIntent back = PendingIntent.getBroadcast(this, 0, backSkipIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.mipmap.ic_skip_previous_black_24dp, "Skip Back", back);


        //need to add the buttons to allow the user to skip songs and such
        Intent skipForwardIntent = new Intent();
        skipForwardIntent.setAction(ServiceHelper.KEY_SKIP_FORWARD);
        PendingIntent skip = PendingIntent.getBroadcast(this, 0, skipForwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.addAction(R.mipmap.ic_skip_next_black_24dp, "Skip Forward", skip);

        return builder.build();

    }

    class MediaServiceBinder extends Binder {
        MediaServiceClass getService() {
            return MediaServiceClass.this;
        }
    }

    //UI Button commands
    @Override
    public void onPrepared(MediaPlayer mp) {
        //the state is ready
        mState = STATE_PREPARED;
        mSongDuration = mediaPlayer.getDuration();
        mediaPlayer.start();
        newSongToBeDisplayed();
        startProgressBar();
        mState = STATE_STARTED;
    }

    //media Functionality for the button presses
    public void play() {
        if (mState == STATE_PAUSED) {
            //dont need to start other aspects
            mediaPlayer.start();
            startProgressBar();
            mState = STATE_STARTED;
        } else if (mState != STATE_STARTED && mState != STATE_PREPARING) {

            mediaPlayer.reset();
            mState = STATE_IDLE;
            try {
                mediaPlayer.setDataSource(this, songsToPlay.get(currentSongIndex).getMusicPath());
                mState = STATE_INITIALIZED;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mState == STATE_INITIALIZED) {
                mediaPlayer.prepareAsync();
                mState = STATE_PREPARING;
            }
        }
        Notification onGoing = buildNotification();
        startForeground(NOTIFICATION_ID, onGoing);
    }

    public void pause() {
        if (mState == STATE_STARTED) {
            mediaPlayer.pause();
            mState = STATE_PAUSED;
            stopProgessBar();
        }
    }

    public void stop() {
        if (mState == STATE_STARTED || mState == STATE_PAUSED || mState == STATE_PLAYBACK_COMPLETED) {
            mediaPlayer.stop();
            mState = STATE_STOPPED;
            //kill notification
            stopForeground(true);
            killprogressBar();
            stopAllSongBeingDisplayed();
        }
    }

    public void skipForward() {
        if (mState == STATE_STARTED || mState == STATE_PAUSED) {
            if (!mLooping) {
                if (currentSongIndex < songsToPlay.size() - 1) {
                    currentSongIndex++;
                } else {
                    currentSongIndex = 0;
                }
                mState = STATE_IDLE;
                play();
                killprogressBar();
            }else{
                //do nothing no skipping mister
            }
        }
    }

    public void returnPrevious() {
        if (mState == STATE_STARTED || mState == STATE_PAUSED) {
            if (!mLooping) {
                if (currentSongIndex > 0) {
                    currentSongIndex--;
                } else {
                    currentSongIndex = songsToPlay.size() - 1;
                }
                mState = STATE_IDLE;
                play();
                killprogressBar();
            }else{
                //no skipping
            }
        }
    }

    public void onSeekBarChanged(int newSongPosition) {
        if (mState == STATE_PREPARED || mState == STATE_PAUSED || mState == STATE_PLAYBACK_COMPLETED
                || mState == STATE_STARTED) {
            //set the current time that the song should be at
            mediaPlayer.seekTo(newSongPosition * 1000);
        } else {
            //need to set the progress bars location to that of null due to there is no song to be seeking with
            killprogressBar();
        }
    }


    //when this will be called the application will be reopened
    // this will then only have a song to be running at this point
    // all of this is used for when the notification handling
    public boolean getTrackPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getTrackPosition() {
        if (mediaPlayer.isPlaying() || mState == STATE_PAUSED) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getcurrentTrackLength() {
        if (mState == STATE_PREPARED || mState == STATE_STARTED || mState == STATE_PAUSED ||
                mState == STATE_STOPPED || mState == STATE_PLAYBACK_COMPLETED) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mState = STATE_PLAYBACK_COMPLETED;
        if (!mLooping) {
            if (currentSongIndex < songsToPlay.size() - 1) {
                currentSongIndex++;
            } else {
                currentSongIndex = 0;
            }
            mState = STATE_IDLE;
            killprogressBar();
            play();
        }else{
            //This will restart the song
            mediaPlayer.start();
            mState = STATE_STARTED;
            //need to kill the progress bar before restarting to clear the timer and the ui work
            killprogressBar();
            //need to reset the progress bar with the prepared song
            //Dont need to get the values from file due to the same song is playing
            startProgressBar();
        }


        // not working will fix when i can skip to end of song
    }

    //Methods to deal with the progress bar with the use of a broadcast
    public void stopProgessBar() {
        //this will be called when the user pauses
        Intent stopProgressBar = new Intent();
        stopProgressBar.setAction(ServiceHelper.KEY_ACTION_TRACK_UPDATE);
        stopProgressBar.putExtra(ServiceHelper.KEY_DURATION, mSongDuration / 1000);
        stopProgressBar.putExtra(ServiceHelper.KEY_TRACK_PLAYING, false);
        stopProgressBar.putExtra(ServiceHelper.KEY_TRACK_CURRENTLOCATION, mediaPlayer.getCurrentPosition() / 1000);
        sendBroadcast(stopProgressBar);
    }

    public void startProgressBar() {
        Intent callback = new Intent();
        callback.setAction(ServiceHelper.KEY_ACTION_TRACK_UPDATE);
        callback.putExtra(ServiceHelper.KEY_DURATION, mSongDuration / 1000);
        //media is playing
        callback.putExtra(ServiceHelper.KEY_TRACK_PLAYING, true);
        callback.putExtra(ServiceHelper.KEY_TRACK_CURRENTLOCATION, mediaPlayer.getCurrentPosition() / 1000);
        sendBroadcast(callback);
    }

    public void killprogressBar() {
        Intent kill = new Intent();
        kill.setAction(ServiceHelper.KEY_KILLPROGRESS);
        sendBroadcast(kill);
    }

    //methods for handling the UI upload for the new song to be displayed
    public void newSongToBeDisplayed() {
        Intent newSongIntent = new Intent();
        newSongIntent.setAction(ServiceHelper.KEY_ACTION_NEW_SONG);
        newSongIntent.putExtra(ServiceHelper.KEY_TITLE, songsToPlay.get(currentSongIndex).getTitle());
        newSongIntent.putExtra(ServiceHelper.KEY_AUTHOR, songsToPlay.get(currentSongIndex).getAuthor());
        newSongIntent.putExtra(ServiceHelper.KEY_IMAGE_LOCATION, songsToPlay.get(currentSongIndex).getMusicArtInt());
        newSongIntent.putExtra(ServiceHelper.KEY_LOOPING, mLooping);
        newSongIntent.putExtra(ServiceHelper.KEY_SHUFFLNG, mShuffling);
        sendBroadcast(newSongIntent);
    }

    public void activityRebootAccessSongInfo() {
        //need to check the states before sending song information back to the fragment
        if (mState == STATE_PAUSED || mState == STATE_PLAYBACK_COMPLETED || mState == STATE_STARTED ||
                mState == STATE_PREPARED) {
            Intent newSongIntent = new Intent();
            newSongIntent.setAction(ServiceHelper.KEY_ACTION_NEW_SONG);
            newSongIntent.putExtra(ServiceHelper.KEY_TITLE, songsToPlay.get(currentSongIndex).getTitle());
            newSongIntent.putExtra(ServiceHelper.KEY_AUTHOR, songsToPlay.get(currentSongIndex).getAuthor());
            newSongIntent.putExtra(ServiceHelper.KEY_IMAGE_LOCATION, songsToPlay.get(currentSongIndex).getMusicArtInt());
            newSongIntent.putExtra(ServiceHelper.KEY_SHUFFLNG, mShuffling);
            newSongIntent.putExtra(ServiceHelper.KEY_LOOPING, mLooping);
            sendBroadcast(newSongIntent);
        }
    }

    public void stopAllSongBeingDisplayed() {
        currentSongIndex = 0;
        Intent stopIntent = new Intent();
        stopIntent.setAction(ServiceHelper.KEY_STOPUI);
        sendBroadcast(stopIntent);
    }

    //Check Box methods to handle looping and shuffling
    public void setLooping(boolean isLooping) {
        //all states except error are excepted.
        //mediaPlayer.setLooping(isLooping); // stupid!
        mLooping = isLooping;
    }

    public void setShuffle(boolean isShufflePlay) {
        mShuffling = isShufflePlay;
        //going to set the current song to the begining of the list regardless of where they are they must start over
        //but this will make it start at 1 unless shuffle is clicked before the play
        currentSongIndex = 0;
        if (mShuffling) {
            shuffle();
        } else {
            songsToPlay = puller.getMediafiles();
        }
    }

    private void shuffle() {
        int n = songsToPlay.size();
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            int change = i + rand.nextInt(n - i);
            MediaData temp = songsToPlay.get(i);
            songsToPlay.set(i, songsToPlay.get(change));
            songsToPlay.set(change, temp);
        }
    }

    //This is to deal with the pending intents from the notification
    public class BroadCastServiceReciever extends BroadcastReceiver {
        BroadCastServiceReciever() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ServiceHelper.KEY_SKIP_FORWARD)) {
                skipForward();
            } else if (intent.getAction().equals(ServiceHelper.KEY_PREVIOUS)) {
                returnPrevious();
            }
        }
    }
}
