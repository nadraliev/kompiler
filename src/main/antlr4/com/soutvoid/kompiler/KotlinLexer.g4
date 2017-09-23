lexer grammar KotlinLexer;

WHITESPACE
    : [\t\n\r\f ]+ -> skip
    ;


KEYWORD_var: 'var';
KEYWORD_val: 'val';
KEYWORD_class: 'class';
KEYWORD_function: 'fun';
KEYWORD_dynamic: 'dynamic';

VISIBILITY_MODIFIER_public: 'public';
VISIBILITY_MODIFIER_private: 'private';
VISIBILITY_MODIFIER_internal: 'internal';
VISIBILITY_MODIFIER_protected: 'protected';

HIERARCHY_MODIFIER_abstract: 'abstract';
HIERARCHY_MODIFIER_open: 'open';
HIERARCHY_MODIFIER_final: 'final';
HIERARCHY_MODIFIER_override: 'override';

CLASS_MODIFIER_data: 'data';
CLASS_MODIFIER_enum: 'enum';
CLASS_MODIFIER_annotation: 'annotation';

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