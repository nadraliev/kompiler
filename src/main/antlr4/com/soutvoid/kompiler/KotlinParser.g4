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

functionBody
    : ('{' statements '}')
    ;

statement
    : declaration
    | expression
    | assignment
    | ifStatement
    | loop
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
    | functionCall      #funcCall
    | identifier        #id
    | literalConstant   #literal
    | left=expression operator=('==' | '<' | '>' | '<=' | '>=') right=expression #binaryOperation
    ;

functionCall
    : SimpleName '(' identifiers ')' ';'*
    ;

ifStatement
    : 'if' '(' expression ')'
    statement
    | ('{' statements '}')
    ;

loop
    : whileLoop
    ;

whileLoop
    : 'while' '(' expression ')'
    statement
    | ('{' statements '}')
    ;


identifiers
    : (identifier (',' identifier)*)?
    ;

identifier
    : SimpleName
    ;

literalConstant
    : intLit
    | doubleLit
    | booleanLit
    | stringLit
    ;

intLit
    : IntegerLiteral
    ;

doubleLit
    : DoubleLiteral
    ;

booleanLit
    : BooleanLiteral
    ;

stringLit
    : StringLiteral
    ;
