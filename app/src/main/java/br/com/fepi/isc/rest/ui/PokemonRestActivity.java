package br.com.fepi.isc.rest.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.com.fepi.isc.rest.R;
import br.com.fepi.isc.rest.controller.HttpClient;
import br.com.fepi.isc.rest.controller.PokemonFactory;
import br.com.fepi.isc.rest.controller.RestClient;
import br.com.fepi.isc.rest.model.PokemonInfo;
import br.com.fepi.isc.rest.model.PokemonUrl;

public class PokemonRestActivity extends AppCompatActivity
{
    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = "PokemonRestActivity";

    // ---------------------------------------------------------------------------------------------
    // Atributos
    // ---------------------------------------------------------------------------------------------

    private Spinner mPokemonSpinner; // Caixa de texto para inserir a palavra
    private View mProgressBarView;

    // Pokémon Selecionado
    private View mPokemonSelecionadoView;
    private TextView mPokemonNomeTextView;
    private ImageView mPokemonFotoImageView;
    private TextView mPokemonTipo;

    private TextView mResultTextView;

    // ---------------------------------------------------------------------------------------------
    // Create da Activity
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon_rest); // Define o layout da Activity

        // Inicialização dos componentes da interface do usuário
        mPokemonSpinner = findViewById(R.id.pokemonSpinner);
        mProgressBarView = findViewById(R.id.progressBar);

        mPokemonSelecionadoView = findViewById(R.id.pokemonSelecionado);
        mPokemonNomeTextView = findViewById(R.id.pokemonNome);
        mPokemonFotoImageView = findViewById(R.id.pokemonFoto);
        mPokemonTipo = findViewById(R.id.pokemonTipo);

        // JSON resultante da busca de informações do Pokémon
        mResultTextView = findViewById(R.id.resultTextView);
//        mResultTextView.setMovementMethod(new ScrollingMovementMethod()); // ativar scrollbar

        // Quando um pokemon for selecionado...
        mPokemonSpinner.setOnItemSelectedListener(mOnPokemonSpinnerItemSelected);

        // Buscar lista de pokemons
        inicializarListaPokemons();
    }

    private void inicializarListaPokemons()
    {
        mProgressBarView.setVisibility(View.VISIBLE);

        RestClient.get("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0", new RestClient.OnResponseCallback()
        {
            @Override
            public void onResponse(String response) {
                try {
                    // Utilizando a fábrica de Pokémon para obter uma lista PokemonUrl
                    JSONObject jsonObject = new JSONObject(response);
                    List<PokemonUrl> pokemons = PokemonFactory.parsePokemonUrl(jsonObject);

                    // Criando um adaptador de array para o Spinner, que irá conter os Pokémons disponíveis
                    ArrayAdapter<PokemonUrl> adapter = new ArrayAdapter<>(
                        PokemonRestActivity.this, android.R.layout.simple_spinner_item, pokemons);

                    // Definindo o layout do dropdown para o adaptador do Spinner
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    // Configurando o adaptador para o Spinner
                    mPokemonSpinner.setAdapter(adapter);
                }
                catch (JSONException e) {
                    Toast.makeText(PokemonRestActivity.this, "Ocorreu uma falha…", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Falha ao obter lista de Pokemons.", e);
                }

                mProgressBarView.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(PokemonRestActivity.this, "Ocorreu uma falha…", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha ao obter lista de Pokemons.", e);

                mProgressBarView.setVisibility(View.GONE);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Pokémon Selecionado
    // ---------------------------------------------------------------------------------------------

    private final AdapterView.OnItemSelectedListener mOnPokemonSpinnerItemSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // Cuidado! Aqui tenha certeza de que sabe o tipo do objeto, ou irá gerar uma exceção
            PokemonUrl pokemonUrl = (PokemonUrl) mPokemonSpinner.getAdapter().getItem(position);
            buscarInformacoes(pokemonUrl);
        }

        // Não usado, ignorar
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void buscarInformacoes(PokemonUrl pokemonUrl)
    {
        if (pokemonUrl.url == null) {
            mPokemonSelecionadoView.setVisibility(View.GONE);
            return;
        }

        mPokemonSelecionadoView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.VISIBLE);
        mPokemonFotoImageView.setImageDrawable(null); // remover imagem atual

        RestClient.get(pokemonUrl.url, new RestClient.OnResponseCallback() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    PokemonInfo pokemonInfo = PokemonFactory.parsePokemonInfo(jsonObject);

                    mPokemonSelecionadoView.setVisibility(View.VISIBLE);
                    mPokemonNomeTextView.setText(pokemonInfo.nome);
                    mPokemonTipo.setText(String.join(", ", pokemonInfo.tipoLista));

                    buscarFoto(pokemonInfo.spriteUrl);

                    mResultTextView.setText(jsonObject.toString(4).replace("\\/", "/"));
                }
                catch (JSONException e) {
                    Toast.makeText(PokemonRestActivity.this, "Ocorreu uma falha…", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Falha ao tratar as informações obtidas do Pokémon '" + pokemonUrl.nome + "'…", e);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(PokemonRestActivity.this, "Ocorreu uma falha…", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha ao obter informações do Pokémon '" + pokemonUrl.nome + "'…", e);
            }
        });
    }

    private void buscarFoto(String fotoUrl)
    {
        // Buscar foto do Pokémon de forma assíncrona
        // [Atenção!] Nunca use a Thread Main para fazer requisição de recursos, ou ela pode travar
        // e gerar erros como "Aplicativo Não está Respondendo"
        new Thread(() ->
        {
            Bitmap bitmap = HttpClient.getBitmapFromURL(fotoUrl);
            runOnUiThread(() -> {
                mPokemonFotoImageView.setImageBitmap(bitmap);

                mProgressBarView.setVisibility(View.GONE);
            });

        }).start();
    }
}