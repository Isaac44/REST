package br.com.fepi.isc.rest.model;

import java.util.ArrayList;
import java.util.List;

public class PokemonInfo
{
    public final String nome;

    public final String spriteUrl;

    public final List<String> tipoLista = new ArrayList<>();

    public PokemonInfo(String nome, String spriteUrl) {
        this.nome = nome;
        this.spriteUrl = spriteUrl;
    }
}
