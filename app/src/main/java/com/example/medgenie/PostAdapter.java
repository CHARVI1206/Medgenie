
package com.example.medgenie;
import com.example.medgenie.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.caption.setText(post.getCaption());
        Glide.with(holder.itemView.getContext()).load(post.getImageUrl()).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView caption;

        ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.postImage);
            caption = itemView.findViewById(R.id.postCaption);
        }
    }
}