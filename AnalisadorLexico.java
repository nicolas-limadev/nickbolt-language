import java.util.HashMap;
import java.util.Map;

public class AnalisadorLexico {
    private final String codigoFonte;
    private int posicao = 0;
    private int linha = 1;
    private static final Map<String, TipoToken> palavrasChave;

    static {
        palavrasChave = new HashMap<>();
        palavrasChave.put("if", TipoToken.IF);
        palavrasChave.put("else", TipoToken.ELSE);
        palavrasChave.put("while", TipoToken.WHILE);
        palavrasChave.put("for", TipoToken.FOR);
        palavrasChave.put("int", TipoToken.INT);
        palavrasChave.put("float", TipoToken.FLOAT);
        palavrasChave.put("string", TipoToken.STRING);
        palavrasChave.put("scanf", TipoToken.SCANF);
        palavrasChave.put("printf", TipoToken.PRINTF);
    }

    public AnalisadorLexico(String codigoFonte) {
        this.codigoFonte = codigoFonte;
    }

    private char charAtual() {
        if (posicao >= codigoFonte.length()) {
            return '\0'; // Caractere nulo para representar o fim do arquivo
        }
        return codigoFonte.charAt(posicao);
    }

    private void avancar() {
        if (charAtual() == '\n') {
            linha++;
        }
        posicao++;
    }

    private char proximoChar() {
        if (posicao + 1 >= codigoFonte.length()) {
            return '\0';
        }
        return codigoFonte.charAt(posicao + 1);
    }

    private void pularBrancos() {
        while (charAtual() != '\0') {
            if (Character.isWhitespace(charAtual())) {
                avancar();
            } else if (charAtual() == '/' && proximoChar() == '/') {
                
                while (charAtual() != '\n' && charAtual() != '\0') {
                    avancar();
                }
            } else if (charAtual() == '/' && proximoChar() == '*') {
                
                avancar(); avancar();
                while (!(charAtual() == '*' && proximoChar() == '/') && charAtual() != '\0') {
                    avancar();
                }
                if (charAtual() != '\0') {
                    avancar(); avancar();
                }
            } else {
                break;
            }
        }
    }

    public Token proximoToken() {
        pularBrancos();

        char c = charAtual();

        if (c == '\0') {
            return new Token(TipoToken.EOF, "", linha);
        }

        if (Character.isLetter(c)) {
            return identifierOuPalavraChave();
        }

        if (Character.isDigit(c)) {
            return numero();
        }

        if (c == '"') {
            return stringLiteral();
        }

        switch (c) {
            case '+':
                if (proximoChar() == '+') {
                    avancar(); avancar();
                    return new Token(TipoToken.INCREMENTO, "++", linha);
                }
                avancar();
                return new Token(TipoToken.MAIS, "+", linha);
            case '-':
                if (proximoChar() == '-') {
                    avancar(); avancar();
                    return new Token(TipoToken.DECREMENTO, "--", linha);
                }
                avancar();
                return new Token(TipoToken.MENOS, "-", linha);
            case '*': avancar(); return new Token(TipoToken.MULT, "*", linha);
            case '/': avancar(); return new Token(TipoToken.DIV, "/", linha);
            case '(': avancar(); return new Token(TipoToken.ABRE_PARENTESES, "(", linha);
            case ')': avancar(); return new Token(TipoToken.FECHA_PARENTESES, ")", linha);
            case '{': avancar(); return new Token(TipoToken.ABRE_CHAVES, "{", linha);
            case '}': avancar(); return new Token(TipoToken.FECHA_CHAVES, "}", linha);
            case ';': avancar(); return new Token(TipoToken.PONTO_VIRGULA, ";", linha);
            case ',': avancar(); return new Token(TipoToken.VIRGULA, ",", linha);
            case '=':
                if (proximoChar() == '=') {
                    avancar(); avancar();
                    return new Token(TipoToken.OP_RELACIONAL, "==", linha);
                }
                avancar();
                return new Token(TipoToken.IGUAL, "=", linha);
            case '!':
                if (proximoChar() == '=') {
                    avancar(); avancar();
                    return new Token(TipoToken.OP_RELACIONAL, "!=", linha);
                }
                avancar();
                return new Token(TipoToken.NAO_LOGICO, "!", linha);
            case '<':
                if (proximoChar() == '=') {
                    avancar(); avancar();
                    return new Token(TipoToken.OP_RELACIONAL, "<=", linha);
                }
                avancar();
                return new Token(TipoToken.OP_RELACIONAL, "<", linha);
            case '>':
                if (proximoChar() == '=') {
                    avancar(); avancar();
                    return new Token(TipoToken.OP_RELACIONAL, ">=", linha);
                }
                avancar();
                return new Token(TipoToken.OP_RELACIONAL, ">", linha);
            case '&':
                if (proximoChar() == '&') {
                    avancar(); avancar();
                    return new Token(TipoToken.E_LOGICO, "&&", linha);
                }
                break;
            case '|':
                if (proximoChar() == '|') {
                    avancar(); avancar();
                    return new Token(TipoToken.OU_LOGICO, "||", linha);
                }
                break;
        }

        throw new RuntimeException("Linha " + linha + ": Caractere inesperado '" + c + "'");
    }

    private Token identifierOuPalavraChave() {
        StringBuilder sb = new StringBuilder();
        while (charAtual() != '\0' && Character.isLetterOrDigit(charAtual())) {
            sb.append(charAtual());
            avancar();
        }
        String lexema = sb.toString();
        TipoToken tipo = palavrasChave.getOrDefault(lexema, TipoToken.ID);
        return new Token(tipo, lexema, linha);
    }

    private Token numero() {
        StringBuilder sb = new StringBuilder();
        
        while (charAtual() != '\0' && Character.isDigit(charAtual())) {
            sb.append(charAtual());
            avancar();
        }
        
        if (charAtual() == '.' && Character.isDigit(proximoChar())) {
            sb.append(charAtual());
            avancar();
            while (charAtual() != '\0' && Character.isDigit(charAtual())) {
                sb.append(charAtual());
                avancar();
            }
        }
        
        return new Token(TipoToken.NUMERO, sb.toString(), linha);
    }
    
    private Token stringLiteral() {
        StringBuilder sb = new StringBuilder();
        avancar();
        
        while(charAtual() != '"' && charAtual() != '\0' && charAtual() != '\n') {
            if (charAtual() == '\\') {
                avancar();
                if (charAtual() == '\0') break;
                switch (charAtual()) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '\\': sb.append('\\'); break;
                    case '"': sb.append('"'); break;
                    default: sb.append('\\').append(charAtual());
                }
                avancar();
            } else {
                sb.append(charAtual());
                avancar();
            }
        }
        
        if (charAtual() == '"') {
            avancar();
            return new Token(TipoToken.STRING_LITERAL, sb.toString(), linha);
        } else {
            throw new RuntimeException("Linha " + linha + ": String literal não terminada");
        }
    }
}