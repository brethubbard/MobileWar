package edu.weber.bhubbard.cs3270.mobilewar.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import edu.weber.bhubbard.cs3270.mobilewar.Constants;
import edu.weber.bhubbard.cs3270.mobilewar.R;

public class ConnectBluetoothActivity extends AppCompatActivity {

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    private BluetoothAdapter mBtAdapter;

    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private Button btnScan;
    private Button btnDiscoverable;

    @Override
    public void onBackPressed() {
        //Force user to exit app or choose device to connect to
        //super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Must be called before onCreate
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_connect_bluetooth);

        setResult((Activity.RESULT_CANCELED));

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        btnScan = (Button) findViewById(R.id.btnScan);
        btnDiscoverable = (Button) findViewById(R.id.btnMakeDiscoverable);

        if (mBtAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            btnDiscoverable.setVisibility(View.GONE);
        }

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                discoverDevices();
                view.setVisibility(View.GONE);
            }
        });

        btnDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeDiscoverable();
            }
        });

        ArrayAdapter<String> pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemLongClickListener(mDevicesLongClickListener);
        pairedListView.setOnItemClickListener(mDevicesClickListener);

        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemLongClickListener(mDevicesLongClickListener);
        newDevicesListView.setOnItemClickListener(mDevicesClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(receiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        this.registerReceiver(receiver, filter);

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        if(pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for(BluetoothDevice device : pairedDevices) {
                pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesArrayAdapter.add(getString(R.string.no_devices_paired));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(receiver);
    }

    private void makeDiscoverable() {
        Log.d("test", "makeDiscoverable()");
        if (mBtAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void discoverDevices() {
        Log.d("test", "discoverDevices()");

        setProgressBarIndeterminateVisibility(true);
        setTitle("Scanning for devices");

        mNewDevicesArrayAdapter.clear();
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDevicesClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Toast.makeText(ConnectBluetoothActivity.this, "Long press to connect to device.", Toast.LENGTH_LONG).show();
        }
    };

    private AdapterView.OnItemLongClickListener mDevicesLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            if (    !((TextView) view).getText().toString().equals(R.string.no_devices_found) &&
                    !((TextView) view).getText().toString().equals(R.string.no_devices_paired)) {
                mBtAdapter.cancelDiscovery();

                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                setResult(Activity.RESULT_OK, intent);
                finish();
                return true;
            } else {
                return false;
            }
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    mNewDevicesArrayAdapter.add(getString(R.string.no_devices_found));
                }
                btnScan.setVisibility(View.VISIBLE);
            } else if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                if(!(intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0) == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)) {
                    btnDiscoverable.setVisibility(View.VISIBLE);
                } else
                {
                    btnDiscoverable.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connect_bluetooth, menu);
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
        } else if (id == R.id.action_discoverable) {
            makeDiscoverable();
        } else if (id == R.id.change_role) {
            MainActivity.ROLE = Constants.ROLE_HOST;
            finish();
        } else if (id == R.id.exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
