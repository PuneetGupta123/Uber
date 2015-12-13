package dell.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.tv.TvInputService;
import android.os.AsyncTask;
import android.service.textservice.SpellCheckerService;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.uber.sdk.android.rides.RequestButton;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.rides.client.Session;
import com.uber.sdk.rides.client.UberRidesService;
import com.uber.sdk.rides.client.UberRidesServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Permission;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class TestUberApp extends ActionBarActivity implements LocationListener{
    protected LocationManager locationManager;
    Context context = this;
    Location location,location1;
    double latitude;
    double longitude;
    String TAG="TestUberApp";

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;
    private static final long MIN_DISTANCE_FOR_UPDATE = 500; //500 meter
    private static final long MIN_TIME_FOR_UPDATE = 1000 * 60 * 1; //2 minutes
    Geocoder geocoder;
    List<Address> addresses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_uber_app);
        RelativeLayout layout=(RelativeLayout)findViewById(R.id.RelativeLayout);
        Session session = new Session.Builder().setServerToken("maMItAGDV5HHqKAG3nLfLTlhMOnQscAONF_aeFcu").setEnvironment(Session.Environment.SANDBOX).build();
        UberRidesService service = UberRidesServices.createSync(session);
        final RequestButton requestButton = new RequestButton(this);
        location1=getLocation();
        requestButton.setClientId("eFrzgz_2Du2KYUXIi3MKaNOWtxo3i77K");
        if(location1!=null)
        {

            RideParameters rideParams = new RideParameters.Builder()
                    .setPickupLocation((float)location1.getLatitude(),(float)location1.getLongitude(),"","")
                    .build();
            requestButton.setRideParameters(rideParams);

        }


        layout.addView(requestButton);
    }



    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                this.canGetLocation = false;
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {

                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_FOR_UPDATE,
                            MIN_DISTANCE_FOR_UPDATE, this);
                    Log.d(TAG, "Network");

                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                } else {
                    // if GPS Enabled and location from cell is null then get lat/long using GPS Services
                    if (isGPSEnabled) {
                        if (location == null) {
                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    MIN_TIME_FOR_UPDATE,
                                    MIN_DISTANCE_FOR_UPDATE, this);
                            Log.d(TAG, "GPS Enabled");

                            if (locationManager != null) {
                                location = locationManager
                                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                if (location != null) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        }
                    }
                }
                if (latitude == 0.0 && longitude == 0.0) {
                    Toast.makeText(context, "Wait", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void getRideEstimates()
    {
        MyVolley.init(context);
        RequestQueue queue = MyVolley.getRequestQueue();
        StringRequest myReq = new StringRequest(Request.Method.GET,"https://api.uber.com/v1/estimates/price"
                , reqSuccessListenerFoodList(), reqErrorListenerFoodList()) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("server_token", "maMItAGDV5HHqKAG3nLfLTlhMOnQscAONF_aeFcu");
                return headers;
            }

            protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("start_latitude", String.valueOf((float)28.613939));
                params.put("start_longitude",String.valueOf((float)77.209021));
                params.put("end_latitude", String.valueOf((float)28.7499));
                params.put("end_longitude", String.valueOf((float)77.1170));

                return params;
            }
        };
        myReq.setRetryPolicy(new DefaultRetryPolicy(25000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myReq);
    }

    private Response.Listener<String> reqSuccessListenerFoodList() {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG,"in volley success");
                Log.d(TAG, "Response" + response);
            }
        };
    }

    private Response.ErrorListener reqErrorListenerFoodList() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"in volley error");
                Log.d(TAG, error.toString());
            }
        };
    }










    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_uber_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_get_ride_estimates) {
            getRideEstimates();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
