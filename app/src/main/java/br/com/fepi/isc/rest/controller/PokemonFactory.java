package br.com.fepi.isc.rest.controller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.fepi.isc.rest.model.PokemonInfo;
import br.com.fepi.isc.rest.model.PokemonUrl;

public class PokemonFactory
{
    // ---------------------------------------------------------------------------------------------
    // Parser
    // ---------------------------------------------------------------------------------------------

    public static List<PokemonUrl> parsePokemonUrl(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = (JSONArray) jsonObject.get("results");

        List<PokemonUrl> resultados = new ArrayList<>();
        resultados.add(new PokemonUrl("", null));

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            resultados.add(new PokemonUrl(obj.getString("name"), obj.getString("url")));
        }

        return resultados;
    }

    public static PokemonInfo parsePokemonInfo(JSONObject jsonObject) throws JSONException
    {
        JSONObject spriteObject = (JSONObject) jsonObject.get("sprites");

        PokemonInfo pokemonInfo = new PokemonInfo(jsonObject.getString("name"), spriteObject.getString("front_default"));

        JSONArray typesArray = (JSONArray) jsonObject.get("types");

        for (int i = 0; i < typesArray.length(); i++) {
            JSONObject typeArrayItem = (JSONObject) typesArray.get(i);
            JSONObject typeItem = (JSONObject) typeArrayItem.get("type");

            pokemonInfo.tipoLista.add(typeItem.getString("name"));
        }

        return pokemonInfo;
    }

}
