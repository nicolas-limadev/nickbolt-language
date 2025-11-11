public enum TokenType {

    // Palavras-chave
    IF, ELSE, WHILE, FOR, INT, FLOAT, STRING, SCANF, PRINTF,
    RETURN, // Adicionado

    // Identificadores e Literais
    ID,          // Nomes de variáveis
    NUMBER,      // 123, 3.14
    STRING_LITERAL, // "texto"

    // Operadores
    ASSIGN,      // =
    PLUS,        // +
    MINUS,       // -
    MULTIPLY,    // *
    DIVIDE,      // /
    RELATIONAL_OP, // ==, !=, <, >, <=, >=
    LOGICAL_AND, // &&
    LOGICAL_OR,  // ||
    LOGICAL_NOT, // !
    INCREMENT,   // ++
    DECREMENT,   // --

    // Pontuação
    LPAREN, RPAREN,     // ( )
    LBRACE, RBRACE,     // { }
    LBRACKET, RBRACKET, // [ ] - para arrays
    SEMICOLON,          // ;
    COMMA,              // ,

    // Fim do arquivo
    EOF
}