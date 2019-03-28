package com.cdwoo.demowoo;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class CDMainActivity extends AppCompatActivity {
    private ImageView disc;
    private ImageView playPageNeedle;
    private float mDiscRotation = 0;
    private float DISC_ROTATION_INCREASE = 1.98f;
    private WifiManager.WifiLock wifiLock;
    private ImageView currentCover;
    private long mExitTime = 0;
    private Runnable mRotationRunnable = new Runnable() {
        @Override
        public void run() {
            mDiscRotation += DISC_ROTATION_INCREASE;
            if (mDiscRotation >= 360) {
                mDiscRotation = 0;
            }
            disc.setRotation(mDiscRotation);
            currentCover.setRotation(mDiscRotation);
            Constants.myHandler.postDelayed(this, 10);
        }
    };
    public class MyHandler extends Handler {
        private final WeakReference<CDMainActivity> mActivity;
        public MyHandler(CDMainActivity activity) {
            mActivity = new WeakReference<CDMainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            final CDMainActivity currentActivity = mActivity.get();
            try{
                switch ((int)msg.what) {
                    //控制播放状态显示
                    case 1:
                        if (Constants.currentStatus == 1) {
                            this.removeCallbacks(mRotationRunnable);
                            this.post(mRotationRunnable);
                            playPageNeedle.setRotation(0);
                            playPageNeedle.setTranslationX(0);
                            playPageNeedle.setTranslationY(0);
                            wifiLock.acquire();
                            currentCover.setImageDrawable(currentActivity.getResources().getDrawable(getDrawResourceID(Constants.currentPlay.coverName)));
                            mDiscRotation = 0;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ((Cover)msg.obj).statusView.setVisibility(View.INVISIBLE);

                        } else {
                            this.removeCallbacks(mRotationRunnable);
                            if (wifiLock.isHeld()) {
                                wifiLock.release();
                            }
                            playPageNeedle.setTranslationX(-48);
                            playPageNeedle.setTranslationY(12);
                            playPageNeedle.setRotation(30);
                            currentCover.setImageDrawable(null);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            ((Cover)msg.obj).statusView.setVisibility(View.INVISIBLE);
                            Constants.currentPlay = null;
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cdmain);
        Constants.activity = this;
        currentCover = findViewById(R.id.currentCover);
        disc = findViewById(R.id.disc);
        playPageNeedle = findViewById(R.id.play_page_needle);
        Constants.myHandler = new MyHandler(this);
        wifiLock = ((WifiManager) this.getApplicationContext().
                getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {

                /**
                 * 网络可用的回调
                 * */
                @Override
                public void onAvailable(Network network) {
                    Log.e("netchange","onAvailable" );
                    if (Constants.currentPlay != null) {
                        Constants.currentPlay.start();
                    }
                    super.onAvailable(network);
                }

                /**
                 * 网络丢失的回调
                 * */
                @Override
                public void onLost(Network network) {
                    Log.e("netchange","onlost" );
                    if (Constants.currentPlay != null) {
                        Constants.currentPlay.shutdown();
                    }
                    super.onLost(network);
                }

                /**
                 * 当建立网络连接时，回调连接的属性
                 * */
                @Override
                public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
                    Log.e("netchange","onLinkPropertiesChanged" );
                    super.onLinkPropertiesChanged(network, linkProperties);
                }

                /**
                 *  按照官方的字面意思是，当我们的网络的某个能力发生了变化回调，那么也就是说可能会回调多次
                 *
                 *  之后在仔细的研究
                 * */
                @Override
                public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                    Log.e("netchange","onCapabilitiesChanged" );
                    super.onCapabilitiesChanged(network, networkCapabilities);
                }

                /**
                 * 在网络失去连接的时候回调，但是如果是一个生硬的断开，他可能不回调
                 * */
                @Override
                public void onLosing(Network network, int maxMsToLive) {
                    Log.e("netchange","onLosing" );
                    super.onLosing(network, maxMsToLive);
                }

                /**
                 * 按照官方注释的解释，是指如果在超时时间内都没有找到可用的网络时进行回调
                 * */
                @Override
                public void onUnavailable() {
                    Log.e("netchange","onUnavailable" );
                    super.onUnavailable();
                }

            });
        }
    }


    public int getDrawResourceID(String resourceName) {
        return getResources().getIdentifier(resourceName,"drawable",this.getPackageName());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出",Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                if (Constants.currentStatus == 1) {
                    if (Constants.currentPlay != null) {
                        Constants.currentPlay.stop();
                    }
                }
                System.exit(0);
            }
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }
}
