# Linguagem Nickbolt

Uma linguagem de programação simples com sintaxe similar ao C, desenvolvida para fins educacionais.

## Características

- Tipagem estática com tipos básicos (int, float, string)
- Estruturas de controle (if/else, for, while)
- Operadores aritméticos, relacionais e lógicos
- Funções de entrada/saída (scanf, printf)
- Comentários de linha e bloco
- Suporte a strings com escape sequences

## Compilação e Execução

```bash
# Compilar o compilador
javac *.java

# Executar um programa Nickbolt
java Main programa.nb
```

## Sintaxe

### Tipos de Dados

```nickbolt
int x, y, contador;
float salario, resultado;
string nome, mensagem;
```

### Operadores

- **Aritméticos**: `+`, `-`, `*`, `/`
- **Relacionais**: `==`, `!=`, `<`, `>`, `<=`, `>=`
- **Lógicos**: `&&`, `||`, `!`
- **Incremento/Decremento**: `++`, `--`
- **Atribuição**: `=`

### Estruturas de Controle

#### Condicional
```nickbolt
if (x > 10) {
    printf("x é maior que 10");
} else {
    printf("x é menor ou igual a 10");
}
```

#### Laço For
```nickbolt
for (i = 0; i < 10; i++) {
    printf("Contador: ", i);
}
```

#### Laço While
```nickbolt
while (x > 0) {
    x = x - 1;
}
```

### Entrada e Saída

```nickbolt
scanf(variavel);                    // Leitura
printf("Mensagem");                 // Saída simples
printf("Valor: ", x);               // Saída com variável
printf("x: ", x, " y: ", y);        // Múltiplos argumentos
```

### Strings

```nickbolt
string texto = "Hello World";
string escape = "Linha 1\nLinha 2\tTab";
string aspas = "String com \"aspas\"";
```

### Comentários

```nickbolt
// Comentário de linha

/* Comentário
   de múltiplas
   linhas */
```

## Exemplo Completo

```nickbolt
int x, y, resultado;
float media;
string nome;

// Entrada de dados
printf("Digite seu nome: ");
scanf(nome);
printf("Digite dois números: ");
scanf(x);
scanf(y);

// Processamento
resultado = x + y;
media = (x + y) / 2.0;

// Saída
printf("Olá, ", nome);
printf("Soma: ", resultado);
printf("Média: ", media);

// Estrutura condicional
if (media >= 7.0) {
    printf("Aprovado!");
} else {
    printf("Reprovado!");
}
```

## Arquivos de Exemplo

- `programa_correto.nb` - Exemplo básico da linguagem
- `programa_correto_2.nb` - Exemplo com recursos avançados
- `programa_com_erro.nb` - Exemplo com erros sintáticos
- `programa_com_erro_2.nb` - Outro exemplo com erros

## Estrutura do Compilador

- **AnalisadorLexico.java** - Análise léxica (tokenização)
- **AnalisadorSintatico.java** - Análise sintática
- **TipoToken.java** - Definição dos tipos de tokens
- **Token.java** - Classe para representar tokens
- **Main.java** - Ponto de entrada do compilador

## Tratamento de Erros

O compilador detecta e reporta:
- Erros léxicos (caracteres inválidos)
- Erros sintáticos (estrutura incorreta)
- Strings não terminadas
- Tokens inesperados

## Limitações

- Não suporta funções definidas pelo usuário
- Não possui arrays ou estruturas de dados complexas
- Escopo global apenas
- Sem verificação semântica (tipos)