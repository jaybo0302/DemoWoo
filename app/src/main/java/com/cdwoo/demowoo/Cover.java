package com.cdwoo.demowoo;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.danikula.videocache.HttpProxyCacheServer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by cd on 2019/1/24.
 */

public class Cover extends ConstraintLayout {
    private int status = 0;
    private String url;
    private ImageView backView;
    public ImageView statusView;
    private TextView title;
    private MediaPlayer mp;
    public String coverName;
    private static int retryTime = 0;
    public Cover(Context context) {
        super(context);
        init(context, null);
    }
    public Cover(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Cover(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs){
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CoverStyle);
        coverName = ta.getString(R.styleable.CoverStyle_backName);
        url = ta.getString(R.styleable.CoverStyle_url);
        HttpProxyCacheServer proxy = HttpProxyCacheUtil.getAudioProxy(context);
        //url = proxy.getProxyUrl(url);
        backView = new ImageView(context);
        backView.setImageDrawable(context.getResources().getDrawable(getDrawResourceID(ta.getString(R.styleable.CoverStyle_backName))));
        title = new TextView(context);
        title.setText(ta.getString(R.styleable.CoverStyle_title));
        title.setVisibility(INVISIBLE);
        title.setTextSize(18);
        title.setTextColor(Color.WHITE);
        statusView = new ImageView(context);
        statusView.setImageDrawable(context.getResources().getDrawable(getDrawResourceID("start")));
        statusView.setVisibility(INVISIBLE);

        //设置标题参数
        LayoutParams titleParam = this.generateDefaultLayoutParams();
        titleParam.matchConstraintPercentHeight = 0.16f;
        titleParam.matchConstraintPercentWidth = 1f;
        titleParam.topToTop = 0;
        titleParam.startToStart = 0;

        //设置背景参数
        LayoutParams backParam = this.generateDefaultLayoutParams();
        backParam.width = 0;
        backParam.height = 0;
        backParam.topToTop = 0;
        backParam.bottomToBottom = 0;
        backParam.startToStart = 0;
        backParam.endToEnd = 0;
        backParam.matchConstraintPercentHeight = 0.99f;
        backParam.matchConstraintPercentWidth = 0.99f;

        //设置启停图标参数
        LayoutParams statusParam = this.generateDefaultLayoutParams();
        statusParam.width = 0;
        statusParam.height = 0;
        statusParam.topToTop = 0;
        statusParam.bottomToBottom = 0;
        statusParam.startToStart = 0;
        statusParam.endToEnd = 0;
        statusParam.matchConstraintPercentWidth = 0.38f;
        statusParam.matchConstraintPercentHeight = 0.38f;
        this.addView(backView, backParam);
        this.addView(title, titleParam);
        this.addView(statusView, statusParam);

        backView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        title.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        title.setVisibility(View.INVISIBLE);
                        if (status == 0) {
                            if (Constants.currentPlay != null) {
                                Constants.currentPlay.stop();
                            }
                            retryTime = 0;
                            Constants.currentStatus = 1;
                            Constants.currentPlay = Cover.this;
                            statusView.setImageDrawable(getContext().getResources().getDrawable(getDrawResourceID("start")));
                            statusView.setVisibility(VISIBLE);
                            retryTime = 0;
                            start();
                        } else if(status ==1){
                            statusView.setImageDrawable(getContext().getResources().getDrawable(getDrawResourceID("stop")));
                            statusView.setVisibility(VISIBLE);
                            Constants.currentStatus = 0;
                            stop();
                        }
                        Message msg = Message.obtain();
                        msg.what = 1;
                        msg.obj = Constants.currentPlay;
                        Constants.myHandler.sendMessage(msg);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
    //开始播放音乐操作
    public void start() {
        status = 1;
        if (mp != null) {
            mp.release();
            mp = null;
        }
        mp = new MediaPlayer();
        mp.stop();
        mp.reset();
        try {
            //设置音乐源
            mp.setDataSource(Constants.activity, Uri.parse(url));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //流方式进行音乐源输入
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //异步载入
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                //保持唤醒锁
                mp.setWakeMode(Constants.activity, PowerManager.PARTIAL_WAKE_LOCK);
            }
        });
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                if (retryTime < 5) {
                    retryTime++;
                    System.err.println("第 " + retryTime + " 次尝试重启播放");
                    start();
                } else {
                    stop();
                }
                return false;
            }
        });
        mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {

            }
        });
        mp.setOnInfoListener(new MediaPlayer.OnInfoListener(){
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                System.err.println("on info " + i + " " + i1);
                return false;
            }
        });

    }
    //停止播放音乐操作
    public void stop(){
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
        status = 0;
    }

    public void shutdown() {
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }
    }

    /**
     * 根据图片的名称获取对应的资源id
     * @param resourceName
     * @return
     */
    public int getDrawResourceID(String resourceName) {
        return getResources().getIdentifier(resourceName,"drawable",getContext().getPackageName());
    }
}
