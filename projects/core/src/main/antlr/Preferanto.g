grammar Preferanto;

options {
  language = Java;
  output = AST;
  ASTLabelType = PreferantoNode;
}
tokens {
   SPECIFICATION;
   PREFERENCE;
   GOAL;
   CONDITION;
   DIRECTION_NODE;
   UNARY_ADD_OP;
   DEFAULT_PREF_OP;
}

@lexer::header{package org.preferanto.antlr;}
 
@header {
  package org.preferanto.antlr;
  import org.preferanto.core.*;
}

@members {
  protected void mismatch(IntStream input, int ttype, BitSet follow) throws RecognitionException {
    throw new MismatchedTokenException(ttype, input);
  }
  public Object recoverFromMismatchedSet(IntStream input, RecognitionException e, BitSet follow) throws RecognitionException {
    throw e;
  }
}

@rulecatch {
  catch (RecognitionException e) {
    throw e;
  }
}

//##########
//# PARSER #
//##########

specification : quantities preferences EOF -> ^(SPECIFICATION quantities preferences);

quantities : QUANTITIES '{' quantity+ '}' -> ^(QUANTITIES quantity+);

quantity : ID (':' TYPE)? ';' ;

preferences : PREFERENCES '{' preference+ '}' -> ^(PREFERENCES preference+);

preference : condition goals ';' -> ^(PREFERENCE condition goals);

goals : goal (',' goal)* -> goal+
  | (lt=REL_OP) {$lt.getText().equals("<") }? goal (',' goal)* (gt=REL_OP) { $gt.getText().equals(">") }? ->  goal+;

goal : addExpr direction -> ^(GOAL addExpr addExpr direction);

condition : ('[' condExpr']')? -> ^(CONDITION ^(DEFAULT_PREF_OP condExpr condExpr)?);

direction : (':' DIRECTION)? -> ^(DIRECTION_NODE DIRECTION?);

condExpr : andExpr ('|'^ andExpr)* ;

andExpr : eqExpr ('&'^ eqExpr)* ;

eqExpr : relExpr (EQ_OP^ relExpr)* ;

relExpr : addExpr (REL_OP^ addExpr)* ;

addExpr : multExpr (ADD_OP^ multExpr)* ;

multExpr : unaryExpr (MUL_OP^ unaryExpr)* ;

unaryExpr : ADD_OP unaryExpr -> ^(UNARY_ADD_OP ADD_OP unaryExpr) 
          | PREF_OP unaryExpr -> ^(PREF_OP unaryExpr unaryExpr)
          | unaryExprPlain ;

unaryExprPlain : '!'^ unaryExpr | primary ;

primary : '(' condExpr ')' -> condExpr | func1 | func2 | ID | INTEGER | REAL | BOOLEAN | STRING ;

func1 : MATH_ONE '(' addExpr ')' -> ^(MATH_ONE addExpr);

func2 : MATH_TWO '(' addExpr ',' addExpr ')' -> ^(MATH_TWO addExpr+);


//#########
//# LEXER #
//#########

QUANTITIES : 'quantities' ;

PREFERENCES : 'preferences' ;

TYPE : 'integer' | 'real' | 'boolean' ;

MATH_ONE : 'sin' | 'cos' | 'asin' | 'acos' | 'tan' | 'atan' | 'sinh' | 'cosh' | 'exp' | 'sqrt' | 'log' | 'log10' | 'abs' | 'ceil' | 'floor' | 'round' ;

MATH_TWO : 'pow' | 'atan2' | 'min' | 'max' ; 

PREF_OP : 'DIFF' | '@' | 'ALL' | '~' | 'EXACTLY_ONE' | '#' | 'AT_LEAST_ONE' ;

ADD_OP : '+' | '-' ;

MUL_OP : '*' | '/' | '%' ;

EQ_OP : '=' | '<>' | '!=' ;

REL_OP : '<=' | '<' | '>=' | '>' ;

BOOLEAN : 'true' | 'false' ;

DIRECTION : 'high' | 'low' ;

ID  : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

INTEGER : '0'..'9'+
    ;

REAL
    :   ('0'..'9')+ '.' ('0'..'9')* EXPONENT?
    |   '.' ('0'..'9')+ EXPONENT?
    |   ('0'..'9')+ EXPONENT
    ;

COMMENT
    :   '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
    |   '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
    ;

WS  :   ( ' '
        | '\t'
        | '\r'
        | '\n'
        ) {$channel=HIDDEN;}
    ;

STRING
    :  '"' ( ESC_SEQ | ~('\\'|'"') )* '"'
    ;

fragment
EXPONENT : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;

fragment
HEX_DIGIT : ('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UNICODE_ESC
    |   OCTAL_ESC
    ;

fragment
OCTAL_ESC
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
UNICODE_ESC
    :   '\\' 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

