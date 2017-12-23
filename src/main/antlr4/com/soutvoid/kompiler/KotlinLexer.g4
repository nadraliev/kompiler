lexer grammar KotlinLexer;

WHITESPACE
    : [\t\n\r\f ]+ -> channel(HIDDEN)
    ;

KEYWORD_var: 'var';
KEYWORD_val: 'val';
KEYWORD_class: 'class';
KEYWORD_function: 'fun';
KEYWORD_external: 'external';

TYPE_Int: 'Int';
TYPE_Boolean: 'Boolean';
TYPE_Double: 'Double';
TYPE_String: 'String';
TYPE_Array: 'Array';

KEYWORD_if: 'if';
KEYWORD_else: 'else';

KEYWORD_while: 'while';
KEYWORD_for: 'for';

KEYWORD_in: 'in';

KEYWORD_return: 'return';

fragment
DIGIT
    : '0' .. '9'
    ;


IntegerLiteral
    : SUB? ('0' | '1' .. '9' DIGIT*)
    ;

DoubleLiteral
    : (IntegerLiteral'.'('0' | '1' .. '9' DIGIT*));

BooleanLiteral
    : 'true'
    | 'false'
    ;

SEMICOLON: ';';

COLON : ':';

MUL: '*';
DIV: '/';

AT: '@';

ADD: '+';
SUB: '-';

INC: '++';
DEC: '--';

EQ: '=';
EQEQ: '==';
NOTEQ: '!=';

LT: '<';
GT: '>';

LTE: '<=';
GTE: '>=';

OPEN_BLOCK
    : '{' -> pushMode(DEFAULT_MODE)
    ;

CLOSE_BLOCK
    : '}' -> popMode
    ;

LPAREN: '(';
RPAREN: ')';

LSQBR: '[';
RSQBR: ']';

COMMA: ',';
DOT: '.';
DOTDOT: '..';
QUESTION: '?';

DOUBLE_QUOTES: '"';

fragment
LETTER
    : 'a' .. 'z'
    | 'A' .. 'Z'
    | '_'
    ;

SimpleName
    : LETTER (LETTER | DIGIT)*
    ;

StringLiteral
  : UnterminatedStringLiteral DOUBLE_QUOTES
  ;

UnterminatedStringLiteral
  : DOUBLE_QUOTES (~["\\\r\n] | '\\' (. | EOF))*
  ;