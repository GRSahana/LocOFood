package com.ubikasoftwares.loc_o_food.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ubikasoftwares.loc_o_food.Helper.CheckConnectivity;
import com.ubikasoftwares.loc_o_food.Helper.ProgressDialog;
import com.ubikasoftwares.loc_o_food.MainActivity;
import com.ubikasoftwares.loc_o_food.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import pl.pawelkleczkowski.customgauge.CustomGauge;


public class PotFragment extends Fragment implements MqttCallback {

    private static final String TAG = "PotFragment";
    CustomGauge progressBar;
    int setProgress = 0;
    MqttAndroidClient client;
    CheckConnectivity checkConnectivity;
    boolean connect = false;
    TextView tvProgressValue;
    Switch pumpSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_pot, container, false);
        progressBar = view.findViewById(R.id.progress_circular);
        tvProgressValue = view.findViewById(R.id.progress_number);
        progressBar.setValue(0);

        pumpSwitch = view.findViewById(R.id.water_pump);

        String clientId = MqttClient.generateClientId();

        checkConnectivity = new CheckConnectivity();
        boolean con = checkConnectivity.checkNow(getContext());

        if(con) {

            client =
                    new MqttAndroidClient(getContext(), "tcp://io.adafruit.com:1883",
                            clientId);

            MqttConnectOptions options = null;
            options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            options.setConnectionTimeout(60);
            options.setKeepAliveInterval(120);
            options.setCleanSession(true);
            options.setUserName("Jubin_KK");
            options.setPassword("4add8f6c1b9a493b866e3181a7be7009".toCharArray());
            try {
                IMqttToken token = client.connect(options);
                client.setCallback(this);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Log.d(TAG, "onSuccess");
                        connect = true;
                        subscribeToATopic();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                      Log.d(TAG, "onFailure");

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

            pumpSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pumpSwitch.isChecked())
                        publishTopic("ON");
                    else
                        publishTopic("OFF");

                }
            });
        }

        return view;
    }

    private void publishTopic(String status) {

        String topic = "Jubin_KK/feeds/manualwatering";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = status.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeToATopic() {
        String topic = "Jubin_KK/feeds/#";
        int qos = 0;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    String Message = asyncActionToken.toString();
                    Log.d("Subdcribed :",Message);
                    Toast.makeText(getContext(),Message,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                   exception.printStackTrace();
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        //Toast.makeText(this,"MQTT Connection Lost",Toast.LENGTH_LONG).show();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMsg) {
        String mqttMessage = mqttMsg.toString();
        Log.d("Subdcribed :",s + mqttMsg);
        if(s.equals("Jubin_KK/feeds/soilmoisture")) {
            progressBar.setValue(Math.round(Float.parseFloat(mqttMessage)));
            tvProgressValue.setText(mqttMessage);
        }
        if(s.equals("Jubin_KK/feeds/manualwatering")){
            if(mqttMessage.equals("ON")){
                Log.d("Subdcribed1 :",s + mqttMsg);
                pumpSwitch.setChecked(true);
            }else if(mqttMessage.equals("OFF")){
                Log.d("Subdcribed2 :",s + mqttMsg);
                pumpSwitch.setChecked(false);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Toast.makeText(getContext(),"Delivery Completed",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(client.isConnected()){
            client.close();
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
