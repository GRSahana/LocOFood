package com.SahanaProjects.loc_o_food.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.SahanaProjects.loc_o_food.Helper.CheckConnectivity;
import com.SahanaProjects.loc_o_food.Helper.ProgressDialog;
import com.SahanaProjects.loc_o_food.R;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import pl.pawelkleczkowski.customgauge.CustomGauge;

public class CompostFragment extends Fragment implements MqttCallback {

    private static final String TAG = "CompostFragment";
    CustomGauge progressBarMoist, progressBarTemp;
    CheckConnectivity checkConnectivity;
    boolean connect = false;
    TextView tvProgressValue, tvProgressTemp;
    MqttAndroidClient client;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_compost, container, false);
        progressBarMoist = view.findViewById(R.id.compost_circular);
        tvProgressValue = view.findViewById(R.id.progress_compost);
        progressBarMoist.setValue(0);
        progressBarTemp = view.findViewById(R.id.temp);
        tvProgressTemp = view.findViewById(R.id.progress_temp);
        progressBarTemp.setValue(0);


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
                ProgressDialog.setProgressDialog(getContext());
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
                        ProgressDialog.hideProgressDialog();
                        Log.d(TAG, "onFailure");

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(getContext(), "Internet Connection Error", Toast.LENGTH_SHORT).show();
        }

            return view;

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
                    ProgressDialog.hideProgressDialog();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    ProgressDialog.hideProgressDialog();
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
        if(getContext()!=null)
        Toast.makeText(getContext(),"MQTT Connection Lost",Toast.LENGTH_LONG).show();
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMsg) {
        String mqttMessage = mqttMsg.toString();
        Log.d("Subdcribed :",s + mqttMsg);
        if(s.equals("Jubin_KK/feeds/compostmoisture")) {
            progressBarMoist.setValue(Math.round(Float.parseFloat(mqttMessage)));
            tvProgressValue.setText(mqttMessage);
        }
        if(s.equals("Jubin_KK/feeds/temperature")){
            Log.d("Subdcribed Temp:",s + mqttMsg);

            progressBarTemp.setValue(Math.round(Float.parseFloat(mqttMessage)));
            tvProgressTemp.setText(mqttMessage);
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
