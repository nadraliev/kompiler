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
    : parameter ('=' expression)?
    ;

parameter
    : SimpleName ':' type
    ;

type
    : 'Int'
    | 'Boolean'
    | 'Double'
    | 'String'
    ;

functionBody
    : ('{' statements '}')
    | ('=' statement)
    ;

statement
    : declaration
    | expression
    | ifStatement
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
    | functionCall
    | identifier
    | literalConstant
    | comparison
    ;

functionCall
    : SimpleName '(' identifiers ')' ';'*
    ;

ifStatement
    : 'if' '(' expression ')'
    statement
    | ('{' statements '}')
    ;

comparison
    : (functionCall
    | identifier
    | literalConstant)
    ('==' | '<' | '>' | '<=' | '>=')
    (functionCall
    | identifier
    | literalConstant)
    ;

identifiers
    : (identifier (',' identifier)*)?
    ;

identifier
    : SimpleName
    ;

literalConstant
    : IntegerLiteral
    | 'true' | 'false'
    | DoubleLiteral
    | StringLiteral
    ;
