package br.com.fepi.isc.rest.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.fepi.isc.rest.R;
import br.com.fepi.isc.rest.controller.RestClient;

public class FerramentaRestActivity extends AppCompatActivity
{
    // ---------------------------------------------------------------------------------------------
    // Atributos
    // ---------------------------------------------------------------------------------------------

    private EditText mRequisicaoGetEditText;  // Campo de texto para o usuário inserir o CEP.
    private Button mRequisitarButton; // Botão para pesquisar o CEP.
    private TextView mResultadoTextView; // Onde será exibido o resultado da pesquisa
    private View mProgressBarView; // TextView para exibir os resultados da pesquisa

    // ---------------------------------------------------------------------------------------------
    // Criacao da Activity
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ferramenta_rest); // Define o layout da activity.

        // Vincula os componentes da interface do usuário
        mRequisicaoGetEditText = findViewById(R.id.editText);
        mRequisitarButton = findViewById(R.id.button);
        mResultadoTextView = findViewById(R.id.resultTextView);
        mProgressBarView = findViewById(R.id.progressBar);

        // Define o que acontece quando o botão de pesquisa é clicado.
        mRequisitarButton.setOnClickListener(v ->
        {
            // Obtém o CEP digitado pelo usuário.
            String url = mRequisicaoGetEditText.getText().toString();
            url = url.trim(); // Remove espaços em branco extras do CEP.

            // Faz uma chamada à API REST para obter informações com base no CEP.
            RestClient.get(url, mRestCallback);

            // Para evitar que o botão seja clicado novamente enquanto a busca está em andamento.
            mRequisitarButton.setEnabled(false);
            mProgressBarView.setVisibility(View.VISIBLE);
        });
    }

    // ---------------------------------------------------------------------------------------------
    // Callback
    // ---------------------------------------------------------------------------------------------

    // Classe interna para lidar com a resposta da API REST.
    private final RestClient.OnResponseCallback mRestCallback = new RestClient.OnResponseCallback()
    {
        @Override
        public void onResponse(String json)
        {
            try {
                // Formata o JSON recebido para uma exibição mais legível.
                json = new JSONObject(json).toString(4);
                mResultadoTextView.setText(json); // Exibe o JSON formatado.
            }
            catch (JSONException e) {
                try {
                    // Formata o JSON recebido para uma exibição mais legível.
                    json = new JSONArray(json).toString(4);
                    mResultadoTextView.setText(json); // Exibe o JSON formatado.
                }
                catch (JSONException e2) {
                    mResultadoTextView.setText(e2.toString()); // Exibe qualquer exceção ocorrida.
                }
            }

            mRequisitarButton.setEnabled(true); // Habilita novamente o botão de pesquisa.
            mProgressBarView.setVisibility(View.GONE);
        }

        @Override
        public void onError(Exception e) {
            mResultadoTextView.setText(e.toString()); // Exibe qualquer erro ocorrido.
            mRequisitarButton.setEnabled(true); // Habilita novamente o botão de pesquisa.
            mProgressBarView.setVisibility(View.GONE);
        }
    };
}