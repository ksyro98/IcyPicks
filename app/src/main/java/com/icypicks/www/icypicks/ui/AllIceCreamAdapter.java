package com.icypicks.www.icypicks.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.icypicks.www.icypicks.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This adapter is used to bind the recycler view with all the ice creams posted.
 * Ice cream's information are being retrieved from the firebase storage in the onBindView method
 */
public class AllIceCreamAdapter extends RecyclerView.Adapter<AllIceCreamAdapter.AllIceCreamViewHolder> {

    private Context context;
    private int numberOfImages;

    AllIceCreamAdapter(Context context, int numberOfImages) {
        this.context = context;
        this.numberOfImages = numberOfImages;
    }

    @NonNull
    @Override
    public AllIceCreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.ice_cream_recycler_view_item_1;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutIdForListItem, parent, false);
        return new AllIceCreamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllIceCreamViewHolder holder, int position) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        holder.iceCreamImageView.setEnabled(false);
        if(firebaseUser != null) {
            StorageReference storageReference = firebaseStorage.getReference().child(String.valueOf(numberOfImages-position)).child("image.jpg");
            storageReference.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(context.getApplicationContext()).load(uri).thumbnail(Glide.with(context.getApplicationContext()).load(R.drawable.progress_bar_placeholder)).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                holder.iceCreamImageView.setEnabled(false);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                holder.iceCreamImageView.setEnabled(true);
                                return false;
                            }
                        }).into(holder.iceCreamImageView);
                    })
                    .addOnFailureListener(Throwable::printStackTrace);
        }

        holder.iceCreamImageView.setOnClickListener(view -> {
            if(holder.iceCreamImageView.getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) holder.iceCreamImageView.getDrawable()).getBitmap();
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
                intent.putExtra(DetailActivity.INTENT_POSITION_EXTRA, numberOfImages - position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return numberOfImages+1;
    }

    void setNumberOfImages(int numberOfImages){
        this.numberOfImages = numberOfImages;
        this.notifyDataSetChanged();
    }

    class AllIceCreamViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.ice_cream_image_view)
        ImageView iceCreamImageView;

        AllIceCreamViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
