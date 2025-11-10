import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java Main <path_to_source_file>");
            System.exit(1);
        }

        String sourceFile = args[0];
        try {
            String sourceCode = new String(Files.readAllBytes(Paths.get(sourceFile)));
            
            LexicalAnalyzer lexer = new LexicalAnalyzer(sourceCode);
            SyntacticAnalyzer parser = new SyntacticAnalyzer(lexer);
            
            parser.program();
            
            System.out.println("Compilation successful! The code is syntactically correct.");

        } catch (IOException e) {
            System.err.println("Error reading file: " + sourceFile);
            e.printStackTrace();
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Compilation error: " + e.getMessage());
            System.exit(1);
        }
    }
}