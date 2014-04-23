tree grammar PreferantoTree;

options {
  output = AST;
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
  public final SymbolTable symtab = new SymbolTable();
  public final Specification spec = new Specification();
}


specification
@init{ThreadLocalContext.setInput(input);} 
      : ^(SPECIFICATION quantities preferences);

quantities :  ^(QUANTITIES (quantity {spec.quantities.add($quantity.symbol);})+) ;

quantity returns [QuantitySymbol symbol]
@init{Type type=_REAL;}
      : ID (':' type {type=$type.tsym;})? ';' {$symbol=new QuantitySymbol($ID.text, type); symtab.putSymbol($symbol);};

type returns [Type tsym]
      : TYPE {$tsym = (Type)symtab.getSymbol($TYPE.text);};

preferences :  ^(PREFERENCES (preference {spec.preferences.add($preference.pref);})+);

preference returns [Preference pref]
@init{$pref = new Preference();}
        : ^(PREFERENCE condition {$pref.condition=$condition.info;}
        (g = goal {$pref.goals.add(g.goal);})+);
        
goal returns [Goal goal] 
@init{$goal = new Goal();}
        : ^(GOAL    
        e1=condExpr {$e1.info.arg=FIRST; $goal.rule1=$e1.info;} 
        e2=condExpr {$e2.info.arg=SECOND; $goal.rule2=$e2.info;} 
        direction {$goal.direction=$direction.dir;});

condition returns [Expression info]
@init{$info = new Expression(); $info.type=_BOOLEAN;}
        : ^(CONDITION (^(DEFAULT_PREF_OP e1=condExpr {checkBoolean($e1.info.type); $info=$e1.info; $e1.info.arg=FIRST;} e2=condExpr {$e2.info.arg=SECOND;}))?);

direction returns [Direction dir]
@init{$dir = Direction.LOW;} 
        : ^(DIRECTION_NODE (d=DIRECTION{$dir=Direction.fromName($d.text);})?);

condExpr returns [Expression info]
@init{$info = new Expression();}
@after{$condExpr.tree.expr=$info;}
         : ^(('|' | '&' | EQ_OP | REL_OP) e1=condExpr e2=condExpr {$info.type=_BOOLEAN; $info.prefOp=getResultPrefOpBool($e1.info.prefOp, $e2.info.prefOp);}) 
         | ^((ADD_OP | MUL_OP) e1=condExpr e2=condExpr {$info.type=getResultType($e1.info.type, $e2.info.type); $info.prefOp=getResultPrefOpMath($e1.info.prefOp, $e2.info.prefOp);})
         | ^(PREF_OP {$info.prefOp=PrefOp.fromName($PREF_OP.text);} 
                 e1=condExpr {checkPrefOp($info.prefOp, $e1.info); $e1.info.arg=FIRST; $info.type=$e1.info.type;}
                 e2=condExpr {checkPrefOp($info.prefOp, $e2.info); $e2.info.arg=SECOND;})
         | ^(UNARY_ADD_OP ADD_OP e=condExpr {checkNumeric($e.info.type); $info.type=$e.info.type; $info.prefOp=getResultPrefOpMath($e.info.prefOp, null);})
         | ^('!' e=condExpr {checkBoolean($e.info.type); $info.type=_BOOLEAN; $info.prefOp=getResultPrefOpBool($e.info.prefOp, null);})
         | ^(MATH_ONE {$info.type=_REAL;} e=condExpr {checkNumeric($e.info.type); $info.prefOp=getResultPrefOpMath($e.info.prefOp, null);}) 
         | ^(MATH_TWO {$info.type=_REAL;} e1=condExpr {checkNumeric($e1.info.type);}
                  e2=condExpr {checkNumeric($e2.info.type); $info.prefOp=getResultPrefOpMath($e1.info.prefOp, $e2.info.prefOp);}) 
         | ID { $info.type = symtab.getSymbol($ID.text).type;}
         | INTEGER { $info.type = _INTEGER;}
         | REAL { $info.type = _REAL;}
         | BOOLEAN { $info.type = _BOOLEAN;}
         | STRING { $info.type = _STRING;}
;
