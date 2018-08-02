package com.icypicks.www.icypicks.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.icypicks.www.icypicks.R;
import com.icypicks.www.icypicks.database.IceCreamContract;
import com.icypicks.www.icypicks.java_classes.IceCream;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MustTryIceCreamAdapter extends RecyclerView.Adapter<MustTryIceCreamAdapter.MustTryIceCreamViewHolder>{

    private Context context;
    private ArrayList<IceCream> mustTryIceCreams;

    public MustTryIceCreamAdapter(Context context, ArrayList<IceCream> mustTryIceCreams){
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
//        Glide.with(context).load(mustTryIceCreams.get(position).getImageUrl()).into(holder.iceCreamImageView);

        byte[] imageBytes = mustTryIceCreams.get(position).getImageBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//        Bitmap bitmap = ((BitmapDrawable) holder.iceCreamImageView.getDrawable()).getBitmap();

        holder.iceCreamImageView.setImageBitmap(bitmap);
//        Glide.with(context).load(imageBytes).into(holder.iceCreamImageView);

        holder.iceCreamImageView.setOnClickListener(view -> {
//            Bitmap bitmap = ((BitmapDrawable) holder.iceCreamImageView.getDrawable()).getBitmap();
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(DetailActivity.INTENT_IMAGE_EXTRA, bitmap);
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
