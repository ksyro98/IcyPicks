package com.icypicks.www.icypicks.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This adapter is used to bind the recycler view items with the ice creams in the must-try list.
 * The adapter has a field representing an ArrayList of ice creams and it gets its data from there.
 */
public class MustTryIceCreamAdapter extends RecyclerView.Adapter<MustTryIceCreamAdapter.MustTryIceCreamViewHolder>{

    private Context context;
    private ArrayList<IceCream> mustTryIceCreams;

    MustTryIceCreamAdapter(Context context, ArrayList<IceCream> mustTryIceCreams){
        this.context = context;
        this.mustTryIceCreams = mustTryIceCreams;
    }

    @NonNull
    @Override
    public MustTryIceCreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.ice_cream_recycler_view_item_1;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutIdForListItem, parent, false);
        return new MustTryIceCreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MustTryIceCreamViewHolder holder, int position) {

        byte[] imageBytes = mustTryIceCreams.get(position).getImageBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

        holder.iceCreamImageView.setImageBitmap(bitmap);

        holder.iceCreamImageView.setOnClickListener(view -> {
            File tempBitmapFile = new File(context.getCacheDir(), "tempBitmapFile.jpg");
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(tempBitmapFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.INTENT_IMAGE_EXTRA, tempBitmapFile.getAbsolutePath());
            intent.putExtra(DetailActivity.INTENT_POSITION_EXTRA, mustTryIceCreams.get(position).getUploadNumber());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        if(mustTryIceCreams != null){
            return mustTryIceCreams.size();
        }
        return 0;
    }

    class MustTryIceCreamViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.ice_cream_image_view)
        ImageView iceCreamImageView;

        MustTryIceCreamViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
