grammar RoR2;


// Esta gramática define uma DSL declarativa para simular o estado
// e o inventário de uma "Run" no jogo Risk of Rain 2.
// Suporta escopos dinâmicos, controle de fluxo e expressões matemáticas.


// --- PONTO DE ENTRADA ---
program         : runDeclaration EOF;
runDeclaration  : 'Run' '{' runBody '}';
runBody         : runStatement*;

// --- DECLARAÇÕES DO BLOCO PRINCIPAL (ROOT) ---
runStatement    : survivorStatement
                 | difficultyStatement
                 | timeStatement
                 | stageStatement
                 | artifactsStatement
                 | itemsBlock
                 | variableDeclaration
                 | assignment
                 | ifStatement;

// --- CONFIGURAÇÕES DE ESTADO DO JOGO ---
survivorStatement   : 'Survivor' ':' IDENTIFIER;
difficultyStatement : 'Difficulty' ':' IDENTIFIER;
timeStatement       : 'Time' ':' INTEGER;
stageStatement      : 'Stage' ':' INTEGER;
artifactsStatement  : 'Artifacts' ':' '[' artifactList? ']';
artifactList        : IDENTIFIER (',' IDENTIFIER)*;

// --- GERENCIAMENTO DE MEMÓRIA E CONTROLE DE FLUXO ---
// Declaração (aloca nova variável) e Atribuição (atualiza existente)
variableDeclaration : 'let' IDENTIFIER '=' expression;
assignment          : IDENTIFIER '=' expression;

// Bloco condicional que cria seu próprio escopo léxico na SymbolTable
ifStatement         : 'if' '(' logicalExpression ')' '{' blockStatement* '}';

// Restringe o que pode ser declarado dentro de um 'if' (não permite 'Items {}')
blockStatement      : variableDeclaration
                     | assignment
                     | itemLine      // Permite adicionar itens diretamente no escopo
                     | ifStatement;  // Suporta ifs aninhados

// --- SISTEMA DE INVENTÁRIO ---
// Bloco agrupador exclusivo do escopo raiz
itemsBlock      : 'Items' '{' itemLine* '}';

// Regra base: uma expressão matemática, o literal 'x' e o nome do item
itemLine        : expression 'x' IDENTIFIER;

// --- EXPRESSÕES MATEMÁTICAS (RESOLUÇÃO TOP-DOWN PRECEDENTE) ---
// A gramática garante que multiplicação/divisão ocorra antes de soma/subtração
expression         : multiplicativeExpr (('+'|'-') multiplicativeExpr)*;
multiplicativeExpr : unaryExpr (('*'|'/') unaryExpr)*;
unaryExpr          : ('+'|'-')? primaryExpr;

// Folhas da árvore matemática (podem ser números, variáveis ou contexto do jogo)
primaryExpr        : INTEGER
                 | IDENTIFIER
                 | 'Time'       // Injeta o tempo global na equação
                 | 'Stage'      // Injeta o estágio global na equação
                 | '(' expression ')';

// --- EXPRESSÕES LÓGICAS (PARA BLOCOS CONDICIONAIS) ---
logicalExpression  : logicalOrExpr;
logicalOrExpr      : logicalAndExpr ('or' logicalAndExpr)*;
logicalAndExpr     : relationalExpr ('and' relationalExpr)*;
relationalExpr     : expression (('=='|'!='|'<'|'>'|'<='|'>=') expression)?;

// --- ANÁLISE LÉXICA (TOKENS RECONHECIDOS E IGNORADOS) ---

IDENTIFIER : [A-Za-z_][A-Za-z0-9_]*; // Variáveis devem começar com letra ou underline
INTEGER    : [0-9]+;

// Ignora espaços em branco, tabulações e quebras de linha na compilação
WS         : [ \t\r\n]+ -> skip;

// Permite que o usuário do script comente seu próprio código sem quebrar o Parser
COMMENT    : '//' ~[\r\n]* -> skip;
ML_COMMENT : '/*' .*? '*/' -> skip;
