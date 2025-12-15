package com.example.applicationid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.applicationId.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private final List<Uri> images = new ArrayList<>();
    private ImageAdapter adapter;

    private RecyclerView recycler;
    private FloatingActionButton fabAdd;

    // ✅ SAF picker з мультивибором
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    int inserted = 0;

                    if (data.getClipData() != null) {
                        int n = data.getClipData().getItemCount();
                        for (int i = 0; i < n; i++) {
                            Uri u = data.getClipData().getItemAt(i).getUri();
                            persist(u);
                            images.add(u);
                            UriStorage.addToGallery(this, u); // або збереження в prefs
                            inserted++;
                        }
                        if (inserted > 0) {
                            adapter.notifyItemRangeInserted(images.size() - inserted, inserted);
                        }
                    } else if (data.getData() != null) {
                        Uri u = data.getData();
                        persist(u);
                        images.add(u);
                        UriStorage.addToGallery(this, u);
                        adapter.notifyItemInserted(images.size() - 1);
                    }
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.applicationId.R.layout.activity_gallery);

        recycler = findViewById(R.id.recycler);
        fabAdd = findViewById(R.id.fabAdd);

        // ✅ стартовий набір (збережений раніше)
        images.addAll(UriStorage.getGalleryUris(this));

        recycler.setLayoutManager(new GridLayoutManager(this, 3));

        // ✅ 1) Adapter з колбеками
        adapter = new ImageAdapter(
                this,
                images,
                pos -> openFull(images, pos),
                this::confirmDelete
        );

        recycler.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> launchPicker());
    }

    // ✅ 2) Відкриття повного екрана
    private void openFull(List<Uri> list, int start) {
        Intent i = new Intent(this, FullscreenActivity.class);
        ArrayList<String> payload = new ArrayList<>();
        for (Uri u : list) payload.add(u.toString());
        i.putStringArrayListExtra("uris", payload);
        i.putExtra("start", start);
        startActivity(i);
    }

    // ✅ 3) Мультивибір через SAF
    private void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickImageLauncher.launch(intent);
    }

    // ✅ persistable дозвіл
    private void persist(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}
    }

    // ✅ 5) Видалення по long-click
    private void confirmDelete(int pos) {
        new AlertDialog.Builder(this)
                .setMessage("Видалити з галереї?")
                .setPositiveButton("Так", (d, w) -> {
                    if (pos < 0 || pos >= images.size()) return;
                    images.remove(pos);
                    UriStorage.saveGalleryUris(this, images); // зберігаємо оновлений список
                    adapter.notifyItemRemoved(pos);
                })
                .setNegativeButton("Ні", null)
                .show();
    }
}
