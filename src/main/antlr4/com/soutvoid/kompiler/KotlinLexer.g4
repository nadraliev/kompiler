lexer grammar KotlinLexer;

WHITESPACE
    : [\t\n\r\f ]+ -> skip
    ;


KEYWORD_var: 'var';
KEYWORD_val: 'val';

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

OPEN_BLOCK
    : '{' -> pushMode(DEFAULT_MODE)
    ;

CLOSE_BLOCK
    : '}' -> popMode
    ;

fragment
LETTER
    : 'a' .. 'z'
    | 'A' .. 'Z'
    | '_'
    ;

SimpleName
    : LETTER (LETTER | DIGIT)*
    ;