parser grammar KotlinParser;

options {tokenVocab=KotlinLexer; }

file
    : (classDeclaration
    | propertyDeclaration
    | functionDeclaration)*
    ;

classDeclaration
    : 'class' SimpleName
    classBody
    ;

classBody
    : '{' (propertyDeclaration
    | functionDeclaration)* '}'
    ;

annotation
    : AT SimpleName ('(' literalConstant ')')?
    ;

externalModificator
    : 'external'
    ;

functionDeclaration
    : annotation?
    externalModificator? 'fun' SimpleName
    functionParameters
    (':' type)?
    body=functionBody?
    ;

functionBody
    : '{' statements '}'
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
    : 'Int'     #int
    | 'Boolean' #bool
    | 'Double'  #double
    | 'String'  #string
    | ('Array''<'type'>')   #array
    ;

statement
    : declaration   #declarationStatement
    | expression    #expressionStatement
    | assignment    #assignmentStatement
    | ifSt           #ifStatement
    | loop          #loopStatement
    | returnStatement   #return
    ;

returnStatement
    : 'return' expression?;

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
    : identifier '=' expression     #simpleIdentAssign
    | arrayAccessExpr '=' expression    #arrayAssign
    ;

expression
    : '(' expression ')' #parenExpression
    | literalConstant   #literal
    | functionCall      #funcCall
    | identifier        #id
    | left=expression operator=('*' | '/') right=expression     #binaryOperation
    | left=expression operator=('+' | '-') right=expression     #binaryOperation
    | left=expression operator=('!=' | '==' | '<' | '>' | '<=' | '>=') right=expression #binaryOperation
    | arrayInitExpr     #arrayInit
    | arrayAccessExpr   #arrayAccess
    | expression '..' expression   #range
    | '++'expression    #increment
    | '--'expression    #decrement
    ;

arrayInitExpr
    : 'Array''<'type'>''('IntegerLiteral')'
    ;

arrayAccessExpr
    : identifier '[' expression ']'
    ;

functionCall
    : SimpleName '(' expressions ')' ';'*
    ;

ifSt
    : 'if' '(' expression ')'
    (mainSt=statement
    | mainBlock=block)
    ('else'
    (elseSt=statement
    | elseBlock=block))?
    ;

block
    : '{' statements '}'
    ;


loop
    : whileLoop     #whileStatement
    | forLoop       #forStatement
    ;

whileLoop
    : 'while' '(' expression ')'
    (statement
    | block)
    ;

forLoop
    : 'for' '(' identifier 'in' expression ')'
    ( statement
    | block )
    ;

literalConstant
    : integerLiteral    #intLit
    | doubleLiteral     #doubleLit
    | BooleanLiteral    #booleanLit
    | StringLiteral     #stringLit
    ;

integerLiteral
    : IntegerLiteral
    | minus='-'IntegerLiteral
    ;

doubleLiteral
    : DoubleLiteral
    | minus='-'DoubleLiteral
    ;


identifiers
    : (identifier (',' identifier)*)?
    ;

expressions
    : (expression (',' expression)*)?
    ;

identifier
    : SimpleName
    ;