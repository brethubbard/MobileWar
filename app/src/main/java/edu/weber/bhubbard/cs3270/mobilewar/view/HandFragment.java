package edu.weber.bhubbard.cs3270.mobilewar.view;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.weber.bhubbard.cs3270.mobilewar.Constants;
import edu.weber.bhubbard.cs3270.mobilewar.R;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Hand;


/**
 * A simple {@link Fragment} subclass.
 */
public class HandFragment extends Fragment {

    public static Hand hand;
    private ImageView imgHand;
    private TextView txvTapToPlay;
    private TextView txvCardsLeft;
    private Button btnDeal;
    private static boolean disableDealButton = false;
    private static boolean enableDealButton;

    public HandFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_hand, container, false);

        imgHand = (ImageView) rootView.findViewById(R.id.imgHand);
        imgHand.setEnabled(false);
        txvCardsLeft = (TextView) rootView.findViewById(R.id.txvCardsLeft);
        txvTapToPlay = (TextView) rootView.findViewById(R.id.txvTapToPlay);

        txvTapToPlay.setText(R.string.tap_to_play);

        btnDeal = (Button) rootView.findViewById(R.id.btnDeal);

        btnDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity ma = (MainActivity) getActivity();
                ma.DealCards();
            }
        });

        hand = new Hand();

        imgHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hand.size() > 0 && hand.size() < 52) {
                    imgHand.setEnabled(false);
                    MainActivity ma = (MainActivity) getActivity();
                    ma.sendCard(hand.getCard());
                } else if (hand.size() <= 0){
                    imgHand.setVisibility(View.INVISIBLE);
                    updateHandSize(0);
                    Toast.makeText(getActivity(), "You lose... Play again!", Toast.LENGTH_LONG).show();
                    txvTapToPlay.setVisibility(View.INVISIBLE);
                    btnDeal.setEnabled(true);
                } else {
                    imgHand.setVisibility(View.INVISIBLE);
                    updateHandSize(0);
                    Toast.makeText(getActivity(), "YOU WIN! Play again!", Toast.LENGTH_LONG).show();
                    txvTapToPlay.setVisibility(View.INVISIBLE);
                    btnDeal.setEnabled(true);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(disableDealButton || MainActivity.disableDealButton) {
            btnDeal.setVisibility(View.GONE);
        }
        if (enableDealButton || MainActivity.enableDealButton) {
            btnDeal.setVisibility(View.VISIBLE);
            btnDeal.setEnabled(true);
        }
    }


    public void getHand(Hand hand) {
        HandFragment.hand = hand;
        if (hand != null && hand.size() > 0) {
            imgHand.setImageResource(R.drawable.b1fv);
            updateHandSize(hand.size());
            btnDeal.setEnabled(false);
            imgHand.setEnabled(true);
        }
    }

    public void updateHandSize(int size){
        if (size > 0) {
            if(imgHand.getVisibility() != View.VISIBLE)
                imgHand.setVisibility(View.VISIBLE);
            txvCardsLeft.setText(size + " card(s) left.");
        } else {
            imgHand.setVisibility(View.INVISIBLE);
            imgHand.setEnabled(false);
            txvCardsLeft.setText("No cards left.");
        }
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        return byteArrayOutputStream.toByteArray();
    }

    public static Hand deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (Hand) objectInputStream.readObject();
    }

    public void disablePlay() {
        imgHand.setEnabled(false);
    }

    public void enablePlay() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Log.d("test", "Interrupted");
        }
        imgHand.setEnabled(true);
    }

    public void enableDealButton() {
        if (btnDeal != null) {
            btnDeal.setVisibility(View.VISIBLE);
            btnDeal.setEnabled(true);
        } else {
            enableDealButton = true;
        }
    }

    public void disableDealButton() {
        if (btnDeal != null) {
            btnDeal.setVisibility(View.GONE);
        } else {
            disableDealButton = true;
        }
    }
}
