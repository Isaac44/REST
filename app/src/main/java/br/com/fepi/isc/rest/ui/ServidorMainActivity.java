package br.com.fepi.isc.rest.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import br.com.fepi.isc.rest.R;
import br.com.fepi.isc.rest.controller.Helpers;

public class ServidorMainActivity extends AppCompatActivity
{
    // ---------------------------------------------------------------------------------------------
    // Constantes
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = "RESTServer";
    private static final String JSON_INVALIDO = "{\"erro\" : \"JSON do servidor é inválido…\"}";

    // ---------------------------------------------------------------------------------------------
    // Atributos
    // ---------------------------------------------------------------------------------------------

    private ServerSocket mServer;

    private String mRespostaJson = "{\"chave\" : \"valor\"}";

    // ---------------------------------------------------------------------------------------------
    // Create da Activity
    // ---------------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_main);

        String url = "Minha URL: http://" + Helpers.getIPAddress(true) + ":8080";
        ((TextView) findViewById(R.id.meuIP)).setText(url);

        EditText editText = findViewById(R.id.editText);

        mRespostaJson = editText.getText().toString();

        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                mRespostaJson = s.toString().trim();
                Log.i(TAG, "Resposta JSON foi alterada: " + mRespostaJson);
            }
        });

        startServer();
    }

    // Finaliza o servidor
    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (mServer != null) {
                mServer.close();
            }
        }
        catch (Exception e) {
            // ignorar
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Servidor
    // ---------------------------------------------------------------------------------------------

    private void startServer() {
        new Thread(this::runServer).start();
    }

    // Este método cria e executa um servidor socket na porta 8080
    private void runServer()
    {
        try
        {
            // Cria um novo ServerSocket na porta 8080
            mServer = new ServerSocket(8080);

            // Mantém o servidor em execução indefinidamente, até que a Activity seja finalizada
            // -> Veja o método "onDestroy" acima
            while (true)
            {
                // Aguarda e aceita conexões de clientes
                Socket socket = mServer.accept();

                try {
                    // Processa a requisição de cliente
                    respondeCliente(socket);
                }
                catch (Exception e) {
                    // Se ocorrer um erro ao processar a requisição do cliente,
                    // o erro é ignorado e registrado nos logs
                    Log.e(TAG, "Falha ao processar cliente.", e);
                }
            }
        }
        catch (IOException e)
        {
            // Se ocorrer um erro ao criar o servidor, o erro é registrado nos logs
            Log.e(TAG, "Falha ao criar servidor.", e);
        }
    }

    private static boolean isJSONValido(String json)
    {
        // Nota: O JSON é SEMPRE "Objeto" ou "Array"

        try {
            new JSONObject(json);
            return true;
        }
        catch (JSONException e1) {
            try {
                new JSONArray(json);
                return true;
            }
            catch (JSONException e2) {
                // ignorar
            }
        }
        return false;
    }

    private static String lerRequisicao(Socket socket) throws IOException {
        // Obtenha a InputStream do socket
        InputStream inputStream = socket.getInputStream();

        // Ler os dados da InputStream
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Armazenar os dados lidos
        StringBuilder stringBuilder = new StringBuilder();

        // Leia cada linha da InputStream, até que receba uma linha vazia
        while (true)
        {
            String line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }

            stringBuilder.append(line).append("\n");
        }

        return stringBuilder.toString();
    }

    private static String encontraEnpoint(String requisicao) {
        for (String linha : requisicao.split("\n")) {
            if (linha.startsWith("GET")) {
                return linha.split(" ")[1];
            }
        }
        return null;
    }

    // Este método responde a uma requisição HTTP de um cliente
    private void respondeCliente(Socket socket) throws IOException
    {
        // Lê a requisição enviada pelo cliente
        String requisicao = lerRequisicao(socket);

        // Extrai o endpoint da requisição
        // Obs: não usamos neste exemplo, mas você pode implementar diversos canais assim.
        // Por exemplo: /mensagens, /contatos, /galeria, etc.
        String endpoint = encontraEnpoint(requisicao);
        Log.i(TAG, "Enpoint solicitado: " + endpoint);

        // Verifica se o JSON é válido, senão, usa um JSON que irá informar o problema para cliente
        String respostaJson = mRespostaJson;
        if (respostaJson == null || !isJSONValido(respostaJson)) {
            respostaJson = JSON_INVALIDO;
        }

        // Envia a resposta HTTP ao cliente
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream());

        printWriter.print("HTTP/1.0 200" + "\r\n");
        printWriter.print("Content-Type: application/json" + "\r\n");
        printWriter.print("Content-Length: " + respostaJson.length() + "\r\n");
        printWriter.print("\r\n");
        printWriter.print(respostaJson + "\r\n");

        printWriter.flush();
    }
}