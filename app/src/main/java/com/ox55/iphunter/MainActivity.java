package com.ox55.iphunter;

import static rikka.shizuku.Shizuku.bindUserService;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    TextView localIpView;
    private Button huntIpBtn;

    private final int SHIZUKU_PERM_CODE = 100;

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;
        // Do stuff based on the result and the request code
    }

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        checkShizukuPermission(0);

        localIpView = findViewById(R.id.localIpTextView);
        localIpView.setText(getIpAccess());

        CountDownTimer countDownTimer = new CountDownTimer(Long.MAX_VALUE, 2500) {
            @Override
            public void onTick(long millisUntilFinished) {
                localIpView.setText(getIpAccess());
            }
            @Override
            public void onFinish() {
                // This will never be called
            }
        }.start();

        huntIpBtn = findViewById(R.id.huntingBtn);
        huntIpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkShizukuPermission(0);
            }
        });

        //if shizuku is enabled
        if (Shizuku.getUid() == 2000 || Shizuku.getUid() == 0) {
           //TODO
        }
    }

    private String getIpAccess() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        Network currentNetwork = connectivityManager.getActiveNetwork();
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(currentNetwork);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(currentNetwork);

        //if wifi, no hunting
        if (currentNetwork != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(currentNetwork);
            if (networkCapabilities != null) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                {
                    return "use cellular";
                }
            }
        }
        //gets IP address
        String ipAddress = null;
        try {
            ipAddress = String.valueOf(linkProperties.getLinkAddresses());
        } catch (NullPointerException e) {
            Log.d("IP NUll_POINTER", e.getMessage());
        }
        return ipAddress;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    private void checkShizukuPermission(int code) {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
        } else {
            // Request the permission
            Shizuku.requestPermission(code);
        }
    }
}

