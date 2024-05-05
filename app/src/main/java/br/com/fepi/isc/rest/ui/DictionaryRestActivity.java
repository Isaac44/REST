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

public class DictionaryRestActivity extends AppCompatActivity
{
    // ---------------------------------------------------------------------------------------------
    // Atributos
    // ---------------------------------------------------------------------------------------------

    private EditText mPalavraEditText; // Caixa de texto para inserir a palavra
    private Button mPesquisarButton; // Botão para fazer a pesquisa
    private TextView mResultadoTextView; // TextView para exibir os resultados da pesquisa
    private View mProgressBarView;

    // ---------------------------------------------------------------------------------------------
    // Create da Activity
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_rest); // Define o layout da Activity

        // Inicialização dos componentes da interface do usuário
        mPalavraEditText = findViewById(R.id.editText);
        mPesquisarButton = findViewById(R.id.button);
        mResultadoTextView = findViewById(R.id.textView);
        mProgressBarView = findViewById(R.id.progressBar);

        // Definir um listener para o botão de pesquisa
        mPesquisarButton.setOnClickListener(v ->
        {
            // Palavra digitada. Remove os espaços do começo e do fim e converte para minúsculas
            String palavra = mPalavraEditText.getText().toString().trim().toLowerCase();

            // Realiza uma chamada à API REST com a palavra fornecida
            RestClient.get("https://api.dicionario-aberto.net/word/" + palavra, mRestCallback);

            // Desativa o botão de pesquisa para evitar múltiplas solicitações simultâneas
            mPesquisarButton.setEnabled(false);
            mProgressBarView.setVisibility(View.VISIBLE);
        });
    }

    // Callback para tratar a resposta da solicitação REST
    private final RestClient.OnResponseCallback mRestCallback = new RestClient.OnResponseCallback()
    {
        @Override
        public void onResponse(String json)
        {
            try {
                // Parser da resposta em string json para o objeto JSON Array
                JSONArray jsonArray = new JSONArray(json);

                String resultado;
                // Verifica se foram encontradas definições para a palavra
                if (jsonArray.length() == 0) {
                    resultado = "Nenhuma definição foi encontrada…";
                } else {
                    StringBuilder sb = new StringBuilder();
                    // Itera sobre as definições encontradas
                    for (int k = 0; k < jsonArray.length(); k++) {
                        sb.append("Definição ").append(k + 1).append(":\n\n");

                        JSONObject jsonObject = (JSONObject) jsonArray.get(k);
                        String xml = (String) jsonObject.get("xml");

                        // Extrai a definição do XML retornado
                        int start = xml.indexOf("<def>");
                        int end = xml.indexOf("</def>");
                        String definicao = xml.substring(start + 5, end);
                        String[] linhas = definicao.trim().split("\n");

                        // Adiciona as linhas de definição ao resultado
                        for (int i = 0; i < linhas.length; i++) {
                            sb.append((i + 1)).append(". ").append(linhas[i]).append("\n");
                        }

                        sb.append("\n");
                    }

                    // Define o resultado final
                    resultado = sb.toString().trim();
                }

                // Exibe o resultado na TextView
                mResultadoTextView.setText(resultado);
            }
            catch (JSONException e) {
                // Em caso de erro de análise JSON, exibe o erro na TextView
                mResultadoTextView.setText(e.toString());
            }

            // Reativa o botão de pesquisa
            mPesquisarButton.setEnabled(true);
            mProgressBarView.setVisibility(View.GONE);
        }

        @Override
        public void onError(Exception e) {
            // Em caso de erro na solicitação REST, exibe o erro na TextView
            mResultadoTextView.setText(e.toString());

            // Reativa o botão de pesquisa
            mPesquisarButton.setEnabled(true);
            mProgressBarView.setVisibility(View.GONE);
        }
    };
}
