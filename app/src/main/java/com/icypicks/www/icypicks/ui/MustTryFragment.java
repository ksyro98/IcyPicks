package com.icypicks.www.icypicks.ui;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.database.IceCreamContentProvider;
import com.icypicks.www.icypicks.database.IceCreamContract;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MustTryFragment extends Fragment {

    @BindView(R.id.must_try_ice_cream_recycler_view)
    RecyclerView mustTryRecyclerView;

    private ArrayList<IceCream> mustTryIceCreams;
    private MustTryIceCreamAdapter mustTryIceCreamAdapter;


    public MustTryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_must_try, container, false);

        ButterKnife.bind(this, view);

        mustTryIceCreams = new ArrayList<>();
        mustTryIceCreamAdapter = new MustTryIceCreamAdapter(getContext(), mustTryIceCreams);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        mustTryRecyclerView.setLayoutManager(layoutManager);
        mustTryRecyclerView.setHasFixedSize(false);
        mustTryRecyclerView.setAdapter(mustTryIceCreamAdapter);

        new IceCreamTask().execute();

        return view;
    }

//    public void setIceCreamAdapter(MustTryIceCreamAdapter mustTryIceCreamAdapter) {
//        this.mustTryIceCreamAdapter = mustTryIceCreamAdapter;
//    }


    class IceCreamTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //TODO add progressbar
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            if(getContext() != null) {
                return getContext().getContentResolver().query(IceCreamContract.IceCreamEntry.CONTENT_URI, null, null, null, null);
            }
            else{
                return null;
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            if(cursor == null || !cursor.moveToFirst()){
                return;
            }

            do{
                String flavor = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR));
                String place = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE));
                String description = cursor.getString(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION));
                byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE));
                int uploadNumber = cursor.getInt(cursor.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER));
                IceCream iceCream = new IceCream(flavor, place, description, null, null);
                iceCream.setUploadNumber(uploadNumber);
                iceCream.setImageBytes(imageBytes);
                mustTryIceCreams.add(iceCream);
            }while (cursor.moveToNext());

            mustTryIceCreamAdapter.notifyDataSetChanged();
        }
    }
}
