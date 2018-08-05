package com.icypicks.www.icypicks.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.database.IceCreamContract;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * This fragment is used to show the ice creams in the must-try list with a recycler viwe.
 * Those items are also saved in the SQLite database and they are selected by the user.
 * The ice creams are also retrieved from the database in this activity and they are loaded in an ArrayList.
 */
public class MustTryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MUST_TRY_LOADER = 2;

    @BindView(R.id.must_try_ice_cream_recycler_view)
    RecyclerView mustTryRecyclerView;

    private ArrayList<IceCream> mustTryIceCreams;
    private MustTryIceCreamAdapter mustTryIceCreamAdapter;
    private GridLayoutManager layoutManager;
    private static int currentPosition;


    public MustTryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_must_try, container, false);

        ButterKnife.bind(this, view);

        mustTryIceCreams = new ArrayList<>();
        mustTryIceCreamAdapter = new MustTryIceCreamAdapter(getContext(), mustTryIceCreams);

        layoutManager = new GridLayoutManager(getActivity(), 2);
        mustTryRecyclerView.setLayoutManager(layoutManager);
        mustTryRecyclerView.setHasFixedSize(false);
        mustTryRecyclerView.setAdapter(mustTryIceCreamAdapter);

        //delay needed for this to work
        new Handler().postDelayed(() -> {
            if(currentPosition != NO_POSITION) {
                mustTryRecyclerView.smoothScrollToPosition(currentPosition);
            }
        }, 100);

        loadData();

        return view;
    }

    public void loadData(){
        if(getActivity() != null) {
            LoaderManager loaderManager = getActivity().getSupportLoaderManager();
            Loader<Cursor> mustTryLoader = loaderManager.getLoader(MUST_TRY_LOADER);
            if(mustTryLoader == null){
                loaderManager.initLoader(MUST_TRY_LOADER, null, this);
            }
            else{
                loaderManager.restartLoader(MUST_TRY_LOADER, null, this);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        currentPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        if(getActivity() != null) {
            Toast.makeText(getActivity(), String.valueOf(currentPosition), Toast.LENGTH_SHORT).show();
        }
        if(currentPosition == NO_POSITION){
            currentPosition = layoutManager.findLastVisibleItemPosition();
        }
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncTaskLoader<Cursor>(getContext()) {
            @Override
            protected void onStartLoading() {
                forceLoad();
                super.onStartLoading();
            }

            @Nullable
            @Override
            public Cursor loadInBackground() {
                return getContext().getContentResolver().query(IceCreamContract.IceCreamEntry.CONTENT_URI, null, null, null, null);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data == null || !data.moveToFirst()){
            return;
        }

        for(int i=mustTryIceCreams.size()-1; i>=0; i--){
            mustTryIceCreams.remove(i);
        }

        do{
            String flavor = data.getString(data.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_FLAVOR));
            String place = data.getString(data.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_PLACE));
            String description = data.getString(data.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_DESCRIPTION));
            byte[] imageBytes = data.getBlob(data.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_IMAGE));
            int uploadNumber = data.getInt(data.getColumnIndex(IceCreamContract.IceCreamEntry.ICE_CREAM_UPLOAD_NUMBER));
            IceCream iceCream = new IceCream(flavor, place, description, null);
            iceCream.setUploadNumber(uploadNumber);
            iceCream.setImageBytes(imageBytes);
            mustTryIceCreams.add(iceCream);
        }while (data.moveToNext());

        mustTryIceCreamAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
