package com.yqtec.baidu_tts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.yqtec.baidu_tts.control.InitConfig;
import com.yqtec.baidu_tts.control.MySyntherizer;
import com.yqtec.baidu_tts.listener.MessageListener;
import com.yqtec.baidu_tts.utils.Logger;
import com.yqtec.baidu_tts.utils.OfflineResource;
import com.yqtec.services.BaiduTtsListenerCallback;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BaiduTtsService extends Service {
    protected String offlineVoice = OfflineResource.VOICE_MALE;
    protected String appId = "14322407";
    protected String appKey = "kjrZiyFpMomfSqzem4dUpuoY";
    protected String secretKey = "jtEDQLQSRdvpzsCcEzc11FaSXSsMeGn0";
    protected TtsMode ttsMode = TtsMode.MIX;
    protected MySyntherizer mSynthesizer;
    Map<String, String> params = new HashMap<String, String>();
    private BaiduTtsListenerCallback mBaiduTtsListenerCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        initBaiduTts();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e("baidutts", "服务死亡");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if (mSynthesizer != null) {
            mSynthesizer.release();
        }
        super.onDestroy();
    }

    private void initBaiduTts() {
        TtsCallback listener = new TtsCallback();
        params = getParams();
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        mSynthesizer = new MySyntherizer(this, initConfig, null); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "7");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(this, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Logger.i("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    private class TtsCallback extends MessageListener {
        /**
         * 播放开始，每句播放开始都会回调
         *
         * @param utteranceId
         */
        @Override
        public void onSpeechStart(String utteranceId) {
            super.onSynthesizeStart(utteranceId);
            if (mBaiduTtsListenerCallback != null) {
                try {
                    mBaiduTtsListenerCallback.onStart();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
         *
         * @param utteranceId
         */
        @Override
        public void onSpeechFinish(String utteranceId) {
            super.onSpeechFinish(utteranceId);
            if (mBaiduTtsListenerCallback != null) {
                try {
                    mBaiduTtsListenerCallback.onFinish();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MyBinder extends com.yqtec.services.BaiduTtsService.Stub {


        @Override
        public void speak(String s) throws RemoteException {
            if (mSynthesizer != null) {
                int result = mSynthesizer.speak(s);
                if (result != 0) {
                    Logger.e("error code :" + result + " method:speak , 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
                }
            }
        }

        @Override
        public void stop() throws RemoteException {
            int result = mSynthesizer.stop();
            if (result != 0) {
                Logger.e("error code :" + result + " method:stop , 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
            }
        }

        @Override
        public void pause() throws RemoteException {
            int result = mSynthesizer.pause();
            if (result != 0) {
                Logger.e("error code :" + result + " method:pause , 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
            }
        }

        @Override
        public void resume() throws RemoteException {
            int result = mSynthesizer.resume();
            if (result != 0) {
                Logger.e("error code :" + result + " method:resume , 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
            }
        }

        @Override
        public void batchSpeak() throws RemoteException {

        }

        @Override
        public void setParam(String s, String s1, String s2, String s3) throws RemoteException {
            // 以下参数均为选填
            // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
            params.put(SpeechSynthesizer.PARAM_SPEAKER, s);
            // 设置合成的音量，0-9 ，默认 5
            params.put(SpeechSynthesizer.PARAM_VOLUME, s1);
            // 设置合成的语速，0-9 ，默认 5
            params.put(SpeechSynthesizer.PARAM_SPEED, s2);
            // 设置合成的语调，0-9 ，默认 5
            params.put(SpeechSynthesizer.PARAM_PITCH, s3);
            params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
            // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
            // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
            // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

            // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
            OfflineResource offlineResource = createOfflineResource(offlineVoice);
            // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                    offlineResource.getModelFilename());
            if (mSynthesizer != null) {
                mSynthesizer.setParams(params);
            }
        }

        @Override
        public void setBaiduTtsListener(BaiduTtsListenerCallback baiduTtsListenerCallback) throws RemoteException {
            mBaiduTtsListenerCallback = baiduTtsListenerCallback;
        }
    }
}
