parser grammar KotlinParser;

options {tokenVocab=KotlinLexer; }

functionDeclaration
    : modifiers 'fun' SimpleName
    functionParameters
    (':' type)?
    functionBody
    ;

functionParameters
    : '(' (functionParameter (',' functionParameter)*)? ')'
    ;

functionParameter
    : modifiers ('val' | 'var')? parameter ('=' expression)?
    ;

parameter
    : SimpleName ':' type
    ;

type
    : userType '?'?
    | 'dynamic' '?'?
    ;

userType
    : simpleUserType ('.' simpleUserType)*
    ;

simpleUserType
    : SimpleName ('<' simpleUserType_typeParam (',' simpleUserType_typeParam)* '>')?
    ;

simpleUserType_typeParam
    : ('*' | type)
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
    : modifiers ('var' | 'val') SimpleName
    (':' type)?
    ('=' expression)?
    ;

expression
    : atomicExpression ('=' atomicExpression)*
    ;

atomicExpression
    : '(' expression ')'
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

modifiers
    : modifier*
    ;

modifier
    : visibilityModifier
    | hierarchyModifier
    | classModifier
    ;

visibilityModifier
    : 'public'
    | 'private'
    | 'internal'
    | 'protected'
    ;

hierarchyModifier
    : 'abstract'
    | 'open'
    | 'final'
    | 'override'
    ;

classModifier
    : 'data'
    | 'enum'
    | 'annotation'
    ;

