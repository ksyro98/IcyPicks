package com.icypicks.www.icypicks.ui;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icypicks.www.icypicks.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.RecyclerView.NO_POSITION;


/**
 * This fragment is used to show all ice creams posted in a recycler view.
 */
public class AllFragment extends Fragment {

    @BindView(R.id.all_ice_creams_recycler_view)
    RecyclerView allRecyclerView;

    private AllIceCreamAdapter allIceCreamAdapter;
    private GridLayoutManager layoutManager;
    private static int currentPosition;

    private static final String TAG = AllFragment.class.getSimpleName();

    public AllFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        ButterKnife.bind(this, view);

        layoutManager = new GridLayoutManager(getActivity(), 2);
        allRecyclerView.setLayoutManager(layoutManager);
        allRecyclerView.setHasFixedSize(false);
        allRecyclerView.setAdapter(allIceCreamAdapter);

        //delay needed for this to work
        new Handler().postDelayed(()-> {
            if (currentPosition != NO_POSITION) {
                allRecyclerView.smoothScrollToPosition(currentPosition);
            }
        }, 100);

        return view;
    }

    public void setAllIceCreamAdapter(AllIceCreamAdapter allIceCreamAdapter){
        this.allIceCreamAdapter = allIceCreamAdapter;
    }

    @Override
    public void onPause() {
        super.onPause();
        currentPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        if(currentPosition == NO_POSITION){
            currentPosition = layoutManager.findLastVisibleItemPosition();
        }
    }
}
