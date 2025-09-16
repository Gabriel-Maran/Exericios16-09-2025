package org.example

import org.example.Repository.conectar
import org.example.Repository.criarTabelaCliente
import org.example.Repository.criarTabelaPessoa
import org.example.Repository.selectCliente

//Alunos:
// Gabriel Enzo Libero Maran, Gustavo Grosbelli, João Pedro Uhry, Paulo Augusto Vieira
fun main() {
    try {
        // Criação das tabelas
        criarTabelaPessoa()
        criarTabelaCliente()

        val banco = conectar.connectarComBanco()
        val stmt = banco!!.createStatement()

        // Inserindo 1 Pessoa
        val insertPessoa = """
            INSERT INTO Pessoa (cpf, nome, idade, telefone, email)
            VALUES ('123.456.789-00', 'João Silva', 30, '(11)99999-9999', 'joao.silva@email.com');
        """
        stmt.execute(insertPessoa)

        // Inserindo 1 Cliente vinculado à Pessoa pelo CPF
        val insertCliente = """
            INSERT INTO Cliente (cpf)
            VALUES ('123.456.789-00');
        """
        stmt.execute(insertCliente)

        println("Tabelas criadas e populadas com sucesso!")
        banco.close()
    } catch (e: Exception) {
        println("Erro: ${e.message}")
    }
    selectCliente()

}
