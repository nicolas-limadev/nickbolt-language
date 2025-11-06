public enum TipoToken {

    IF, ELSE, WHILE, FOR, INT, FLOAT, STRING, SCANF, PRINTF,

    ID,          // Nomes de variáveis
    NUMERO,      // 123, 3.14
    STRING_LITERAL, // "texto"

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

    ABRE_PARENTESES, FECHA_PARENTESES, // ( )
    ABRE_CHAVES, FECHA_CHAVES,         // { }
    PONTO_VIRGULA,                     // ;
    VIRGULA,                           // ,


    EOF
}