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
    block
    ;

functionParameters
    : '(' (functionParameter (',' functionParameter)*)? ')'
    ;

functionParameter
    : parameter
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

statement
    : declaration   #declarationStatement
    | expression    #expressionStatement
    | assignment    #assignmentStatement
    | ifSt           #ifStatement
    | loop          #loopStatement
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
    ('=' expression)
    ;

assignment
    : identifier '=' expression
    ;

expression
    : '(' expression ')' #parenExpression
    | literalConstant   #literal
    | functionCall      #funcCall
    | identifier        #id
    | left=expression operator=('==' | '<' | '>' | '<=' | '>=') right=expression #binaryOperation
    ;

functionCall
    : SimpleName '(' identifiers ')' ';'*
    ;

ifSt
    : 'if' '(' expression ')'
    (statement
    | block)
    ;

block
    : '{' statements '}'
    ;


loop
    : whileLoop
    ;

whileLoop
    : 'while' '(' expression ')'
    (statement
    | block)
    ;

literalConstant
    : IntegerLiteral    #intLit
    | DoubleLiteral     #doubleLit
    | BooleanLiteral    #booleanLit
    | StringLiteral     #stringLit
    ;


identifiers
    : (identifier (',' identifier)*)?
    ;

identifier
    : SimpleName
    ;