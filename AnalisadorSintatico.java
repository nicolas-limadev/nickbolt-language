import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnalisadorSintatico {
    private final AnalisadorLexico lexer;
    private Token tokenAtual;
    private Map<String, TipoToken> tabelaSimbolos = new HashMap<>();
    private Set<String> variaveisDeclaradas = new HashSet<>();

    public AnalisadorSintatico(AnalisadorLexico lexer) {
        this.lexer = lexer;
        this.tokenAtual = lexer.proximoToken();
    }

    private void consome(TipoToken tipoEsperado) {
        if (tokenAtual.tipo == tipoEsperado) {
            tokenAtual = lexer.proximoToken();
        } else {
            throw new RuntimeException("Linha " + tokenAtual.linha +
                    ": Esperava '" + tipoEsperado + "' mas encontrou '" + tokenAtual.tipo + "' (" + tokenAtual.lexema + ")");
        }
    }
    
    private void verificaVariavelDeclarada(String nome) {
        if (!variaveisDeclaradas.contains(nome)) {
            throw new RuntimeException("Linha " + tokenAtual.linha + ": Variável '" + nome + "' não declarada");
        }
    }
    
    private void declaraVariavel(String nome, TipoToken tipo) {
        if (variaveisDeclaradas.contains(nome)) {
            throw new RuntimeException("Linha " + tokenAtual.linha + ": Variável '" + nome + "' já declarada");
        }
        variaveisDeclaradas.add(nome);
        tabelaSimbolos.put(nome, tipo);
    }

    public void programa() {
        listaDeclaracoes();
        listaComandos();
        if(tokenAtual.tipo != TipoToken.EOF) {
            throw new RuntimeException("Linha " + tokenAtual.linha + ": Código extra encontrado no final do programa");
        }
    }

    private void listaDeclaracoes() {
        while (tokenAtual.tipo == TipoToken.INT || tokenAtual.tipo == TipoToken.FLOAT || tokenAtual.tipo == TipoToken.STRING) {
            declaracao();
        }
    }

    private void declaracao() {
        TipoToken tipoVar = tokenAtual.tipo;
        tipo();
        String nomeVar = tokenAtual.lexema;
        consome(TipoToken.ID);
        declaraVariavel(nomeVar, tipoVar);
        listaIds(tipoVar);
        consome(TipoToken.PONTO_VIRGULA);
    }

    private void listaIds(TipoToken tipo) {
        while (tokenAtual.tipo == TipoToken.VIRGULA) {
            consome(TipoToken.VIRGULA);
            String nomeVar = tokenAtual.lexema;
            consome(TipoToken.ID);
            declaraVariavel(nomeVar, tipo);
        }
    }

    private void tipo() {
        if (tokenAtual.tipo == TipoToken.INT) {
            consome(TipoToken.INT);
        } else if (tokenAtual.tipo == TipoToken.FLOAT) {
            consome(TipoToken.FLOAT);
        } else {
            consome(TipoToken.STRING);
        }
    }

    private void listaComandos() {
        while (tokenAtual.tipo != TipoToken.EOF && tokenAtual.tipo != TipoToken.FECHA_CHAVES) {
            comando();
        }
    }

    private void comando() {
        switch (tokenAtual.tipo) {
            case ID: 
                String nomeVar = tokenAtual.lexema;
                consome(TipoToken.ID);
                verificaVariavelDeclarada(nomeVar);
                
                if (tokenAtual.tipo == TipoToken.INCREMENTO || tokenAtual.tipo == TipoToken.DECREMENTO) {
                    consome(tokenAtual.tipo);
                    consome(TipoToken.PONTO_VIRGULA);
                } else {
                    consome(TipoToken.IGUAL);
                    expressaoAritmetica();
                    consome(TipoToken.PONTO_VIRGULA);
                }
                break;
            case IF: comandoIf(); break;
            case WHILE: comandoWhile(); break;
            case FOR: comandoFor(); break;
            case SCANF: comandoLeitura(); break;
            case PRINTF: comandoEscrita(); break;
            case ABRE_CHAVES:
                consome(TipoToken.ABRE_CHAVES);
                listaComandos();
                consome(TipoToken.FECHA_CHAVES);
                break;
            default:
                 throw new RuntimeException("Linha " + tokenAtual.linha + ": Comando inválido ou inesperado '" + tokenAtual.lexema + "'");
        }
    }

    private void comandoAtribuicao() {
        String nomeVar = tokenAtual.lexema;
        consome(TipoToken.ID);
        verificaVariavelDeclarada(nomeVar);
        consome(TipoToken.IGUAL);
        expressaoAritmetica();
        consome(TipoToken.PONTO_VIRGULA);
    }

    private void comandoIf() {
        consome(TipoToken.IF);
        consome(TipoToken.ABRE_PARENTESES);
        expressaoLogica();
        consome(TipoToken.FECHA_PARENTESES);
        comando();
        if (tokenAtual.tipo == TipoToken.ELSE) {
            consome(TipoToken.ELSE);
            comando();
        }
    }

    private void comandoWhile() {
        consome(TipoToken.WHILE);
        consome(TipoToken.ABRE_PARENTESES);
        expressaoLogica();
        consome(TipoToken.FECHA_PARENTESES);
        comando();
    }

    private void comandoFor() {
        consome(TipoToken.FOR);
        consome(TipoToken.ABRE_PARENTESES);
        comandoAtribuicao();
        expressaoLogica();
        consome(TipoToken.PONTO_VIRGULA);
        
        String nomeVar = tokenAtual.lexema;
        consome(TipoToken.ID);
        verificaVariavelDeclarada(nomeVar);
        
        if (tokenAtual.tipo == TipoToken.INCREMENTO || tokenAtual.tipo == TipoToken.DECREMENTO) {
            consome(tokenAtual.tipo);
        } else {
            consome(TipoToken.IGUAL);
            expressaoAritmetica();
        }
        
        consome(TipoToken.FECHA_PARENTESES);
        comando();
    }

    private void comandoLeitura() {
        consome(TipoToken.SCANF);
        consome(TipoToken.ABRE_PARENTESES);
        String nomeVar = tokenAtual.lexema;
        consome(TipoToken.ID);
        verificaVariavelDeclarada(nomeVar);
        consome(TipoToken.FECHA_PARENTESES);
        consome(TipoToken.PONTO_VIRGULA);
    }

    private void comandoEscrita() {
        consome(TipoToken.PRINTF);
        consome(TipoToken.ABRE_PARENTESES);
        
        if(tokenAtual.tipo == TipoToken.STRING_LITERAL) {
            consome(TipoToken.STRING_LITERAL);
        } else {
            expressaoAritmetica();
        }
        
        while (tokenAtual.tipo == TipoToken.VIRGULA) {
            consome(TipoToken.VIRGULA);
            if(tokenAtual.tipo == TipoToken.STRING_LITERAL) {
                consome(TipoToken.STRING_LITERAL);
            } else {
                expressaoAritmetica();
            }
        }
        
        consome(TipoToken.FECHA_PARENTESES);
        consome(TipoToken.PONTO_VIRGULA);
    }

    private void expressaoLogica() {
        termoLogico();
        while (tokenAtual.tipo == TipoToken.E_LOGICO || tokenAtual.tipo == TipoToken.OU_LOGICO) {
            consome(tokenAtual.tipo);
            termoLogico();
        }
    }

    private void termoLogico() {
        if (tokenAtual.tipo == TipoToken.NAO_LOGICO) {
            consome(TipoToken.NAO_LOGICO);
        }
        
        if (tokenAtual.tipo == TipoToken.ABRE_PARENTESES) {
            consome(TipoToken.ABRE_PARENTESES);
            expressaoLogica();
            consome(TipoToken.FECHA_PARENTESES);
        } else {
            expressaoRelacional();
        }
    }

    private void expressaoRelacional() {
        expressaoAritmetica();
        consome(TipoToken.OP_RELACIONAL);
        expressaoAritmetica();
    }
    

    private void expressaoAritmetica() {
        termo();
        while (tokenAtual.tipo == TipoToken.MAIS || tokenAtual.tipo == TipoToken.MENOS) {
            consome(tokenAtual.tipo);
            termo();
        }
    }

    private void termo() {
        fator();
        while (tokenAtual.tipo == TipoToken.MULT || tokenAtual.tipo == TipoToken.DIV) {
            consome(tokenAtual.tipo);
            fator();
        }
    }

    private void fator() {
        if (tokenAtual.tipo == TipoToken.MENOS) {
            consome(TipoToken.MENOS);
        }
        
        if (tokenAtual.tipo == TipoToken.ID) {
            String nomeVar = tokenAtual.lexema;
            consome(TipoToken.ID);
            verificaVariavelDeclarada(nomeVar);
        } else if (tokenAtual.tipo == TipoToken.NUMERO) {
            consome(TipoToken.NUMERO);
        } else if (tokenAtual.tipo == TipoToken.ABRE_PARENTESES) {
            consome(TipoToken.ABRE_PARENTESES);
            expressaoAritmetica();
            consome(TipoToken.FECHA_PARENTESES);
        } else {
            throw new RuntimeException("Linha " + tokenAtual.linha + ": Fator inválido, esperado ID, NUMERO ou (expressão). Encontrado: " + tokenAtual.tipo);
        }
    }
}