package br.com.fepi.isc.rest.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestClient
{
    // ---------------------------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------------------------

    private static final String TAG = RestClient.class.getSimpleName();

    // ---------------------------------------------------------------------------------------------
    // Callback
    // ---------------------------------------------------------------------------------------------

    // Interface para os callbacks de resposta e erro das solicitações HTTP
    public interface OnResponseCallback
    {
        void onResponse(String response);

        void onError(Exception e);
    }

    // ---------------------------------------------------------------------------------------------
    // GET
    // ---------------------------------------------------------------------------------------------

    // Método para executar uma operação na thread principal (UI thread)
    private static void runOnMainThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    // Método para realizar solicitações GET assíncronas
    public static void get(final String urlString, final OnResponseCallback listener)
    {
        new Thread(() ->
        {
            HttpURLConnection urlConnection = null;

            try {
                // Conexao com endpoint
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();

                // Receber resposta
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                final StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                // Passar resposta para o callback
                runOnMainThread(() -> listener.onResponse(sb.toString()));
            }
            catch (final Exception e) {
                // Notificar excecao no callback
                Log.e(TAG, "GET Request failed", e);
                runOnMainThread(() -> listener.onError(e));
            }
            // Deu certo ou errado, finalizar conexao
            finally {
                try {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                catch (Exception e1) {
                    // ignorar
                }
            }
        }).start();
    }

    // Método para realizar solicitações POST assíncronas
    public static void post(final String urlString, final String json, final OnResponseCallback listener)
    {
        new Thread(() ->
        {
            HttpURLConnection urlConnection = null;

            try {
                // Conexao com endpoint
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                // Enviar "body" da requisicao
                OutputStream outputStream = urlConnection.getOutputStream();
                outputStream.write(json.getBytes());
                outputStream.flush();
                outputStream.close();

                // Receber resposta
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                final StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                // Passar resposta para o callback
                runOnMainThread(() -> listener.onResponse(sb.toString()));
            }
            catch (final Exception e) {
                Log.e(TAG, "POST Request failed", e);
                runOnMainThread(() -> listener.onError(e));
            }
            finally {
                try {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
                catch (Exception e1) {
                    // ignorar
                }
            }
        }).start();
    }
}

