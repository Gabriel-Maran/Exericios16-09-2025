package org.example.Dominio.Entidade

class Cliente(
    nome: String,
    idade: Int,
    var CPF: String,
    var telefone: String,
    var email: String,
): Pessoa(nome = nome, idade = idade,) {
}