package br.com.fepi.isc.rest.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import br.com.fepi.isc.rest.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        makeOpenActivityLink(R.id.btn_dictionary, DictionaryRestActivity.class);
        makeOpenActivityLink(R.id.btn_correios, CorreiosRestActivity.class);
        makeOpenActivityLink(R.id.btn_pokemon, PokemonRestActivity.class);
        makeOpenActivityLink(R.id.btn_ferramenta_rest, FerramentaRestActivity.class);
        makeOpenActivityLink(R.id.btn_servidor_rest, ServidorMainActivity.class);
    }

    private void makeOpenActivityLink(int resId, Class<?> activityClass) {
        findViewById(resId).setOnClickListener(v -> startActivity(new Intent(this, activityClass)));
    }
}