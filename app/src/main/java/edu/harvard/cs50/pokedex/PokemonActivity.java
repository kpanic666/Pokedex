package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView descTextView;
    private ImageView imgView;
    private String url;
    private String name;
    private RequestQueue requestQueue;
    private Button btn;
    private boolean isCaught;

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imgView.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        name = getIntent().getStringExtra("name").toLowerCase();
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        descTextView = findViewById(R.id.pokemon_desc);
        btn = findViewById(R.id.btn_catch);
        imgView = findViewById(R.id.imgPokemon);

        load();
    }

    public void toggleCatch(View view) {
        isCaught = !isCaught;

        if (!isCaught) {
            btn.setText("Catch");
        }
        else {
            btn.setText("Release");
        }

        getPreferences(MODE_PRIVATE).edit().putBoolean(nameTextView.getText().toString(), isCaught).
                commit();
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");
        descTextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }

                    isCaught = getPreferences(MODE_PRIVATE).getBoolean(nameTextView.getText().toString(), false);
                    if (!isCaught) {
                        btn.setText("Catch");
                    }
                    else {
                        btn.setText("Release");
                    }

                    JSONObject sprites = response.getJSONObject("sprites");
                    String img_url = sprites.getString("front_default");
                    new DownloadSpriteTask().execute(img_url);

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);

        String sReqUrl = "https://pokeapi.co/api/v2/pokemon-species/" + name + "/";

        JsonObjectRequest sRequest = new JsonObjectRequest(Request.Method.GET, sReqUrl,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray typeEntries = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        String lang = typeEntry.getJSONObject("language").getString("name");
                        if (lang.equals("en"))
                        {
                            descTextView.setText(typeEntry.getString("flavor_text"));
                            break;
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon desc json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon description error", error);
            }
        });

        requestQueue.add(sRequest);
    }
}
