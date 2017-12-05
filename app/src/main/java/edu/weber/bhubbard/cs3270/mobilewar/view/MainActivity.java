package edu.weber.bhubbard.cs3270.mobilewar.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import edu.weber.bhubbard.cs3270.mobilewar.Constants;
import edu.weber.bhubbard.cs3270.mobilewar.R;
import edu.weber.bhubbard.cs3270.mobilewar.bluetoothcontroller.BluetoothTransferService;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Card;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Deck;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Hand;


public class MainActivity extends AppCompatActivity {

    public static String ROLE = null;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_DISCOVERABLE = 3;

    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothTransferService mTransferService = null;

    private Card mine;
    private Card theirs;
    public static boolean disableDealButton = false;
    public static boolean enableDealButton = false;
    public boolean vibrate = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (mTransferService != null) {
            if (mTransferService.getState() == BluetoothTransferService.STATE_NONE) {
                mTransferService.start();
            }
        }
        getFragmentManager().beginTransaction()
                .add(R.id.fragContainerTop, new BoardFragment(), "Board")
                .add(R.id.fragContainerBottom, new HandFragment(), "Hand")
                .commit();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.no_bluetooth), Toast.LENGTH_LONG).show();
            finish();
        }

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("Choose to 'Host' or 'Join' a game");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Host", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setupHost();
                alertDialog.dismiss();
                startGame();
                HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
                handFragment.enablePlay();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setupClient();
                alertDialog.dismiss();
                startGame();
                HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
                handFragment.disableDealButton();
            }
        });
        alertDialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTransferService != null) {
            mTransferService.stop();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void resetRole() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("Choose to 'Host' or 'Join' a game");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Host", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setupHost();
                alertDialog.dismiss();
                startGame();
                HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
                handFragment.enableDealButton();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Join", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setupClient();
                alertDialog.dismiss();
                startGame();
                HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
                handFragment.disableDealButton();
            }
        });
        alertDialog.show();
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
        } else if (id == R.id.action_vibrate) {
            item.setChecked(!item.isChecked());
            vibrate = item.isChecked();
        } else if (id == R.id.action_reset) {
            if (ROLE.equals(Constants.ROLE_HOST)) {
                final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                ad.setTitle("Are you sure?");
                ad.setMessage("This action will reset the game.");
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DealCards();
                    }
                });
                ad.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad.dismiss();
                    }
                });
                ad.show();
            } else {
                final AlertDialog ad = new AlertDialog.Builder(MainActivity.this).create();
                ad.setTitle("Invalid Action");
                ad.setMessage("Only the host can reset the game");
                ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ad.dismiss();
                    }
                });
                ad.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getFragmentManager().findFragmentByTag("Board") == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragContainerTop, new BoardFragment(), "Board")
                    .add(R.id.fragContainerBottom, new HandFragment(), "Hand")
                    .commit();
        }
        HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
        if (ROLE != null && ROLE.equals(Constants.ROLE_CLIENT)) {
            if (handFragment != null) {
                handFragment.disableDealButton();
            } else {
                disableDealButton = true;
            }
        } else if (ROLE != null && ROLE.equals(Constants.ROLE_HOST)) {
            if (handFragment != null) {
                handFragment.enableDealButton();
            } else {
                enableDealButton = true;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        getFragmentManager().beginTransaction()
                .remove(getFragmentManager().findFragmentByTag("Board"))
                .remove(getFragmentManager().findFragmentByTag("Hand"))
                .commit();
    }

    //Verifies BT is available and enabled.
    public void startGame() {
        if(ROLE != null) {
            if (ROLE.equals(Constants.ROLE_CLIENT)) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    setupGame();
                }
            } else {
                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE);
                } else {
                    if (mTransferService == null) {
                        mTransferService = new BluetoothTransferService(this, mHandler, ROLE);
                    }
                    connectDevice(null);
                }
            }
        }
    }


    //Initial setup for the game. Starts with getting the desired device to play with.
    private void setupGame() {
        if (ROLE != null) {
            if(ROLE.equals(Constants.ROLE_CLIENT)) {
                Intent serverIntent = new Intent(this, ConnectBluetoothActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        }
    }

    //Sets up roles
    public static void setupHost() {
        ROLE = Constants.ROLE_HOST;
    }

    public static void setupClient() {
        ROLE = Constants.ROLE_CLIENT;
    }


    //Send Hands
    public void sendLocalHand(Hand hand) {
        HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
        handFragment.getHand(hand);
    }

    public void sendRemoteHand(Hand hand) {
        try {
            mTransferService.write(hand);
        } catch (Exception e) {
            Log.e("test", "Error sending hand.");
        }
    }
//End Send Hands

    //Send Card methods
    public void sendCard(Card card) {
        sendRemoteCard(card);
        sendLocalCard(card);
    }

    public void sendLocalCard(Card card) {
        BoardFragment boardFragment = (BoardFragment) getFragmentManager().findFragmentByTag("Board");
        boardFragment.placeCard(card, "Me");
    }

    public void sendRemoteCard(Card card) {
        try {
            Hand hand = new Hand();
            hand.add(card);
            mine = card;
            mTransferService.write(hand);
            if (theirs != null) {
                displayWinner();
                mHandler.postDelayed(runnable, 2000);
            } else {
                HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
                handFragment.disablePlay();
            }
        } catch (Exception e) {
            Log.e("test", "Error sending card.");
        }
    }
//End Send Card methods

    //Receive Hand method
    private void receiveRemoteHand(Hand hand) {
        HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
        handFragment.getHand(hand);
    }
//End Receive Card Methods


    //Deal Cards for beginning hands
    public void DealCards() {
        if (mTransferService.getState() == BluetoothTransferService.STATE_CONNECTED) {
            Deck deck = new Deck();
            ArrayList<Hand> hands = deck.Deal();

            sendLocalHand(hands.get(0));
            sendRemoteHand(hands.get(1));
        }
    }

    public void displayWinner() {
        String winner;
        if (mine.CompareTo(theirs) > 0) {
            winner = "You won!";
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (v.hasVibrator() && vibrate) {
                long[] pattern = {0, 100, 200, 100};
                v.vibrate(pattern, -1);
            }
        } else if (mine.CompareTo(theirs) == 0) {
            winner = "Tie. Cards go to next winner!";
        } else {
            winner = "You lost.";
        }
        Toast.makeText(MainActivity.this, winner, Toast.LENGTH_SHORT).show();
    }

    //Enable onClickListener for Hand
    public void enablePlay() {
        HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
        handFragment.enablePlay();
    }

    //Processes response based on beginning hand or single card
    private void ProcessResponse(Hand hand) {
        if (hand.size() == 1) {
            theirs = hand.get(0);
            BoardFragment boardFragment = (BoardFragment) getFragmentManager().findFragmentByTag("Board");
            boardFragment.placeCard(theirs, "Them");
            boardFragment.theirsVisible(true);
            if (mine != null) {
                displayWinner();
                mHandler.postDelayed(runnable, 2000);
            }
        } else if (hand.size() > 1) {
            receiveRemoteHand(hand);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            endOfRound();
        }
    };

    private void endOfRound() {
        BoardFragment boardFragment = (BoardFragment) getFragmentManager().findFragmentByTag("Board");
        boardFragment.endOfRound(mine, theirs);
        mine = null;
        theirs = null;
    }

    //Sets the status of BT on title bar
    private void setStatus(CharSequence title) {
        setTitle("Mobile War - " + title);
    }

    //Connects devices based on their role.
    private void connectDevice(Intent data) {
        final BluetoothDevice device;
        if (data != null) {
            String address = data.getExtras().getString(ConnectBluetoothActivity.EXTRA_DEVICE_ADDRESS);
            device = mBluetoothAdapter.getRemoteDevice(address);
        } else {
            device = null;
        }
        if (ROLE.equals(Constants.ROLE_HOST)) {
            mTransferService.start();
        } else {
            AlertDialog ad = new AlertDialog.Builder(this).create();
            ad.setMessage("Verify the Host is Listening");
            ad.setCancelable(false);
            ad.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mTransferService.connect(device);
                }
            });
            ad.show();
        }
    }

    public void updateHandSize(int size) {
        HandFragment handFragment = (HandFragment) getFragmentManager().findFragmentByTag("Hand");
        if (handFragment != null) {
            handFragment.updateHandSize(size);
        }
    }

    //Handles results from activities for Result
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    if (mTransferService == null) {
                        mTransferService = new BluetoothTransferService(this, mHandler, ROLE);
                    }
                    connectDevice(data);
                } else {
                    ROLE = Constants.ROLE_HOST;
                    startGame();
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    if (mTransferService == null) {
                        startGame();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Bluetooth must be enabled.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case REQUEST_DISCOVERABLE:
                if(resultCode != Activity.RESULT_CANCELED) {
                    Toast.makeText(MainActivity.this, "Discoverable", Toast.LENGTH_LONG).show();
                }
                if (mTransferService == null) {
                    mTransferService = new BluetoothTransferService(this, mHandler, ROLE);
                }
                connectDevice(null);
                break;
        }
    }


    //Handler for BT communication when message is received.
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            byte[] readBuf;
            Hand readMessageHand;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothTransferService.STATE_CONNECTED:
                            setStatus("Connected");
                            break;
                        case BluetoothTransferService.STATE_CONNECTING:
                            setStatus("Connecting...");
                            break;
                        case BluetoothTransferService.STATE_LISTEN:
                            setStatus("Listening...");
                            break;
                        case BluetoothTransferService.STATE_NONE:
                            setStatus("Not Connected");
                            break;
                    }
                    break;
                case Constants.MESSAGE_READ:
                    Log.d("test", "Handler just recieved message: ");
                    readBuf = (byte[]) msg.obj;
                    readMessageHand = null;

                    try {
                        readMessageHand = Hand.deserialize(readBuf);
                    } catch (IOException e) {
                        Log.e("test", "IOException: " + e.getMessage());
                    } catch (ClassNotFoundException e) {
                        Log.e("test", "ClassNotFoundException: " + e.getMessage());
                    }

                    if (readMessageHand != null) {
                        Log.d("test", "Received Hand: " + readMessageHand.size());
                        ProcessResponse(readMessageHand);
                    } else
                        Log.d("test", "Hand is nothing");
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(MainActivity.this, "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(MainActivity.this, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    if (msg.getData().getString(Constants.TOAST).equals("Device connection was lost")) {
                        if (!isFinishing()) {
                            HandFragment.hand.clear();
                            updateHandSize(HandFragment.hand.size());
                            resetRole();
                        }
                    }
            }
        }
    };

}
