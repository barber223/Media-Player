//Eric Barber
//MDF3- 1804
//MediaPlayerFragment.java

package com.example.barber223.barbereric_ce02.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.barber223.barbereric_ce02.R;
import com.example.barber223.barbereric_ce02.ToFragmentInterface;

import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        SeekBar.OnSeekBarChangeListener, ToFragmentInterface {

    //A Variable to hold the seek bar,
    // don't want to keep finding it
    private SeekBar mSeekBar;
    private TextView msongPosition;
    private Timer timer = null;

    //interface to talk to the activity that called upon fragment
    public interface PlaybackCommandListener {
        void play();

        void pause();

        void stop();

        void skip();

        void previous();

        void shuffle(boolean _shuffle);

        void loop(boolean _looping);

        void seekBarWasAltered(int _newPosition);
    }

    private PlaybackCommandListener mListenr;

    public MediaPlayerFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlaybackCommandListener) {
            mListenr = (PlaybackCommandListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.media_player_fragment_layout, container, false);
    }

    //this is to set up the different functionality depending on the buttons that are pressed
    @Override
    public void onClick(View v) {
        if (mListenr == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.play_track_btn:
                mListenr.play();
                break;
            case R.id.stop_track_btn:
                mListenr.stop();
                break;
            case R.id.pause_track_btn:
                mListenr.pause();
                break;
            case R.id.skip_track_btn:
                mListenr.skip();
                break;
            case R.id.previous_track_btn:
                mListenr.previous();
                break;
        }
    }

    //this will allow me to update the service
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.loopingCheckBox:
                mListenr.loop(isChecked);
                break;
            case R.id.shuffleCheckBox:
                mListenr.shuffle(isChecked);
                break;
        }
    }

    //set the listeners for the views and objects on the UI
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View root = getView();
        //set the on click listeners for the buttons within the application
        if (root != null) {
            root.findViewById(R.id.play_track_btn).setOnClickListener(this);
            root.findViewById(R.id.stop_track_btn).setOnClickListener(this);
            root.findViewById(R.id.pause_track_btn).setOnClickListener(this);
            root.findViewById(R.id.skip_track_btn).setOnClickListener(this);
            root.findViewById(R.id.previous_track_btn).setOnClickListener(this);
            CheckBox boc = root.findViewById(R.id.shuffleCheckBox);
            boc.setOnCheckedChangeListener(this);
            boc = root.findViewById(R.id.loopingCheckBox);
            boc.setOnCheckedChangeListener(this);
            mSeekBar = root.findViewById(R.id.track_seek_bar);
            mSeekBar.setOnSeekBarChangeListener(this);
            msongPosition = root.findViewById(R.id.track_position);
        }
    }

    public void setUpSeekBar(int maxTime, int currentPostion, boolean playing) {
        final View v = getView();

        if (v != null) {
            if (playing) {
                TextView tv = v.findViewById(R.id.track_total_time);
                String length = convertToTime(maxTime);
                tv.setText(length);
                mSeekBar.setMax(maxTime);
                mSeekBar.setProgress(currentPostion);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //yucky
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI();
                            }
                        });
                    }
                }, 0, 1000);
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            }
        }
    }

    private void updateUI() {
        //If the SeekBar is held refferenc on the Main Thread
        int currentP = mSeekBar.getProgress();
        mSeekBar.setProgress(currentP + 1);
        if (msongPosition != null) {
            String postion = convertToTime(currentP + 1);
            msongPosition.setText(postion);
        }
    }

    //interface override method
    @Override
    public void passingSeekInformation(int maxTime, int _currentPosition, boolean _running) {
        setUpSeekBar(maxTime, _currentPosition, _running);
    }

    //interface override method
    @Override
    public void resetProgressBar() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        mSeekBar.setProgress(0);
        msongPosition.setText("0:00");
    }

    @Override
    public void setUIForNewImage(String _title, String _author, int _photoLocation) {
        View v = getView();
        if (v != null) {
            TextView tV = v.findViewById(R.id.track_title);
            tV.setText(_title);
            tV = v.findViewById(R.id.track_author);
            tV.setText(_author);
            ImageView iv = v.findViewById(R.id.image_view);
            iv.setImageDrawable(getResources().getDrawable(_photoLocation, null));
        }
    }

    @Override
    public void stopAllUI() {
        View v = getView();
        if (v != null) {
            TextView tV = v.findViewById(R.id.track_title);
            tV.setText("");
            tV = v.findViewById(R.id.track_author);
            tV.setText("");
            ImageView iv = v.findViewById(R.id.image_view);
            iv.setImageDrawable(null);
        }
    }

    @Override
    public void setUpCheckBoxes(Boolean _shuffling, Boolean _looping) {
        View v = getView();
        if (v != null){
            CheckBox cH = v.findViewById(R.id.loopingCheckBox);
            cH.setChecked(_looping);
            cH = v.findViewById(R.id.shuffleCheckBox);
            cH.setChecked(_shuffling);
        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //need to get the position to then update the service on the current location of the track being played
        int currentPosition = seekBar.getProgress();
        mListenr.seekBarWasAltered(currentPosition);
    }

    private String convertToTime(int time) {
        int totalMinutes = time / 60;
        int seconds = time - totalMinutes * 60;

        if (seconds < 10) {
            return String.valueOf(totalMinutes) + ":0" + String.valueOf(seconds);
        } else {
            return String.valueOf(totalMinutes) + ":" + String.valueOf(seconds);
        }
    }
}
