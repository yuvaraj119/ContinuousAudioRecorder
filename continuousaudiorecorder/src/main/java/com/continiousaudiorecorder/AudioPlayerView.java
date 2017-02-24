package com.continiousaudiorecorder;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Yuvaraj on 2/23/2017.
 */
public class AudioPlayerView extends LinearLayout implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private Context mContext;

    private Button mStartButton, mPauseButton, mPlayButton, mDiscardButton;
    private SeekBar mSeekBar;
    private TextView mStartTime, mEndTime;
    private LinearLayout mLlAudioRecorder;

    private Uri mAudioRecordUri;
    private String mActiveRecordFileName;

    public AudioRecorder mAudioRecorder;
    private int status = 0;

    // Media Player
    private MediaPlayer mp;
    // Handler to update Media UI timer, seekBar.
    private Handler mHandler = new Handler();
    // For calculating the recorded file time
    Utils utils;

    public AudioPlayerView(Context context) {
        super(context);
        init(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public AudioPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * Initialize the View and Audio Recorder
     */
    private void init(Context context) {
        mContext = context;
        View view = View.inflate(mContext, R.layout.audio_recorder_view, this);

        initAudioRecording();
        utils = new Utils();
        initMediaPlayer();

        mStartButton = (Button) view.findViewById(R.id.bt_start);
        mStartButton.setOnClickListener(mOnClickListener);
        mPauseButton = (Button) view.findViewById(R.id.bt_pause);
        mPauseButton.setOnClickListener(mOnClickListener);
        mPlayButton = (Button) view.findViewById(R.id.bt_play);
        mPlayButton.setOnClickListener(mOnClickListener);
        mDiscardButton = (Button) view.findViewById(R.id.bt_discard);
        mDiscardButton.setOnClickListener(mOnClickListener);
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mStartTime = (TextView) view.findViewById(R.id.tv_startTime);
        mEndTime = (TextView) view.findViewById(R.id.tv_endTime);
        mLlAudioRecorder = (LinearLayout) view.findViewById(R.id.ll_audio_recorder);

        invalidateViews();

    }

    /**
     * Initialize the Audio Recording
     */
    private void initAudioRecording() {
        mAudioRecorder = getRecorder();
        if (mAudioRecorder == null || mAudioRecorder.getStatus() == AudioRecorder.Status.STATUS_UNKNOWN) {
            mAudioRecorder = AudioRecorderBuilder.with(mContext)
                    .fileName(getNextFileName())
                    .config(AudioRecorder.MediaRecorderConfig.DEFAULT)
                    .loggable()
                    .build();
            setRecorder(mAudioRecorder);
        }
    }

    /**
     * Initialize the media player for playing the recorded audio
     */
    private void initMediaPlayer() {
        // Media player
        mp = new MediaPlayer();
    }

    /**
     * Setting the audio recorder instance
     */
    private void setRecorder(@NonNull AudioRecorder recorder) {
        mAudioRecorder = recorder;
    }

    /**
     * Audio Recorder
     */
    private AudioRecorder getRecorder() {
        return mAudioRecorder;
    }

    /**
     * Create a file in dir before starting recording
     */
    private String getNextFileName() {

        String outputFile = Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + File.separator
                + "Record_"
                + System.currentTimeMillis()
                + ".m4a";

        File file = new File(outputFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return outputFile;
    }

    /**
     * Get recorded file path
     */
    public String getFilePath(){
        return mActiveRecordFileName;
    }

    /**
     * Update UI status
     */
    private void invalidateViews() {
        switch (mAudioRecorder.getStatus()) {
            case STATUS_UNKNOWN:
                mStartButton.setEnabled(true);
                mPauseButton.setEnabled(false);
                mPlayButton.setEnabled(false);
                mDiscardButton.setEnabled(false);
                initSeekBar();
                break;
            case STATUS_READY_TO_RECORD:
                mStartButton.setEnabled(true);
                mPauseButton.setEnabled(false);
                mPlayButton.setEnabled(false);
                mDiscardButton.setEnabled(false);
                initSeekBar();
                break;
            case STATUS_RECORDING:
                mStartButton.setEnabled(false);
                mPauseButton.setEnabled(true);
                mPlayButton.setEnabled(false);
                mDiscardButton.setEnabled(false);
                initSeekBar();
                break;
            case STATUS_RECORD_PAUSED:
                mStartButton.setEnabled(true);
                mPauseButton.setEnabled(false);
                mPlayButton.setEnabled(true);
                mDiscardButton.setEnabled(true);
                break;
            default:
                break;
        }
    }

    /**
     * Player seek bar
     */
    private void initSeekBar() {
        mEndTime.setText("00:00");
        mStartTime.setText("00:00");
        mSeekBar.setProgress(0);
    }

    /**
     * Try again after audio and external read write permission
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void tryStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int checkAudio = ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
            final int checkStorage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            final int checkReadStorage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (checkAudio != PackageManager.PERMISSION_GRANTED || checkStorage != PackageManager.PERMISSION_GRANTED ||
                    checkReadStorage != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(mContext, "Grant Permission to Continue", Toast.LENGTH_LONG).show();
                showNeedPermissionsMessage();
            } else {
                start();
            }
        } else {
            start();
        }
    }

    /**
     * Check audio and external read write permission
     */
    private void showNeedPermissionsMessage() {
        invalidateViews();
        message(mContext.getString(R.string.error_no_permissions));
    }

    private void message(String message) {
        final View root = mLlAudioRecorder;
        if (root != null) {
            final Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(android.R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    tryStart();
                }
            });
            snackbar.show();
        }
    }

    /**
     * Start recording audio
     */
    public void start() {
        initAudioRecording();
        mAudioRecorder.start(new AudioRecorder.OnStartListener() {
            @Override
            public void onStarted() {
                invalidateViews();
            }

            @Override
            public void onException(Exception e) {
                invalidateViews();
                message(mContext.getString(R.string.error_audio_recorder, e));
            }
        });
    }

    /**
     * Delete recorded audio
     */
    private void deleteFile() {
        if (mActiveRecordFileName != null) {
            File file = new File(mActiveRecordFileName);
            if (file.exists()) {
                file.delete();
            }
        }
        mAudioRecordUri = null;
    }

    /**
     * Pause recorded audio
     */
    public void pause() {
        mAudioRecorder.pause(new AudioRecorder.OnPauseListener() {
            @Override
            public void onPaused(String activeRecordFileName) {
                mActiveRecordFileName = activeRecordFileName;

                //setResult(Activity.RESULT_OK,
                        //new Intent().setData(Uri.parse(mActiveRecordFileName)));
                        //new Intent().setData(saveCurrentRecordToMediaDB(mActiveRecordFileName)));
                saveCurrentRecordToMediaDB(mActiveRecordFileName);
                invalidateViews();
            }

            @Override
            public void onException(Exception e) {
                //setResult(Activity.RESULT_CANCELED);
                invalidateViews();
                message(mContext.getString(R.string.error_audio_recorder, e));
            }
        });
    }

    /**
     * Discard recorded audio
     */
    public void discard() {
        if (mAudioRecorder.isRecording()) {
            mAudioRecorder.cancel();
        }
        deleteFile();
        //setResult(Activity.RESULT_CANCELED);
        status = 0;
        mStartButton.setText(getResources().getString(R.string.start));
        mAudioRecorder.setStatus(AudioRecorder.Status.STATUS_UNKNOWN);
        invalidateViews();
    }

    /**
     * Play recorded audio
     */
    public void play() {
        File file = new File(mActiveRecordFileName);
        if (file.exists()) {
            playAudio();
        }
        mDiscardButton.setEnabled(false);
        mStartButton.setEnabled(false);
    }

    /**
     * Creates new item in the system's media database.
     */
    protected Uri saveCurrentRecordToMediaDB(final String fileName) {
        if (mAudioRecordUri != null) return mAudioRecordUri;

        final Resources res = getResources();
        final ContentValues cv = new ContentValues();
        final File file = new File(fileName);
        final long current = System.currentTimeMillis();
        final long modDate = file.lastModified();
        final Date date = new Date(current);
        final String dateTemplate = res.getString(R.string.audio_db_title_format);
        final SimpleDateFormat formatter = new SimpleDateFormat(dateTemplate, Locale.getDefault());
        final String title = formatter.format(date);
        final long sampleLengthMillis = 1;
        // Lets label the recorded audio file as NON-MUSIC so that the file
        // won't be displayed automatically, except for in the playlist.
        cv.put(MediaStore.Audio.Media.IS_MUSIC, true);

        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        cv.put(MediaStore.Audio.Media.DATE_MODIFIED, (int) (modDate / 1000));
        cv.put(MediaStore.Audio.Media.DURATION, sampleLengthMillis);
        cv.put(MediaStore.Audio.Media.MIME_TYPE, "audio/*");
        cv.put(MediaStore.Audio.Media.ARTIST, res.getString(R.string.audio_db_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM, res.getString(R.string.audio_db_album_name));

        final ContentResolver resolver = mContext.getContentResolver();
        final Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        mAudioRecordUri = resolver.insert(base, cv);
        if (mAudioRecordUri == null) {
            return null;
        }
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, mAudioRecordUri));
        return mAudioRecordUri;
    }

    @Override
    protected void onDetachedFromWindow() {
        mp.release();
        mHandler.removeCallbacks(null);
        if (mAudioRecorder.isRecording()) {
            mAudioRecorder.cancel();
            //setResult(Activity.RESULT_CANCELED);
        }
        super.onDetachedFromWindow();
    }

    /**
     * Function to play a audio
     */
    private void playAudio() {
        // Play audio
        try {
            mp.reset();
            mp.setDataSource(mActiveRecordFileName);
            mp.prepare();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

            mp.setOnCompletionListener(this); // Important

            // set seekBar values
            mSeekBar.setProgress(0);
            mSeekBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            mEndTime.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            mStartTime.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating seekBar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            mSeekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mDiscardButton.setEnabled(true);
        mStartButton.setEnabled(true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mp.isPlaying()) {
            mHandler.removeCallbacks(mUpdateTimeTask);
            int totalDuration = mp.getDuration();
            int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

            // forward or backward to certain seconds
            mp.seekTo(currentPosition);

            // update timer progress again
            updateProgressBar();
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mStartButton) {
                v.setEnabled(false);
                if (status == 0) {
                    mStartButton.setText(getResources().getString(R.string.resume));
                } else {
                    mStartButton.setText(getResources().getString(R.string.start));
                }
                tryStart();
            } else if (v == mPauseButton) {
                v.setEnabled(false);
                pause();
            } else if (v == mPlayButton) {
                play();
            } else if (v == mDiscardButton) {
                discard();
            }
        }
    };


}
