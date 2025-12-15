package com.example.applicationid;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationId.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.VH> {

    public interface OnImageClick { void onClick(int pos); }
    public interface OnImageLongClick { void onLongClick(int pos); }

    private final Context context;
    private final List<Uri> data;
    private final OnImageClick click;
    private final OnImageLongClick longClick;

    public ImageAdapter(Context context, List<Uri> data,
                        OnImageClick click, OnImageLongClick longClick) {
        this.context = context;
        this.data = data;
        this.click = click;
        this.longClick = longClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(com.example.applicationId.R.layout.item_image, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.img.setImageURI(data.get(position));

        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (click != null && pos != RecyclerView.NO_POSITION) click.onClick(pos);
        });

        h.itemView.setOnLongClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (longClick != null && pos != RecyclerView.NO_POSITION) {
                longClick.onLongClick(pos);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // ✅ VH має бути public, інакше буде warning про visibility scope
    public static class VH extends RecyclerView.ViewHolder {
        ImageView img;

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }
    }
}
