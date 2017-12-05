package edu.weber.bhubbard.cs3270.mobilewar.view;


import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import edu.weber.bhubbard.cs3270.mobilewar.R;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Card;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Deck;
import edu.weber.bhubbard.cs3270.mobilewar.gameobjects.Hand;


/**
 * A simple {@link Fragment} subclass.
 */
public class BoardFragment extends Fragment {

    private ImageView imgMyCard;
    private ImageView imgTheirCard;

    public static boolean tie = false;
    private Hand tieHand = new Hand();

    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_board, container, false);
        imgMyCard = (ImageView) rootView.findViewById(R.id.imgMyCard);
        imgTheirCard = (ImageView) rootView.findViewById(R.id.imgTheirCard);

        // Inflate the layout for this fragment
        return rootView;

    }

    public void updateCard(Card card, String user) {
        StringBuilder sb = new StringBuilder();
        switch (card.getSuit()) {
            case C:
                sb.append("c");
                break;
            case S:
                sb.append("s");
                break;
            case D:
                sb.append("d");
                break;
            case H:
                sb.append("h");
                break;
        }
        switch (card.getRank()) {
            case _1:
                sb.append("1");
                break;
            case _2:
                sb.append("2");
                break;
            case _3:
                sb.append("3");
                break;
            case _4:
                sb.append("4");
                break;
            case _5:
                sb.append("5");
                break;
            case _6:
                sb.append("6");
                break;
            case _7:
                sb.append("7");
                break;
            case _8:
                sb.append("8");
                break;
            case _9:
                sb.append("9");
                break;
            case _10:
                sb.append("10");
                break;
            case J:
                sb.append("j");
                break;
            case Q:
                sb.append("q");
                break;
            case K:
                sb.append("k");
                break;
        }
        MainActivity ma = (MainActivity) getActivity();
        int resource = ma.getResources().getIdentifier("drawable/" + sb.toString(), null, ma.getPackageName());

        if (user.equals("Me")) {
            imgMyCard.setVisibility(View.VISIBLE);
            imgMyCard.setImageResource(resource);
        } else {
            imgTheirCard.setVisibility(View.VISIBLE);
            imgTheirCard.setImageResource(resource);
        }

    }

    public void endOfRound(Card mine, Card theirs) {
        MainActivity ma = (MainActivity) getActivity();
        if(mine.CompareTo(theirs) > 0) {
            if (tie){
                HandFragment.hand.addAll(tieHand);
                Toast.makeText(getActivity(), (tieHand.size() + 2) + " cards added from ties!", Toast.LENGTH_SHORT).show();
                tieHand.clear();
                tie = false;
            }
            HandFragment.hand.add(mine);
            HandFragment.hand.add(theirs);
            Collections.shuffle(HandFragment.hand);
        } else if(mine.CompareTo(theirs) == 0) {
            tieHand.add(mine);
            tieHand.add(theirs);
            tie = true;
        } else {
            if (tie) {
                tie = false;
                Toast.makeText(getActivity(), (tieHand.size() + 2) + " cards lost from ties!", Toast.LENGTH_SHORT).show();
                tieHand.clear();
            }
        }
        ma.updateHandSize(HandFragment.hand.size());
        imgMyCard.setVisibility(View.INVISIBLE);
        imgTheirCard.setVisibility(View.INVISIBLE);
        ma.enablePlay();
    }

    public void placeCard(Card card, String user) {
        updateCard(card, user);
    }

    public void theirsVisible(boolean visible) {
        if (visible) {
            imgTheirCard.setVisibility(View.VISIBLE);
        } else {
            imgTheirCard.setVisibility(View.INVISIBLE);
        }
    }

    public void mineVisible(boolean visible) {
        if (visible) {
            imgTheirCard.setVisibility(View.VISIBLE);
        } else {
            imgTheirCard.setVisibility(View.INVISIBLE);
        }
    }
}
