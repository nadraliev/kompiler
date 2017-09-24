parser grammar KotlinParser;

options {tokenVocab=KotlinLexer; }

classDeclaration
    : 'class' SimpleName
    classBody
    ;

classBody
    : '{' (propertyDeclaration
    | functionDeclaration)* '}'
    ;

functionDeclaration
    : 'fun' SimpleName
    functionParameters
    (':' type)?
    functionBody
    ;

functionParameters
    : '(' (functionParameter (',' functionParameter)*)? ')'
    ;

functionParameter
    : ('val' | 'var')? parameter ('=' expression)?
    ;

parameter
    : SimpleName ':' type
    ;

type
    : 'Int'
    | 'Boolean'
    | 'Double'
    ;

functionBody
    : ('{' statements '}')
    | ('=' statement)
    ;

statement
    : declaration
    | expression
    ;

statements
    : ';'* (statement (';'* statement)*)? ';'*
    ;

declaration
    : propertyDeclaration
    ;

propertyDeclaration
    : ('var' | 'val') SimpleName
    (':' type)
    ('=' expression)?
    ;

expression
    : '(' expression ')'
    | expression '=' expression
    | identifier
    | literalConstant
    ;

identifier
    : SimpleName
    ;

literalConstant
    : IntegerLiteral
    | 'true' | 'false'
    ;
