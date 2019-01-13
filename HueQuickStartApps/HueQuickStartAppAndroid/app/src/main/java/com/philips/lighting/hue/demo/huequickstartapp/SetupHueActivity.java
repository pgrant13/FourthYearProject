package com.philips.lighting.hue.demo.huequickstartapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SetupHueActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener{

    private static final String TAG = "SetupHueActivity";
    private Bridge bridge;
    private BridgeDiscovery bridgeDiscovery;
    private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

    // UI elements
    private TextView statusTextView;
    private ListView bridgeDiscoveryListView;
    private TextView bridgeIpTextView;
    private View pushlinkImage;
    private Button bridgeDiscoveryButton;

    enum UIState {
        Idle,
        BridgeDiscoveryRunning,
        BridgeDiscoveryResults,
        Connecting,
        Pushlinking,
        Connected
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_hue);

        // Setup the UI
        statusTextView = (TextView)findViewById(R.id.status_text);
        bridgeDiscoveryListView = (ListView)findViewById(R.id.bridge_discovery_result_list);
        bridgeDiscoveryListView.setOnItemClickListener(this);
        bridgeIpTextView = (TextView)findViewById(R.id.bridge_ip_text);
        pushlinkImage = findViewById(R.id.pushlink_image);
        bridgeDiscoveryButton = (Button)findViewById(R.id.bridge_discovery_button);
        bridgeDiscoveryButton.setOnClickListener(this);

        // Connect to a bridge or start the bridge discovery
        String bridgeIp = getLastUsedBridgeIp();
        if (bridgeIp == null) {
            startBridgeDiscovery();
        } else {
            connectToBridge(bridgeIp);
        }
    }

    //---------------------------------------------Hue Default Methods----------------------------------------------------------

    /**
     * Use the KnownBridges API to retrieve the last connected bridge
     * @return Ip address of the last connected bridge, or null
     */
    private String getLastUsedBridgeIp() {
        List<KnownBridge> bridges = KnownBridges.getAll();

        if (bridges.isEmpty()) {
            return null;
        }

        return Collections.max(bridges, new Comparator<KnownBridge>() {
            @Override
            public int compare(KnownBridge a, KnownBridge b) {
                return a.getLastConnected().compareTo(b.getLastConnected());
            }
        }).getIpAddress();
    }

    /**
     * Start the bridge discovery search
     * Read the documentation on meethue for an explanation of the bridge discovery options
     */
    private void startBridgeDiscovery() {
        disconnectFromBridge();
        bridgeDiscovery = new BridgeDiscovery();
        // ALL Include [UPNP, IPSCAN, NUPNP] but in some nets UPNP and NUPNP is not working properly
        bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.ALL, bridgeDiscoveryCallback);
        updateUI(SetupHueActivity.UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");
    }

    /**
     * Stops the bridge discovery if it is still running
     */
    private void stopBridgeDiscovery() {
        if (bridgeDiscovery != null) {
            bridgeDiscovery.stop();
            bridgeDiscovery = null;
        }
    }

    /**
     * The callback that receives the results of the bridge discovery
     */
    private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
        @Override
        public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
            // Set to null to prevent stopBridgeDiscovery from stopping it
            bridgeDiscovery = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (returnCode == ReturnCode.SUCCESS) {
                        bridgeDiscoveryListView.setAdapter(new BridgeDiscoveryResultAdapter(getApplicationContext(), results));
                        bridgeDiscoveryResults = results;

                        updateUI(SetupHueActivity.UIState.BridgeDiscoveryResults, "Found " + results.size() + " bridge(s) in the network.");
                    } else if (returnCode == ReturnCode.STOPPED) {
                        Log.i(TAG, "Bridge discovery stopped.");
                    } else {
                        updateUI(SetupHueActivity.UIState.Idle, "Error doing bridge discovery: " + returnCode);
                    }
                }
            });
        }
    };

    /**
     * Use the BridgeBuilder to create a bridge instance and connect to it
     */
    private void connectToBridge(String bridgeIp) {
        stopBridgeDiscovery();
        disconnectFromBridge();

        bridge = new BridgeBuilder("app name", "device name")
                .setIpAddress(bridgeIp)
                .setConnectionType(BridgeConnectionType.LOCAL)
                .setBridgeConnectionCallback(bridgeConnectionCallback)
                .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
                .build();

        bridge.connect();

        bridgeIpTextView.setText("Bridge IP: " + bridgeIp);
        updateUI(SetupHueActivity.UIState.Connecting, "Connecting to bridge...");
    }

    /**
     * Disconnect a bridge
     * The hue SDK supports multiple bridge connections at the same time,
     * but for the purposes of this demo we only connect to one bridge at a time.
     */
    private void disconnectFromBridge() {
        if (bridge != null) {
            bridge.disconnect();
            bridge = null;
        }
    }

    /**
     * The callback that receives bridge connection events
     */
    private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
        @Override
        public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
            Log.i(TAG, "Connection event: " + connectionEvent);

            switch (connectionEvent) {
                case LINK_BUTTON_NOT_PRESSED:
                    updateUI(SetupHueActivity.UIState.Pushlinking, "Press the link button to authenticate.");
                    break;

                case COULD_NOT_CONNECT:
                    updateUI(SetupHueActivity.UIState.Connecting, "Could not connect.");
                    break;

                case CONNECTION_LOST:
                    updateUI(SetupHueActivity.UIState.Connecting, "Connection lost. Attempting to reconnect.");
                    break;

                case CONNECTION_RESTORED:
                    updateUI(SetupHueActivity.UIState.Connected, "Connection restored.");
                    break;

                case DISCONNECTED:
                    // User-initiated disconnection.
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
            for (HueError error : list) {
                Log.e(TAG, "Connection error: " + error.toString());
            }
        }
    };

    /**
     * The callback the receives bridge state update events
     */
    private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
        @Override
        public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
            Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);

            switch (bridgeStateUpdatedEvent) {
                case INITIALIZED:
                    // The bridge state was fully initialized for the first time.
                    // It is now safe to perform operations on the bridge state.
                    updateUI(SetupHueActivity.UIState.Connected, "Connected!");
                    break;

                case LIGHTS_AND_GROUPS:
                    // At least one light was updated.
                    break;

                default:
                    break;
            }
        }
    };

    //when the user clicks on the bridge they wish to connect to
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String bridgeIp = bridgeDiscoveryResults.get(i).getIP();

        connectToBridge(bridgeIp);
    }

    //when the user clicks on one of the buttons
    @Override
    public void onClick(View view) {
        if (view == bridgeDiscoveryButton) {
            startBridgeDiscovery();
        }
    }

    private void updateUI(final SetupHueActivity.UIState state, final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Status: " + status);
                statusTextView.setText(status);

                bridgeDiscoveryListView.setVisibility(View.GONE);
                bridgeIpTextView.setVisibility(View.GONE);
                pushlinkImage.setVisibility(View.GONE);
                bridgeDiscoveryButton.setVisibility(View.GONE);

                switch (state) {
                    case Idle:
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryRunning:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case BridgeDiscoveryResults:
                        bridgeDiscoveryListView.setVisibility(View.VISIBLE);
                        break;
                    case Connecting:
                        bridgeIpTextView.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case Pushlinking:
                        bridgeIpTextView.setVisibility(View.VISIBLE);
                        pushlinkImage.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                    case Connected:
                        statusTextView.setVisibility(View.VISIBLE);
                        bridgeDiscoveryButton.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }
}
