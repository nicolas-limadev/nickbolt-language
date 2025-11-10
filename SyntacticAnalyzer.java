import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SyntacticAnalyzer {
    private final LexicalAnalyzer lexer;
    private Token currentToken;
    
    private Map<String, TokenType> symbolTable = new HashMap<>();
    private Set<String> declaredVariables = new HashSet<>();

    public SyntacticAnalyzer(LexicalAnalyzer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken();
    }

    private void consume(TokenType expectedType) {
        if (currentToken.type == expectedType) {
            currentToken = lexer.getNextToken();
        } else {
            throw new RuntimeException("Line " + currentToken.line +
                    ": Expected '" + expectedType + "' but found '" + currentToken.type + "' (" + currentToken.lexeme + ")");
        }
    }
    
    private void checkVariableDeclared(String name) {
        if (!declaredVariables.contains(name)) {
            throw new RuntimeException("Line " + currentToken.line + ": Variable '" + name + "' not declared");
        }
    }
    
    private void declareVariable(String name, TokenType type) {
        if (declaredVariables.contains(name)) {
            throw new RuntimeException("Line " + currentToken.line + ": Variable '" + name + "' already declared");
        }
        declaredVariables.add(name);
        symbolTable.put(name, type);
    }

    public void program() {
        declarationList();
        statementList();
        if(currentToken.type != TokenType.EOF) {
            throw new RuntimeException("Line " + currentToken.line + ": Extra code found at the end of the program");
        }
    }

    private void declarationList() {
        while (currentToken.type == TokenType.INT || currentToken.type == TokenType.FLOAT || currentToken.type == TokenType.STRING) {
            declaration();
        }
    }

    private void declaration() {
        TokenType varType = currentToken.type;
        type();
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        declareVariable(varName, varType);
        idList(varType);
        consume(TokenType.SEMICOLON);
    }

    private void idList(TokenType type) {
        while (currentToken.type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            String varName = currentToken.lexeme;
            consume(TokenType.ID);
            declareVariable(varName, type);
        }
    }

    private void type() {
        if (currentToken.type == TokenType.INT) {
            consume(TokenType.INT);
        } else if (currentToken.type == TokenType.FLOAT) {
            consume(TokenType.FLOAT);
        } else {
            consume(TokenType.STRING);
        }
    }

    private void statementList() {
        while (currentToken.type != TokenType.EOF && currentToken.type != TokenType.RBRACE) {
            statement();
        }
    }

    private void statement() {
        switch (currentToken.type) {
            case ID: 
                String varName = currentToken.lexeme;
                consume(TokenType.ID);
                checkVariableDeclared(varName);
                
                TokenType variableType = symbolTable.get(varName);
                
                if (currentToken.type == TokenType.INCREMENT || currentToken.type == TokenType.DECREMENT) {
                    if (variableType == TokenType.STRING) {
                         throw new RuntimeException("Line " + currentToken.line + ": Operator '"+ currentToken.lexeme +"' invalid for type STRING");
                    }
                    consume(currentToken.type);
                    consume(TokenType.SEMICOLON);
                } else {
                    consume(TokenType.ASSIGN);
                    
                    TokenType expressionType = expression();
                    
                    checkAssignment(variableType, expressionType, currentToken.line);
                    
                    consume(TokenType.SEMICOLON);
                }
                break;
            case IF: ifStatement(); break;
            case WHILE: whileStatement(); break;
            case FOR: forStatement(); break;
            case SCANF: readStatement(); break;
            case PRINTF: writeStatement(); break;
            case LBRACE:
                consume(TokenType.LBRACE);
                statementList();
                consume(TokenType.RBRACE);
                break;
            default:
                 throw new RuntimeException("Line " + currentToken.line + ": Invalid or unexpected command '" + currentToken.lexeme + "'");
        }
    }
    
    private TokenType assignmentStatement() {
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        checkVariableDeclared(varName);
        
        TokenType variableType = symbolTable.get(varName);
        
        consume(TokenType.ASSIGN);
        
        TokenType expressionType = expression();
        checkAssignment(variableType, expressionType, currentToken.line);

        consume(TokenType.SEMICOLON);
        return variableType;
    }

    private void ifStatement() {
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        logicalExpression();
        consume(TokenType.RPAREN);
        statement();
        if (currentToken.type == TokenType.ELSE) {
            consume(TokenType.ELSE);
            statement();
        }
    }

    private void whileStatement() {
        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        logicalExpression();
        consume(TokenType.RPAREN);
        statement();
    }

    private void forStatement() {
        consume(TokenType.FOR);
        consume(TokenType.LPAREN);
        
        TokenType counterType = assignmentStatement();
        
        logicalExpression(); 
        
        consume(TokenType.SEMICOLON);
        
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        checkVariableDeclared(varName);
        
        if (symbolTable.get(varName) != counterType) {
            throw new RuntimeException("Line " + currentToken.line + ": FOR loop increment variable inconsistent");
        }

        if (currentToken.type == TokenType.INCREMENT || currentToken.type == TokenType.DECREMENT) {
            if (counterType == TokenType.STRING) {
                 throw new RuntimeException("Line " + currentToken.line + ": Operator '"+ currentToken.lexeme +"' invalid for type STRING");
            }
            consume(currentToken.type);
        } else {
            consume(TokenType.ASSIGN);
            TokenType exprType = expression();
            checkAssignment(counterType, exprType, currentToken.line);
        }
        
        consume(TokenType.RPAREN);
        statement();
    }

    private void readStatement() {
        consume(TokenType.SCANF);
        consume(TokenType.LPAREN);
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        checkVariableDeclared(varName);
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
    }

    private void writeStatement() {
        consume(TokenType.PRINTF);
        consume(TokenType.LPAREN);
        
        expression();
        
        while (currentToken.type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            expression();
        }
        
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
    }

    private TokenType expression() {
        if (currentToken.type == TokenType.STRING_LITERAL) {
            consume(TokenType.STRING_LITERAL);
            return TokenType.STRING;
        } else {
            return arithmeticExpression();
        }
    }


    private void logicalExpression() {
        logicalTerm();
        while (currentToken.type == TokenType.LOGICAL_AND || currentToken.type == TokenType.LOGICAL_OR) {
            consume(currentToken.type);
            logicalTerm();
        }
    }

    private void logicalTerm() {
        if (currentToken.type == TokenType.LOGICAL_NOT) {
            consume(TokenType.LOGICAL_NOT);
        }
        
        if (currentToken.type == TokenType.LPAREN) {
            consume(TokenType.LPAREN);
            logicalExpression();
            consume(TokenType.RPAREN);
        } else {
            relationalExpression();
        }
    }

    private void relationalExpression() {
        TokenType type1 = arithmeticExpression();
        
        if (currentToken.type != TokenType.RELATIONAL_OP) {
            throw new RuntimeException("Line " + currentToken.line + ": Relational operator expected, but found " + currentToken.type);
        }
        
        consume(TokenType.RELATIONAL_OP);
        TokenType type2 = arithmeticExpression();

        if (type1 == TokenType.STRING || type2 == TokenType.STRING) {
            throw new RuntimeException("Line " + currentToken.line + ": Relational operations not supported for type STRING");
        }
    }
    

    private TokenType arithmeticExpression() {
        TokenType type1 = term();

        while (currentToken.type == TokenType.PLUS || currentToken.type == TokenType.MINUS) {
            TokenType op = currentToken.type;
            consume(op);
            TokenType type2 = term();

            if (type1 == TokenType.STRING || type2 == TokenType.STRING) {
                throw new RuntimeException("Line " + currentToken.line + ": Operator '" + op + "' invalid for type STRING");
            }
            
            type1 = promoteType(type1, type2, currentToken.line);
        }
        return type1;
    }

    private TokenType term() {
        TokenType type1 = factor();

        while (currentToken.type == TokenType.MULTIPLY || currentToken.type == TokenType.DIVIDE) {
            TokenType op = currentToken.type;
            consume(op);
            TokenType type2 = factor();
            
            if (type1 == TokenType.STRING || type2 == TokenType.STRING) {
                throw new RuntimeException("Line " + currentToken.line + ": Operator '" + op + "' invalid for type STRING");
            }

            if (op == TokenType.DIVIDE) {
                type1 = TokenType.FLOAT;
            } else {
                type1 = promoteType(type1, type2, currentToken.line);
            }
        }
        return type1;
    }

    private TokenType factor() {
        boolean isNegative = false;
        if (currentToken.type == TokenType.MINUS) {
            consume(TokenType.MINUS);
            isNegative = true;
        }
        
        if (currentToken.type == TokenType.ID) {
            String varName = currentToken.lexeme;
            consume(TokenType.ID);
            checkVariableDeclared(varName);
            
            TokenType type = symbolTable.get(varName);
            
            if (isNegative && type == TokenType.STRING) {
                throw new RuntimeException("Line " + currentToken.line + ": Operator '-' (negation) invalid for type STRING");
            }
            return type; 
            
        } else if (currentToken.type == TokenType.NUMBER) {
            String numberLexeme = currentToken.lexeme;
            consume(TokenType.NUMBER);
            
            if (numberLexeme.contains(".")) {
                return TokenType.FLOAT;
            } else {
                return TokenType.INT;
            }
            
        } else if (currentToken.type == TokenType.LPAREN) {
            consume(TokenType.LPAREN);
            TokenType type = arithmeticExpression();
            consume(TokenType.RPAREN);
            
            if (isNegative && type == TokenType.STRING) {
                 throw new RuntimeException("Line " + currentToken.line + ": Operator '-' (negation) invalid for type STRING");
            }
            return type;
        } else {
            throw new RuntimeException("Line " + currentToken.line + ": Invalid factor, expected ID, NUMBER or (expression). Found: " + currentToken.type);
        }
    }
    
    private TokenType promoteType(TokenType type1, TokenType type2, int line) {
        if (type1 == TokenType.FLOAT || type2 == TokenType.FLOAT) {
            return TokenType.FLOAT;
        }
        if (type1 == TokenType.INT && type2 == TokenType.INT) {
            return TokenType.INT;
        }
        throw new RuntimeException("Line " + line + ": Incompatible types for arithmetic operation: " + type1 + " and " + type2);
    }
    
    private void checkAssignment(TokenType variableType, TokenType expressionType, int line) {
        if (variableType == expressionType) {
            return;
        }
        
        if (variableType == TokenType.FLOAT && expressionType == TokenType.INT) {
            return;
        }
        
        throw new RuntimeException("Line " + line + 
                ": Incompatible types. Cannot assign " + expressionType + 
                " to a variable of type " + variableType);
    }
}
