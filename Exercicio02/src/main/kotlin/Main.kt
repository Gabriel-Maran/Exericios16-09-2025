package org.example

import org.example.Connection.EntidadeJDBC
import java.math.BigDecimal

// ----- Conexão (use seu EntidadeJDBC já existente) -----
val conectar = EntidadeJDBC(
    url = "jdbc:postgresql://localhost:5433/exemploHuilson",
    usuario = "postgres",
    senha = "root"
)
// Gabriel Enzo Libero Maran, Gustavo Grosbelli, João Pedro Uhry, Paulo Augusto Vieira

// ----- Model simples -----
data class Produto(val id: Int? = null, val nome: String, val preco: BigDecimal)

// ----- Utils de número (varchar <-> BigDecimal) -----
object NumberUtils {
    fun parseBigDecimal(input: String?): BigDecimal {
        if (input == null) return BigDecimal.ZERO
        var s = input.trim().replace(Regex("[R$\\s]"), "")
        val lastDot = s.lastIndexOf('.'); val lastComma = s.lastIndexOf(',')
        s = if (lastDot >= 0 && lastComma > lastDot) s.replace(".", "").replace(",", ".") else s.replace(",", ".")
        return try { BigDecimal(s) } catch (e: Exception) { println("Erro parse: ${e.message}"); BigDecimal.ZERO }
    }
    fun toDatabaseString(v: BigDecimal) = v.toPlainString()
}

// ----- Main: cria tabelas, insere 1 registro em cada, e faz selects (resumido) -----
fun main() {
    val cpfEx = "123.456.789-00"
    val conn = conectar.connectarComBanco()

    try {
        conn?.use { c ->
            // 1) criar tabelas
            c.createStatement().use { s ->
                s.execute("""
                    CREATE TABLE IF NOT EXISTS Pessoa (
                        cpf VARCHAR(14) PRIMARY KEY,
                        nome VARCHAR(255) NOT NULL,
                        idade INT NOT NULL,
                        telefone VARCHAR(20) NOT NULL,
                        email VARCHAR(255) NOT NULL
                    );
                """.trimIndent())
                s.execute("""
                    CREATE TABLE IF NOT EXISTS Cliente (
                        id serial PRIMARY KEY,
                        cpf VARCHAR(14) NOT NULL,
                        FOREIGN KEY (cpf) REFERENCES Pessoa(cpf)
                    );
                """.trimIndent())
                s.execute("""
                    CREATE TABLE IF NOT EXISTS Produto (
                        id serial PRIMARY KEY,
                        nome VARCHAR(255) NOT NULL,
                        preco VARCHAR(50) NOT NULL
                    );
                """.trimIndent())
            }

            // 2) inserir 1 Pessoa (ON CONFLICT evita duplicar)
            val insPessoa = """
                INSERT INTO Pessoa (cpf, nome, idade, telefone, email)
                VALUES (?, ?, ?, ?, ?) ON CONFLICT (cpf) DO NOTHING;
            """.trimIndent()
            c.prepareStatement(insPessoa).use { ps ->
                ps.setString(1, cpfEx)
                ps.setString(2, "João Silva")
                ps.setInt(3, 30)
                ps.setString(4, "(11)99999-9999")
                ps.setString(5, "joao.silva@email.com")
                ps.executeUpdate()
            }

            // 3) inserir 1 Cliente (somente se não existir cpf na tabela Cliente)
            val insCliente = """
                INSERT INTO Cliente (cpf)
                SELECT ? WHERE NOT EXISTS (SELECT 1 FROM Cliente WHERE cpf = ?);
            """.trimIndent()
            c.prepareStatement(insCliente).use { ps ->
                ps.setString(1, cpfEx)
                ps.setString(2, cpfEx)
                ps.executeUpdate()
            }

            // 4) inserir 1 Produto (preço salvo como VARCHAR) - evita duplicar por nome
            val precoEx = BigDecimal("1234.56")
            val insProduto = """
                INSERT INTO Produto (nome, preco)
                SELECT ?, ? WHERE NOT EXISTS (SELECT 1 FROM Produto WHERE nome = ?);
            """.trimIndent()
            c.prepareStatement(insProduto).use { ps ->
                ps.setString(1, "Cimento")
                ps.setString(2, NumberUtils.toDatabaseString(precoEx))
                ps.setString(3, "Cimento")
                ps.executeUpdate()
            }

            // 5) Select Cliente JOIN Pessoa (mostra cliente com dados pessoais)
            val selCliente = """
                SELECT c.id, p.cpf, p.nome, p.idade, p.telefone, p.email
                FROM Cliente c
                JOIN Pessoa p ON c.cpf = p.cpf;
            """.trimIndent()
            c.createStatement().use { stmt ->
                val rs = stmt.executeQuery(selCliente)
                println("---- Clientes ----")
                while (rs.next()) {
                    println("Cliente ${rs.getInt("id")}: ${rs.getString("nome")}, CPF: ${rs.getString("cpf")}, Idade: ${rs.getInt("idade")}, Telefone: ${rs.getString("telefone")}, Email: ${rs.getString("email")}")
                }
                rs.close()
            }

            // 6) Select Produtos e conversão preco VARCHAR -> BigDecimal
            val selProduto = "SELECT id, nome, preco FROM Produto;"
            c.createStatement().use { stmt ->
                val rs = stmt.executeQuery(selProduto)
                println("---- Produtos ----")
                while (rs.next()) {
                    val id = rs.getInt("id")
                    val nome = rs.getString("nome")
                    val precoStr = rs.getString("preco")
                    val precoBd = NumberUtils.parseBigDecimal(precoStr)
                    println("Produto(id=$id, nome='$nome', preco=$precoBd)")
                }
                rs.close()
            }
        } ?: run {
            println("Erro: conexão nula.")
        }
    } catch (e: Exception) {
        println("Erro geral: ${e.message}")
        e.printStackTrace()
    } finally {
        try { conn?.close() } catch (_: Exception) {}
    }
}
