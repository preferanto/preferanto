tree grammar DefaultPrefOpSetter;

options {
  output = AST;
  ASTLabelType = PreferantoNode;
  tokenVocab = Preferanto;
  filter=true;
}

@header {
  package org.preferanto.antlr;
  import org.preferanto.core.*;

  import static org.preferanto.core.Expression.*;
}

bottomup : rule1;

rule1
@after{e1.expr.arg=0;}
    : ^(DEFAULT_PREF_OP e1=. {$e1.expr.prefOp != null}? e2=.) -> $e1 ; 
  