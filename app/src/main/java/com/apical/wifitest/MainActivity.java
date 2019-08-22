package com.apical.wifitest;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "WiFiTest";
    private EditText mTextSSID   = null;
    private EditText mTextPasswd = null;
    private Button   mBtnConnect = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextSSID   = (EditText)findViewById(R.id.txt_ssid   );
        mTextPasswd = (EditText)findViewById(R.id.txt_passwd );
        mBtnConnect = (Button  )findViewById(R.id.btn_connect);
        mTextSSID  .setText("Apical001_2.4G");
        mTextPasswd.setText("apicalgood"    );
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectWifi(MainActivity.this, mTextSSID.getText().toString(), mTextPasswd.getText().toString(), 2);
            }
        });
    }

    private String getWifiPassword(Context context, String ssid, int type) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            List<WifiConfiguration> savedConfigs = wm.getConfiguredNetworks();
            for (WifiConfiguration cfg : savedConfigs) {
                if (cfg != null && cfg.SSID.equals("\"" + ssid + "\"")) {
                    switch (type) {
                    case 1: return cfg.wepKeys[0];
                    case 2: return cfg.preSharedKey;
                    default: return "";
                    }
                }
            }
        }
        return "";
    }

    private void connectWifi(Context context, String ssid, String passwd, int type)
    {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wm == null) {
            Log.d(TAG, "failed to get wifi manager !");
            return;
        }

        WifiConfiguration config = null;
        List<WifiConfiguration> savedConfigs = wm.getConfiguredNetworks();
        for (WifiConfiguration cfg : savedConfigs) {
            if (cfg != null && cfg.SSID.equals("\"" + ssid + "\"")) {
                config = cfg;
                break;
            }
        }

        if (config == null) {
            config = new WifiConfiguration();
        }
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        switch (type) {
        case 0: // none
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            break;
        case 1: // wep
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + passwd + "\"";
            config.wepTxKeyIndex = 0;
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            break;
        case 2: // wpa
            config.hiddenSSID = true;
            config.preSharedKey = "\"" + passwd + "\"";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            break;
        }

        if (config.networkId != -1) {
            boolean ret = wm.removeNetwork(config.networkId);
            Log.d(TAG, "wm.removeNetwork, ret = " + ret);
        }
        wm.enableNetwork(wm.addNetwork(config), true);
    }
}
