package org.example.Repository

import org.example.Connection.EntidadeJDBC;

val conectar = EntidadeJDBC(
    url = "jdbc:postgresql://localhost:5433/exemploHuilson",
    usuario = "postgres",
    senha = "root"
)

fun criarTabelaPessoa() {
    val sql = "CREATE TABLE IF NOT EXISTS Pessoa (" +
            "    cpf VARCHAR(14) NOT NULL PRIMARY KEY," +
            "    nome VARCHAR(255) NOT NULL," +
            "    idade INT NOT NULL," +
            "    telefone VARCHAR(20) NOT NULL," +
            "    email VARCHAR(255) NOT NULL" +
            ");"
    val banco = conectar.connectarComBanco()
    val enviarParaBanco = banco!!.createStatement().execute(sql)

    println(enviarParaBanco)
    banco.close()
}

fun criarTabelaCliente() {
    val sql = "CREATE TABLE IF NOT EXISTS Cliente (" +
            "    id serial NOT NULL PRIMARY KEY," +
            "    cpf VARCHAR(14) NOT NULL," +
            "    FOREIGN KEY (cpf) REFERENCES Pessoa(cpf)" +
            ");"
    val banco = conectar.connectarComBanco()
    val enviarParaBanco = banco!!.createStatement().execute(sql)

    println(enviarParaBanco)
    banco.close()
}

//Função resposavel por se conectar no banco e dar um SELECT * em cliente
fun selectCliente() {
    val sql = """
        SELECT c.id, p.cpf, p.nome, p.idade, p.telefone, p.email
        FROM Cliente c
        INNER JOIN Pessoa p ON c.cpf = p.cpf;
    """

    val banco = conectar.connectarComBanco()
    val stmt = banco!!.createStatement()
    val rs = stmt.executeQuery(sql)

    while (rs.next()) {
        val id = rs.getInt("id")
        val cpf = rs.getString("cpf")
        val nome = rs.getString("nome")
        val idade = rs.getInt("idade")
        val telefone = rs.getString("telefone")
        val email = rs.getString("email")

        println("Cliente $id: $nome, CPF: $cpf, Idade: $idade, Telefone: $telefone, Email: $email")
    }

    rs.close()
    banco.close()
}
