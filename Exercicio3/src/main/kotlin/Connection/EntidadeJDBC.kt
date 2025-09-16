package org.example.Connection

import java.sql.Connection
import java.sql.DriverManager

class EntidadeJDBC(
    val url: String,
    val usuario: String,
    val senha: String
) {
    fun connectarComBanco(): Connection? {
        try {
            val conexao: Connection = DriverManager.getConnection(
                this.url, this.usuario, this.senha
            )
            return conexao
        } catch (e : Exception){
            println(e.printStackTrace())
        }
        return null
    }
}