lexer grammar KotlinLexer;

WHITESPACE
    : [\t\n\r\f ]+ -> skip
    ;


KEYWORD_var: 'var';
KEYWORD_val: 'val';
KEYWORD_class: 'class';
KEYWORD_function: 'fun';

TYPE_Int: 'Int';
TYPE_Boolean: 'Boolean';
TYPE_Double: 'Double';

LITERAL_true: 'true';
LITERAL_false: 'false';

fragment
DIGIT
    : '0' .. '9'
    ;


IntegerLiteral
    : ('0' | '1' .. '9' DIGIT*)
    ;

SEMICOLON: ';';

COLON : ':';

EQ: '=';

LT: '<';
GT: '>';

OPEN_BLOCK
    : '{' -> pushMode(DEFAULT_MODE)
    ;

CLOSE_BLOCK
    : '}' -> popMode
    ;

LPAREN: '(';
RPAREN: ')';

COMMA: ',';
DOT: '.';
STAR: '*';
QUESTION: '?';

fragment
LETTER
    : 'a' .. 'z'
    | 'A' .. 'Z'
    | '_'
    ;

SimpleName
    : LETTER (LETTER | DIGIT)*
    ;