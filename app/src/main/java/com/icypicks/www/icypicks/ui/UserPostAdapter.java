package com.icypicks.www.icypicks.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.icypicks.www.icypicks.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * This adapter is used to bind the recycler view in the user profile with the ice creams he uploaded.
 * The data are retrieved from the firebase storage in onBindView.
 * The selection of the items is being done from the contents of the uploadNumbers ArrayList
 * that contains the upload numbers of the ice creams the user has uploaded.
 */
public class UserPostAdapter extends RecyclerView.Adapter<UserPostAdapter.UserPostViewHolder>{

    private Context context;
    private ArrayList<Integer> uploadNumbers;

    UserPostAdapter(Context context, ArrayList<Integer> uploadNumbers){
        this.context = context;
        this.uploadNumbers = uploadNumbers;
    }

    @NonNull
    @Override
    public UserPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.ice_cream_recycler_view_item_2;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(layoutIdForListItem, parent, false);
        return new UserPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserPostViewHolder holder, int position) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null) {
            StorageReference storageReference = firebaseStorage.getReference(String.valueOf(uploadNumbers.get(position)));
            storageReference.child("image.jpg").getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(context.getApplicationContext()).load(uri).thumbnail(Glide.with(context.getApplicationContext()).load(R.drawable.progress_bar_placeholder)).into(holder.userPostIceCreamImageView))
                    .addOnFailureListener(Throwable::printStackTrace);

            File flavorFile = new File(context.getCacheDir(), "user_post_flavor.txt");
            storageReference.child("flavor.txt").getFile(flavorFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        try {
                            String flavor = readFile(flavorFile);
                            holder.userPostFlavorTextView.setText(flavor);
                            flavorFile.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                            holder.userPostFlavorTextView.setText(R.string.unable_to_load_flavor);
                            flavorFile.delete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        holder.userPostFlavorTextView.setText(R.string.unable_to_load_flavor);
                        flavorFile.delete();
                    });

            File descriptionFile = new File(context.getCacheDir(), "user_post_description.txt");
            storageReference.child("description.txt").getFile(descriptionFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        try {
                            String description = readFile(descriptionFile);
                            holder.userPostDescriptionTextView.setText(description);
                            descriptionFile.delete();
                        } catch (IOException e) {
                            e.printStackTrace();
                            holder.userPostDescriptionTextView.setText(R.string.unable_to_load_description);
                            descriptionFile.delete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        holder.userPostDescriptionTextView.setText(R.string.unable_to_load_description);
                        descriptionFile.delete();
                    });

            holder.userPostLinearLayout.setOnClickListener(view -> {
                Bitmap bitmap = ((BitmapDrawable) holder.userPostIceCreamImageView.getDrawable()).getBitmap();
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra(DetailActivity.INTENT_IMAGE_EXTRA, bitmap);
                intent.putExtra(DetailActivity.INTENT_POSITION_EXTRA, uploadNumbers.get(position));
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        if(uploadNumbers != null){
            return uploadNumbers.size();
        }
        return 0;
    }

    private String readFile(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String returnString = bufferedReader.readLine();
        bufferedReader.close();
        fileReader.close();

        return returnString;
    }

    class UserPostViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.item_2_linear_layout)
        LinearLayout userPostLinearLayout;

        @BindView(R.id.item_2_ice_cream_image_view)
        ImageView userPostIceCreamImageView;

        @BindView(R.id.item_2_flavor_text_view)
        TextView userPostFlavorTextView;

        @BindView(R.id.item_2_description_text_view)
        TextView userPostDescriptionTextView;


        UserPostViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
