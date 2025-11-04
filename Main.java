import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Uso: java Main <caminho_para_o_arquivo_fonte>");
            System.exit(1);
        }

        String arquivoFonte = args[0];
        try {
            String codigo = new String(Files.readAllBytes(Paths.get(arquivoFonte)));
            
            AnalisadorLexico lexer = new AnalisadorLexico(codigo);
            AnalisadorSintatico parser = new AnalisadorSintatico(lexer);
            
            parser.programa(); // Inicia a análise
            
            System.out.println("Compilação bem-sucedida! O código está sintaticamente correto.");

        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo: " + arquivoFonte);
            e.printStackTrace();
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println("Erro de compilação: " + e.getMessage());
            System.exit(1);
        }
    }
}