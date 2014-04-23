tree grammar ByteCodeBuilder;

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
  public Specification spec;
  private ByteCodeHelper bch;

  public ByteCodeBuilder(TreeNodeStream input, Specification spec) {
        this(input);
        this.spec = spec;
        this.bch = new ByteCodeHelper(spec);
  }
  
  public byte[] getEvalByteCode() {
    return bch.getEvalByteCode();
  }
  
  public byte[] getObjectivesByteCode() {
    return bch.getObjectivesByteCode();
  }
  
  public String getPrefEvalClassNameDot() {
    return bch.getPrefEvalClassNameDot();
  }
  
  public String getPrefObjectivesClassNameDot() {
    return bch.getPrefObjectivesClassNameDot();
  }
}



specification
@init{bch.startImpl();} 
@after{bch.endImpl();} 
      : ^(SPECIFICATION quantities preferences);

quantities :  ^(QUANTITIES quantity+) ;

quantity
@init{Type type=_REAL;}
      : ID (':' TYPE)? ';';

preferences 
@init{bch.startPreferences();} 
@after{bch.endPreferences();} 
      :  ^(PREFERENCES (preference)+);

preference
@init{ boolean hasCondition = false; int goalCount = 0; bch.startPreference();}
        : ^(PREFERENCE 
        ^(CONDITION (condExpr {hasCondition=true;})?) {bch.preference(hasCondition);}
        (goal {goalCount++;})+ 
        {bch.endPreference(goalCount);});

goal
@init{ Direction dir = Direction.LOW;}
        : ^(GOAL
        e1=condExpr
        e2=condExpr
        ^(DIRECTION_NODE (DIRECTION{dir=Direction.fromName($DIRECTION.text) ;})?) 
        {bch.goal($e1.tree.expr.type, dir) ;});


condExpr
         : ^(op=('|' | '&' | EQ_OP | REL_OP) e1=condExpr e2=condExpr { bch.logicalOp($op.text, $e1.tree.expr.type, $e2.tree.expr.type) ;}) 
         | ^(op=(ADD_OP | MUL_OP) e1=condExpr e2=condExpr { bch.arithmOp($op.text, $e1.tree.expr.type, $e2.tree.expr.type) ;})
         | ^(PREF_OP e1=condExpr e2=condExpr { bch.prefOp($PREF_OP.text, $e1.tree.expr.type) ;})
         | ^(DEFAULT_PREF_OP e1=condExpr e2=condExpr {bch.prefOp(PrefOp.AT_LEAST_ONE.name(), $e1.tree.expr.type) ;})
         | ^(UNARY_ADD_OP ADD_OP e=condExpr { bch.unaryAdd($ADD_OP.text, $e.tree.expr.type) ;})
         | ^('!' condExpr { bch.negate() ;})
         | ^(MATH_ONE e=condExpr { bch.pushMathOne($MATH_ONE.text, $e.tree.expr.type) ;})
         | ^(MATH_TWO e1=condExpr e2=condExpr { bch.pushMathTwo($MATH_TWO.text, $e1.tree.expr.type, $e2.tree.expr.type) ;}) 
         | ID { bch.pushId($ID.text, $ID.expr.arg) ;}
         | INTEGER { bch.pushInteger($INTEGER.text) ;}
         | REAL { bch.pushReal($REAL.text) ;}
         | BOOLEAN { bch.pushBoolean($BOOLEAN.text) ;}
         | STRING { bch.pushString($STRING.text) ;}
;
