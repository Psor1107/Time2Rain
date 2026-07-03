# Linguagem para simulação de runs de Risk of Rain 2 (RiskyLang)

- Gabriel Lucchetta Garcia Sanchez - 828513
- Bruno Nieri Nunes - 820590
- Pietro Bernardo Dutra Scaglione - 824375

## 1. Sobre o Programa

O programa consiste na implementação de um compilador para uma Linguagem focada na simulação de *runs* do jogo *Risk of Rain 2*. O objetivo da linguagem é permitir que o usuário declare o estado atual de uma partida (sobrevivente, dificuldade, tempo e estágio) e o seu inventário de itens, utilizando uma estrutura léxica com suporte a controle de fluxo dinâmico, variáveis locais e expressões matemáticas.

Ao final da compilação e interpretação do código fonte (arquivos com extensão `.ror2`), o sistema gera um arquivo de texto formatado contendo um relatório de simulação. Este relatório exibe:

* As configurações da *run*;
* O inventário final consolidado do jogador;
* O cálculo de DPS (Dano Por Segundo) considerando as sinergias e bônus dos itens;
* O *Time To Kill* (TTK), que representa o tempo estimado para derrotar os inimigos do estágio atual, ajustado pela dificuldade e progressão temporal.

## 2. Estrutura da Linguagem e Sintaxe

A linguagem RiskyLang opera dentro de um bloco delimitador global chamado `Run`. Todo o código deve estar encapsulado neste bloco. A linguagem é *case-sensitive* e permite a adição de comentários utilizando `//` para linha única e `/* */` para múltiplas linhas.

### 2.1. Declarações de Estado

O estado inicial da partida deve ser definido diretamente na raiz do bloco principal. Os comandos obrigatórios são:

* `Survivor: <NomeDoSobrevivente>` (Ex: Commando, Huntress)
* `Difficulty: <NomeDaDificuldade>` (Ex: Drizzle, Rainstorm, Monsoon)
* `Time: <Minutos>`
* `Stage: <Nível>`

Modificadores opcionais podem ser declarados através da lista de Artefatos:

* `Artifacts: [<Artefato1>, <Artefato2>]`

### 2.2. Variáveis e Operações Matemáticas

A linguagem suporta alocação de memória local através da palavra reservada `let`. É possível realizar operações matemáticas (`+`, `-`, `*`, `/`) utilizando números inteiros, outras variáveis, ou as palavras reservadas `Time` e `Stage` (que injetam os valores do estado atual da *run* na equação).

```text
let multiplicador = 2
let nivel_ameaca = (Stage * 2) + (Time / 10)

```

### 2.3. Controle de Fluxo

É possível utilizar condicionais lógicas (`if`) para aplicar blocos de código dinamicamente. Os operadores suportados incluem relações matemáticas (`==`, `!=`, `<`, `>`, `<=`, `>=`) e booleanas (`and`, `or`). O bloco `if` cria seu próprio escopo léxico na tabela de símbolos.

```text
if (nivel_ameaca >= 15 and Time <= 60) {
    let mitigacao = 5
}

```

### 2.4. Inserção de Itens

Os itens devem ser precedidos por uma quantidade (que pode ser um número ou uma expressão matemática) seguida do caractere `x` e do nome do item. Podem ser agrupados em um bloco global `Items {}` ou inseridos de forma direta (inline) dentro de um bloco `if`.

```text
// Declaração no escopo global
Items {
    10 x LensMakersGlasses
}

// Declaração dinâmica condicionada
if (Stage > 5) {
    (multiplicador * 3) x AtGMissileMk1
}

```

## 3. Regras Semânticas

O compilador executa uma análise estática rigorosa antes de interpretar o código. Caso alguma das regras abaixo seja violada, a compilação é abortada e uma mensagem de erro é exibida no terminal:

1. **Validação de Escopo e Declaração:** Não é permitido utilizar ou reatribuir uma variável que não foi previamente declarada com `let`.
2. **Prevenção de Redeclaração:** Não é permitido declarar uma variável com o mesmo nome em um escopo onde ela já existe.
3. **Validação de Domínio:** O compilador consulta um banco de dados interno. Não é permitido declarar Sobreviventes, Dificuldades, Artefatos ou Itens que não existam no jogo.
4. **Proteção Matemática:** A árvore de avaliação de expressões bloqueia explicitamente divisões por zero.
5. **Regras de Equipamento:** Não é permitido possuir mais de um item classificado como "Equipamento" (Ex: `ExecutiveCard`).
6. **Avisos Não-Fatais (Warnings):** O sistema emite um alerta caso a quantidade do item `LensMakersGlasses` ultrapasse o limite lógico do jogo (10 unidades para 100% de chance de acerto crítico), mas permite que a compilação prossiga.

## 4. Como Compilar e Executar

### 4.1. Requisitos Prévios

Para compilar e executar o compilador, os seguintes softwares devem estar instalados e configurados nas variáveis de ambiente do sistema:

* **Java JDK** (23 ou superior)
* **Apache Maven** (Versão 3.x)

### 4.2. Compilação

Abra o terminal na pasta raiz do projeto (diretório `Time2Rain`) e execute o comando abaixo. O Maven irá baixar as dependências do ANTLR4, gerar os *Lexers*, *Parsers* e *Visitors*, e compilar o arquivo executável.

```bash
mvn clean package

```

A mensagem `BUILD SUCCESS` indicará que o arquivo `.jar` foi gerado com sucesso no diretório `target`.

### 4.3. Execução

Para executar a análise em um script da linguagem, utilize o comando `java -jar` apontando para o compilador recém-criado, passando como argumentos o arquivo de entrada (`.ror2`) e o destino do arquivo de saída (`.txt`).

**Sintaxe de Execução:**

```bash
java -jar target\riskylang-1.0.0-jar-with-dependencies.jar <caminho_entrada> <caminho_saida>

```

**Exemplo de Execução Manual (Testes Embutidos):**
No diretório do projeto, existe uma pasta `testes` contendo arquivos de validação. Para rodar o teste básico e gerar o relatório na pasta `testes_saida`, utilize o comando:

```bash
java -jar target\riskylang-1.0.0-jar-with-dependencies.jar testes\01_als_basico.ror2 testes_saida\out.txt

```

Após a execução bem-sucedida, abra o arquivo `testes_saida\out.txt` para visualizar o relatório gerado pela simulação. Caso o script contenha erros sintáticos ou semânticos, a execução será interrompida e os detalhes da falha serão exibidos no próprio arquivo de saída.

### [Vídeo demonstração](https://drive.google.com/file/d/1KyLY6M5uHG43ieUXACyqZQujlL3YHvTA/view?usp=sharing)

