tree grammar ArgSetter;

options {
  output = AST;
  ASTLabelType = PreferantoNode;
  tokenVocab = Preferanto;
  filter=true;
}

@header {
  package org.preferanto.antlr;
  import org.preferanto.core.*;
}

topdown : rule1 | rule2 | rule3;

rule1 : ^(('|' | '&' | EQ_OP | REL_OP | ADD_OP | MUL_OP | PREF_OP | MATH_TWO) e1=. e2=. {$e1.setArgFromParent(); $e2.setArgFromParent(); }); 
rule2 : ^(UNARY_ADD_OP ADD_OP e=. {$e.setArgFromParent();});
rule3 : ^(('!' | MATH_ONE) e=. {$e.setArgFromParent();});
