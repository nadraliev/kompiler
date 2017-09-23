parser grammar KotlinParser;

options {tokenVocab=KotlinLexer; }


topLevelObject
    : '{' statement+ '}'
    ;

statement
    : variableDeclInit
    | variableDeclaration
    | variableInitialization
    ;

variableDeclaration :
    'var'
    SimpleName;

variableInitialization:
    SimpleName
    '=' IntegerLiteral
    ;

variableDeclInit:
    'var'
    SimpleName
    '=' IntegerLiteral
    ;

identifier
    : SimpleName
    ;