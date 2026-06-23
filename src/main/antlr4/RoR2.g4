grammar RoR2;

program         : runDeclaration EOF;
runDeclaration  : 'Run' '{' runBody '}';
runBody         : runStatement*;
runStatement    : survivorStatement
                 | difficultyStatement
                 | timeStatement
                 | stageStatement
                 | artifactsStatement
                 | itemsBlock;

survivorStatement   : 'Survivor' ':' IDENTIFIER;
difficultyStatement : 'Difficulty' ':' IDENTIFIER;
timeStatement       : 'Time' ':' INTEGER;
stageStatement      : 'Stage' ':' INTEGER;
artifactsStatement  : 'Artifacts' ':' '[' artifactList? ']';
artifactList        : IDENTIFIER (',' IDENTIFIER)*;
itemsBlock          : 'Items' '{' itemLine* '}';
itemLine            : INTEGER 'x' IDENTIFIER;

IDENTIFIER : [A-Za-z][A-Za-z0-9]*;
INTEGER    : [0-9]+;
WS         : [ \t\r\n]+ -> skip;
COMMENT    : '//' ~[\r\n]* -> skip;
ML_COMMENT : '/*' .*? '*/' -> skip;
