package com.yqtec.baidu_tts.listener;

import android.os.Handler;

public class UiMessageListener extends MessageListener {
    private Handler mHandler;

    public UiMessageListener(Handler handler) {
        mHandler = handler;
    }
    /**
     * 合成数据和进度的回调接口，分多次回调。
     * 注意：progress表示进度，与播放到哪个字无关
     * @param utteranceId
     * @param data 合成的音频数据。该音频数据是采样率为16K，2字节精度，单声道的pcm数据。
     * @param progress 文本按字符划分的进度，比如:你好啊 进度是0-3
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {
        super.onSynthesizeDataArrived(utteranceId, data, progress);
        if (mHandler!=null){
            mHandler.sendMessage(mHandler.obtainMessage(UI_CHANGE_SYNTHES_TEXT_SELECTION,progress,0));
        }
    }
    /**
     * 播放进度回调接口，分多次回调
     * 注意：progress表示进度，与播放到哪个字无关
     *
     * @param utteranceId
     * @param progress 文本按字符划分的进度，比如:你好啊 进度是0-3
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {
        if (mHandler!=null){
            mHandler.sendMessage(mHandler.obtainMessage(UI_CHANGE_INPUT_TEXT_SELECTION, progress, 0));
        }
    }
}
