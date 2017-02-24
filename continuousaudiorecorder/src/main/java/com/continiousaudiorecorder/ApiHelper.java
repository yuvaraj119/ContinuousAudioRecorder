package com.continiousaudiorecorder;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Build;

/**
 * @author Yuvaraj
 * @since 2/25/17
 */
public final class ApiHelper {

    private ApiHelper() {
    }

    public static final boolean HAS_EXECUTE_ON_EXECUTOR_METHOD =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    @SuppressLint("InlinedApi")
    public static final int DEFAULT_AUDIO_ENCODER =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1
                    ? MediaRecorder.AudioEncoder.AAC
                    : MediaRecorder.AudioEncoder.DEFAULT;

}
