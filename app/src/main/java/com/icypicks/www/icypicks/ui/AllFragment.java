package com.icypicks.www.icypicks.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icypicks.www.icypicks.R;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllFragment extends Fragment {

    @BindView(R.id.all_ice_creams_recycler_view)
    RecyclerView allRecyclerView;

    private AllIceCreamAdapter allIceCreamAdapter;

    public AllFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        ButterKnife.bind(this, view);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        allRecyclerView.setLayoutManager(layoutManager);
        allRecyclerView.setHasFixedSize(false);

        allRecyclerView.setAdapter(allIceCreamAdapter);

        return view;
    }

    public void setAllIceCreamAdapter(AllIceCreamAdapter allIceCreamAdapter){
        this.allIceCreamAdapter = allIceCreamAdapter;
    }
}
