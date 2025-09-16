package org.example

import org.example.Connection.EntidadeJDBC

// Gabriel Enzo Libero Maran, Gustavo Grosbelli, João Pedro Uhry, Paulo Augusto Vieira

// Esta função conecta no banco e faz uma busca por nome (com LIKE %texto%).
// Ela ignora maiúsculas/minúsculas e retorna todas as pessoas encontradas.
// Usa PreparedStatement para evitar SQL Injection e monta objetos Pessoa.
// Depois fecha resultado e conexão para não dar vazamento de recursos.
// Por fim devolve uma lista com as pessoas que batem com a pesquisa.

val conectar = EntidadeJDBC(
    url = "jdbc:postgresql://localhost:5433/exemploHuilson",
    usuario = "postgres",
    senha = "root"
)

// model simples da tabela Pessoa
data class Pessoa(
    val cpf: String,
    val nome: String,
    val idade: Int,
    val telefone: String,
    val email: String
)


/**
 * Busca pessoas cujo nome contenha a string fornecida (case-insensitive).
 * Ex.: buscarPessoasPorNome("A") retorna todas que tenham 'A' no nome.
 */
fun buscarPessoasPorNome(nomeBusca: String): List<Pessoa> {
    val resultado = mutableListOf<Pessoa>()
    val sql = "SELECT cpf, nome, idade, telefone, email FROM Pessoa WHERE nome ILIKE ?;"

    val conn = conectar.connectarComBanco()
    try {
        conn?.use { c ->
            c.prepareStatement(sql).use { ps ->
                ps.setString(1, "%${nomeBusca}%") // wildcard antes/depois
                val rs = ps.executeQuery()
                rs.use {
                    while (it.next()) {
                        val cpf = it.getString("cpf")
                        val nome = it.getString("nome")
                        val idade = it.getInt("idade")
                        val telefone = it.getString("telefone")
                        val email = it.getString("email")
                        resultado.add(Pessoa(cpf, nome, idade, telefone, email))
                    }
                }
            }
        } ?: run {
            println("Erro: conexão nula em buscarPessoasPorNome")
        }
    } catch (e: Exception) {
        println("Erro ao buscar pessoas: ${e.message}")
    } finally {
        try { conn?.close() } catch (_: Exception) {}
    }

    return resultado
}

// Exemplo de uso
fun main() {
    // busca tudo que tenha 'A' no nome
    val pessoas = buscarPessoasPorNome("A")
    println("Encontradas: ${pessoas.size}")
    pessoas.forEach { p ->
        println("CPF: ${p.cpf} | Nome: ${p.nome} | Idade: ${p.idade} | Tel: ${p.telefone} | Email: ${p.email}")
    }
}
