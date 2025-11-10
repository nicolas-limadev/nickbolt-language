public enum TokenType {

    // Keywords
    IF, ELSE, WHILE, FOR, INT, FLOAT, STRING, SCANF, PRINTF,

    // Literals and Identifiers
    ID,             // Nomes de variáveis
    NUMBER,         // 123, 3.14
    STRING_LITERAL, // "texto"

    // Operators
    ASSIGN,         // =
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    RELATIONAL_OP,  // ==, !=, <, >, <=, >=
    LOGICAL_AND,    // &&
    LOGICAL_OR,     // ||
    LOGICAL_NOT,    // !
    INCREMENT,      // ++
    DECREMENT,      // --

    // Delimiters
    LPAREN, RPAREN, // ( )
    LBRACE, RBRACE, // { }
    SEMICOLON,      // ;
    COMMA,          // ,

    // End of File
    EOF
}