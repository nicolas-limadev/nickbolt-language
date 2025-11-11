import java.util.HashMap;
import java.util.Map;

public class LexicalAnalyzer {
    private final String sourceCode;
    private int position = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("string", TokenType.STRING);
        keywords.put("scanf", TokenType.SCANF);
        keywords.put("printf", TokenType.PRINTF);
        keywords.put("return", TokenType.RETURN); // Adicionado
    }

    public LexicalAnalyzer(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    private char peek() {
        if (position >= sourceCode.length()) {
            return '\0';
        }
        return sourceCode.charAt(position);
    }

    private void advance() {
        if (peek() == '\n') {
            line++;
        }
        position++;
    }

    private char peekNext() {
        if (position + 1 >= sourceCode.length()) {
            return '\0';
        }
        return sourceCode.charAt(position + 1);
    }

    private void skipWhitespaceAndComments() {
        while (peek() != '\0') {
            if (Character.isWhitespace(peek())) {
                advance();
            } else if (peek() == '/' && peekNext() == '/') {
                while (peek() != '\n' && peek() != '\0') {
                    advance();
                }
            } else if (peek() == '/' && peekNext() == '*') {
                advance(); advance();
                while (!(peek() == '*' && peekNext() == '/') && peek() != '\0') {
                    advance();
                }
                if (peek() != '\0') {
                    advance(); advance();
                }
            } else {
                break;
            }
        }
    }

    public Token getNextToken() {
        skipWhitespaceAndComments();
        char c = peek();
        if (c == '\0') {
            return new Token(TokenType.EOF, "", line);
        }
        if (Character.isLetter(c)) {
            return identifierOrKeyword();
        }
        if (Character.isDigit(c)) {
            return number();
        }
        if (c == '"') {
            return stringLiteral();
        }
        switch (c) {
            case '+':
                if (peekNext() == '+') {
                    advance(); advance();
                    return new Token(TokenType.INCREMENT, "++", line);
                }
                advance();
                return new Token(TokenType.PLUS, "+", line);
            case '-':
                if (peekNext() == '-') {
                    advance(); advance();
                    return new Token(TokenType.DECREMENT, "--", line);
                }
                advance();
                return new Token(TokenType.MINUS, "-", line);
            case '*': advance(); return new Token(TokenType.MULTIPLY, "*", line);
            case '/': advance(); return new Token(TokenType.DIVIDE, "/", line);
            case '(': advance(); return new Token(TokenType.LPAREN, "(", line);
            case ')': advance(); return new Token(TokenType.RPAREN, ")", line);
            case '{': advance(); return new Token(TokenType.LBRACE, "{", line);
            case '}': advance(); return new Token(TokenType.RBRACE, "}", line);
            case '[': advance(); return new Token(TokenType.LBRACKET, "[", line);
            case ']': advance(); return new Token(TokenType.RBRACKET, "]", line);
            case ';': advance(); return new Token(TokenType.SEMICOLON, ";", line);
            case ',': advance(); return new Token(TokenType.COMMA, ",", line);
            case '=':
                if (peekNext() == '=') {
                    advance(); advance();
                    return new Token(TokenType.RELATIONAL_OP, "==", line);
                }
                advance();
                return new Token(TokenType.ASSIGN, "=", line);
            case '!':
                if (peekNext() == '=') {
                    advance(); advance();
                    return new Token(TokenType.RELATIONAL_OP, "!=", line);
                }
                advance();
                return new Token(TokenType.LOGICAL_NOT, "!", line);
            case '<':
                if (peekNext() == '=') {
                    advance(); advance();
                    return new Token(TokenType.RELATIONAL_OP, "<=", line);
                }
                advance();
                return new Token(TokenType.RELATIONAL_OP, "<", line);
            case '>':
                if (peekNext() == '=') {
                    advance(); advance();
                    return new Token(TokenType.RELATIONAL_OP, ">=", line);
                }
                advance();
                return new Token(TokenType.RELATIONAL_OP, ">", line);
            case '&':
                if (peekNext() == '&') {
                    advance(); advance();
                    return new Token(TokenType.LOGICAL_AND, "&&", line);
                }
                break;
            case '|':
                if (peekNext() == '|') {
                    advance(); advance();
                    return new Token(TokenType.LOGICAL_OR, "||", line);
                }
                break;
        }
        throw new RuntimeException("Line " + line + ": Unexpected character '" + c + "'");
    }

    private Token identifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        while (peek() != '\0' && Character.isLetterOrDigit(peek())) {
            sb.append(peek());
            advance();
        }
        String lexeme = sb.toString();
        TokenType type = keywords.getOrDefault(lexeme, TokenType.ID);
        return new Token(type, lexeme, line);
    }

    private Token number() {
        StringBuilder sb = new StringBuilder();
        while (peek() != '\0' && Character.isDigit(peek())) {
            sb.append(peek());
            advance();
        }
        if (peek() == '.' && Character.isDigit(peekNext())) {
            sb.append(peek());
            advance();
            while (peek() != '\0' && Character.isDigit(peek())) {
                sb.append(peek());
                advance();
            }
        }
        return new Token(TokenType.NUMBER, sb.toString(), line);
    }
    
    private Token stringLiteral() {
        StringBuilder sb = new StringBuilder();
        advance();
        while(peek() != '"' && peek() != '\0' && peek() != '\n') {
            if (peek() == '\\') {
                advance();
                if (peek() == '\0') break;
                switch (peek()) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '\\': sb.append('\\'); break;
                    case '"': sb.append('"'); break;
                    default: sb.append('\\').append(peek());
                }
                advance();
            } else {
                sb.append(peek());
                advance();
            }
        }
        if (peek() == '"') {
            advance();
            return new Token(TokenType.STRING_LITERAL, sb.toString(), line);
        } else {
            throw new RuntimeException("Line " + line + ": Unterminated string literal");
        }
    }
}