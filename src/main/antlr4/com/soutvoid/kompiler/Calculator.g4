grammar Calculator ;

expression
    :   left=expression (MUL|DIV) right=expression # MulDiv
    |   left=expression (ADD|SUB) right=expression # AddSub
    |   func #Function
    |   INT #Int
    |   LPAREN expression RPAREN #Parenthesis ;

func
   : funcname LPAREN expression RPAREN
   ;

funcname
   : SQR
   | CUB
   ;

MUL : '*' ;
DIV : '/' ;
ADD : '+' ;
SUB : '-' ;
SQR : 'sqr';
CUB : 'cube';
LPAREN: '(';
RPAREN: ')';
INT : [0-9]+ ;
WS  : [ \t\r\n\f]+ -> skip ;