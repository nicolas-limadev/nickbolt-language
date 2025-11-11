import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SyntacticAnalyzer {
    private final LexicalAnalyzer lexer;
    private Token currentToken;
    private Stack<Map<String, TokenType>> scopeStack = new Stack<>();
    private Map<String, Integer> arrayDimensions = new HashMap<>();
    
    private static class FunctionSignature {
        TokenType returnType;
        List<TokenType> paramTypes = new ArrayList<>();
        FunctionSignature(TokenType returnType) { this.returnType = returnType; }
    }
    
    private Map<String, FunctionSignature> functionTable = new HashMap<>();
    private TokenType currentFunctionReturnType = null;

    public SyntacticAnalyzer(LexicalAnalyzer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken();
        pushScope();
    }
    
    private void pushScope() {
        scopeStack.push(new HashMap<String, TokenType>());
    }
    
    private void popScope() {
        scopeStack.pop();
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
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).containsKey(name)) {
                return;
            }
        }
        throw new RuntimeException("Line " + currentToken.line + ": Variable '" + name + "' not declared");
    }
    
    private void declareVariable(String name, TokenType type) {
        if (scopeStack.peek().containsKey(name)) {
            throw new RuntimeException("Line " + currentToken.line + ": Variable '" + name + "' already declared in this scope");
        }
        scopeStack.peek().put(name, type);
    }
    
    private void declareArray(String name, TokenType type, int size) {
        declareVariable(name, type);
        arrayDimensions.put(name, size);
    }
    
    private boolean isArray(String name) {
        return arrayDimensions.containsKey(name);
    }
    
    private TokenType getVariableType(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            if (scopeStack.get(i).containsKey(name)) {
                return scopeStack.get(i).get(name);
            }
        }
        throw new RuntimeException("Line " + currentToken.line + ": Internal compiler error. Variable '" + name + "' type not found.");
    }

    public void program() {
        while (currentToken.type != TokenType.EOF) {
            topLevelDeclaration();
        }
        popScope();
    }

    private void topLevelDeclaration() {
        TokenType varType = type();
        String name = currentToken.lexeme;
        consume(TokenType.ID);
        
        if (currentToken.type == TokenType.LPAREN) {
            functionDeclaration(varType, name);
        } else {
            variableDeclaration(varType, name);
            consume(TokenType.SEMICOLON);
        }
    }

    private void variableDeclaration(TokenType type, String name) {
        if (currentToken.type == TokenType.LBRACKET) {
            consume(TokenType.LBRACKET);
            if (currentToken.type != TokenType.NUMBER) {
                throw new RuntimeException("Line " + currentToken.line + ": Array size must be a number");
            }
            int size = Integer.parseInt(currentToken.lexeme);
            consume(TokenType.NUMBER);
            consume(TokenType.RBRACKET);
            declareArray(name, type, size);
        } else {
            declareVariable(name, type);
        }
        idList(type);
    }

    private void functionDeclaration(TokenType returnType, String name) {
        if (functionTable.containsKey(name)) {
            throw new RuntimeException("Line " + currentToken.line + ": Function '" + name + "' already defined");
        }
        
        FunctionSignature signature = new FunctionSignature(returnType);
        functionTable.put(name, signature);
        
        consume(TokenType.LPAREN);
        
        pushScope();
        
        currentFunctionReturnType = returnType;
        
        if (currentToken.type != TokenType.RPAREN) {
            parameterList(signature);
        }
        
        consume(TokenType.RPAREN);
        
        consume(TokenType.LBRACE);
        statementList();
        consume(TokenType.RBRACE);
        
        currentFunctionReturnType = null;
        popScope();
    }
    
    private void parameterList(FunctionSignature signature) {
        param(signature);
        while (currentToken.type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            param(signature);
        }
    }

    private void param(FunctionSignature signature) {
        TokenType paramType = type();
        String paramName = currentToken.lexeme;
        consume(TokenType.ID);
        
        signature.paramTypes.add(paramType);
        declareVariable(paramName, paramType);
    }

    private void declaration() {
        TokenType varType = type();
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

    private TokenType type() {
        if (currentToken.type == TokenType.INT) {
            consume(TokenType.INT);
            return TokenType.INT;
        } else if (currentToken.type == TokenType.FLOAT) {
            consume(TokenType.FLOAT);
            return TokenType.FLOAT;
        } else {
            consume(TokenType.STRING);
            return TokenType.STRING;
        }
    }

    private void statementList() {
        while (currentToken.type != TokenType.EOF && currentToken.type != TokenType.RBRACE) {
            if (currentToken.type == TokenType.INT || currentToken.type == TokenType.FLOAT || currentToken.type == TokenType.STRING) {
                declaration();
            } else {
                statement();
            }
        }
    }

    private void statement() {
        switch (currentToken.type) {
            case ID: 
                String varName = currentToken.lexeme;
                consume(TokenType.ID);
                
                if (currentToken.type == TokenType.LPAREN) {
                    functionCall(varName);
                    consume(TokenType.SEMICOLON);
                } else {
                    checkVariableDeclared(varName);
                    TokenType variableType = getVariableType(varName);
                    
                    if (currentToken.type == TokenType.LBRACKET) {
                        // Array access
                        if (!isArray(varName)) {
                            throw new RuntimeException("Line " + currentToken.line + ": Variable '" + varName + "' is not an array");
                        }
                        consume(TokenType.LBRACKET);
                        TokenType indexType = arithmeticExpression();
                        if (indexType != TokenType.INT) {
                            throw new RuntimeException("Line " + currentToken.line + ": Array index must be an integer");
                        }
                        consume(TokenType.RBRACKET);
                        
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
                    } else if (currentToken.type == TokenType.INCREMENT || currentToken.type == TokenType.DECREMENT) {
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
                }
                break;
            case IF: ifStatement(); break;
            case WHILE: whileStatement(); break;
            case FOR: forStatement(); break;
            case SCANF: readStatement(); break;
            case PRINTF: writeStatement(); break;
            case RETURN: returnStatement(); break;
            case LBRACE:
                consume(TokenType.LBRACE);
                pushScope();
                statementList();
                popScope();
                consume(TokenType.RBRACE);
                break;
            default:
                 throw new RuntimeException("Line " + currentToken.line + ": Invalid or unexpected command '" + currentToken.lexeme + "'");
        }
    }
    
    private void returnStatement() {
        consume(TokenType.RETURN);
        
        if (currentFunctionReturnType == null) {
            throw new RuntimeException("Line " + currentToken.line + ": 'return' statement outside of a function");
        }
        
        TokenType expressionType = expression();
        checkAssignment(currentFunctionReturnType, expressionType, currentToken.line);
        
        consume(TokenType.SEMICOLON);
    }
    
    private TokenType assignmentStatement() {
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        checkVariableDeclared(varName);
        
        TokenType variableType = getVariableType(varName);
        
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
        
        pushScope();
        
        if(currentToken.type == TokenType.INT || currentToken.type == TokenType.FLOAT || currentToken.type == TokenType.STRING) {
            declaration();
        } else {
            assignmentStatement();
        }
        
        logicalExpression(); 
        
        consume(TokenType.SEMICOLON);
        
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        checkVariableDeclared(varName);
        
        TokenType counterType = getVariableType(varName);

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
        popScope();
    }

    private void readStatement() {
        consume(TokenType.SCANF);
        consume(TokenType.LPAREN);
        String varName = currentToken.lexeme;
        consume(TokenType.ID);
        checkVariableDeclared(varName);
        
        if (currentToken.type == TokenType.LBRACKET) {
            // Array element access in scanf
            if (!isArray(varName)) {
                throw new RuntimeException("Line " + currentToken.line + ": Variable '" + varName + "' is not an array");
            }
            consume(TokenType.LBRACKET);
            TokenType indexType = arithmeticExpression();
            if (indexType != TokenType.INT) {
                throw new RuntimeException("Line " + currentToken.line + ": Array index must be an integer");
            }
            consume(TokenType.RBRACKET);
        }
        
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
    }

    private void writeStatement() {
        consume(TokenType.PRINTF);
        consume(TokenType.LPAREN);
        expressionList();
        consume(TokenType.RPAREN);
        consume(TokenType.SEMICOLON);
    }
    
    private void expressionList() {
        expression();
        while (currentToken.type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            expression();
        }
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
            return;
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
            String name = currentToken.lexeme;
            consume(TokenType.ID);
            
            if (currentToken.type == TokenType.LPAREN) {
                return functionCall(name);
            }
            
            checkVariableDeclared(name);
            TokenType type = getVariableType(name);
            
            if (currentToken.type == TokenType.LBRACKET) {
                // Array access
                if (!isArray(name)) {
                    throw new RuntimeException("Line " + currentToken.line + ": Variable '" + name + "' is not an array");
                }
                consume(TokenType.LBRACKET);
                TokenType indexType = arithmeticExpression();
                if (indexType != TokenType.INT) {
                    throw new RuntimeException("Line " + currentToken.line + ": Array index must be an integer");
                }
                consume(TokenType.RBRACKET);
            }
            
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
            TokenType type = expression();
            consume(TokenType.RPAREN);
            if (isNegative && type == TokenType.STRING) {
                 throw new RuntimeException("Line " + currentToken.line + ": Operator '-' (negation) invalid for type STRING");
            }
            return type;
        } else {
            throw new RuntimeException("Line " + currentToken.line + ": Invalid factor, expected ID, NUMBER or (expression). Found: " + currentToken.type);
        }
    }
    
    private TokenType functionCall(String name) {
        if (!functionTable.containsKey(name)) {
            throw new RuntimeException("Line " + currentToken.line + ": Function '" + name + "' not defined");
        }
        
        FunctionSignature signature = functionTable.get(name);
        consume(TokenType.LPAREN);
        
        List<TokenType> argTypes = new ArrayList<>();
        if (currentToken.type != TokenType.RPAREN) {
            argTypes = argumentList();
        }
        
        consume(TokenType.RPAREN);
        
        if (signature.paramTypes.size() != argTypes.size()) {
            throw new RuntimeException("Line " + currentToken.line + ": Incorrect number of arguments for function '" + name + "'. Expected " + signature.paramTypes.size() + ", got " + argTypes.size());
        }
        
        for (int i = 0; i < argTypes.size(); i++) {
            checkAssignment(signature.paramTypes.get(i), argTypes.get(i), currentToken.line);
        }
        
        return signature.returnType;
    }

    private List<TokenType> argumentList() {
        List<TokenType> argTypes = new ArrayList<>();
        argTypes.add(expression());
        while (currentToken.type == TokenType.COMMA) {
            consume(TokenType.COMMA);
            argTypes.add(expression());
        }
        return argTypes;
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