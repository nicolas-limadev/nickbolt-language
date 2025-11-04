public enum TipoToken {
    // Palavras-chave
    IF, ELSE, WHILE, FOR, INT, FLOAT, STRING, SCANF, PRINTF,

    // Identificadores e Literais
    ID,          // Nomes de variáveis
    NUMERO,      // 123, 3.14
    STRING_LITERAL, // "texto"

    // Operadores
    IGUAL,       // =
    MAIS,        // +
    MENOS,       // -
    MULT,        // *
    DIV,         // /
    OP_RELACIONAL, // ==, !=, <, >, <=, >=
    E_LOGICO,    // &&
    OU_LOGICO,   // ||
    NAO_LOGICO,  // !
    INCREMENTO,  // ++
    DECREMENTO,  // --

    // Pontuação
    ABRE_PARENTESES, FECHA_PARENTESES, // ( )
    ABRE_CHAVES, FECHA_CHAVES,         // { }
    PONTO_VIRGULA,                     // ;
    VIRGULA,                           // ,

    // Fim do arquivo
    EOF
}