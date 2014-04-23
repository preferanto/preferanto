tree grammar JavaPreferantoEval;

options {
  output = template;
  ASTLabelType = PreferantoNode;
  tokenVocab = Preferanto;
}

@header {
  package org.preferanto.antlr;
  import org.preferanto.core.*;
  
  import java.util.Map;
  import java.util.TreeMap;
  import static org.preferanto.core.BuiltInTypeSymbol.*;
  import static org.preferanto.core.Expression.*;
}

@members {
  public SymbolTable symtab;
  public Specification spec;
  private int prefCount = 0;
  private int condPrefCount = 0;
  private int goalCount = 0;
  private boolean hasCond = false;

  public JavaPreferantoEval(TreeNodeStream input, SymbolTable symtab, Specification spec) {
        this(input);
        this.symtab = symtab;
        this.spec = spec;
  }
}

specification : ^(SPECIFICATION quantities preferences) -> spec(spec={spec}, p={$preferences.st}, pc={prefCount}, cpc={condPrefCount});

quantities :  ^(QUANTITIES (q+=quantity)+) -> quantities(q={$q});

quantity : ID (':' type)? ';';

type : TYPE -> {%{$TYPE.text}};

preferences :  ^(PREFERENCES (p+=preference {prefCount++;})+) -> preferences(p={$p});

preference : ^(PREFERENCE {hasCond=false; goalCount=0;} ^(CONDITION (c=condExpr {hasCond=true; condPrefCount++;})?) (g+=goal {goalCount++;})+) 
                  -> pref(idx={prefCount}, c={$c.st}, gc={goalCount}, g={$g});

goal : ^(GOAL e1=condExpr e2=condExpr ^(DIRECTION_NODE (d=DIRECTION)?)) 
                  -> goal(idx={goalCount}, c={hasCond}, t1={$e1.start.expr.type}, e1={$e1.st}, t2={$e2.start.expr.type}, e2={$e2.st}, high={"high".equals($d.text)});

condExpr
         : ^(('|' | '&' | EQ_OP | REL_OP | ADD_OP | MUL_OP) e1=condExpr e2=condExpr) -> binaryOp(op={$start.token.getText()}, e1={$e1.st}, e2={$e2.st})
         | ^(PREF_OP e1=condExpr e2=condExpr) {PrefOp prefOp = PrefOp.fromName($PREF_OP.text);} 
                 -> prefOp(atLeastOne={prefOp==PrefOp.AT_LEAST_ONE}, all={prefOp==PrefOp.ALL}, exactlyOne={prefOp==PrefOp.EXACTLY_ONE}, e1={$e1.st}, e2={$e2.st})
         | ^(DEFAULT_PREF_OP e1=condExpr e2=condExpr) -> prefOp(atLeastOne={true}, all={false}, exactlyOne={false}, e1={$e1.st}, e2={$e2.st})
         | ^(UNARY_ADD_OP ADD_OP e=condExpr) -> unaryAdd(op={$ADD_OP.text}, e={$e.st})
         | ^('!' e=condExpr) -> unaryNot(e={$e.st})
         | ^(MATH_ONE e=condExpr) -> mathOne(f={$MATH_ONE.text}, e={$e.st}) 
         | ^(MATH_TWO e1=condExpr e2=condExpr) -> mathTwo(f={$MATH_TWO.text}, e1={$e1.st}, e2={$e2.st}) 
         | ID -> id(arg={($ID.expr.arg > 0) ? ("" + $ID.expr.arg) : null}, name={$ID.text})
         | INTEGER -> {%{$INTEGER.text}}
         | REAL -> {%{$REAL.text}}
         | BOOLEAN -> {%{$BOOLEAN.text}}
         | STRING -> {%{$STRING.text}}
;
