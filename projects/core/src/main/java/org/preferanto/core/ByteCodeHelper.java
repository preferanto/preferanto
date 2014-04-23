package org.preferanto.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteCodeHelper implements Opcodes {
	private static final Logger log = LoggerFactory.getLogger(ByteCodeHelper.class);
	private static final String DYNAMIC_PACKAGE = "org.preferanto.dynamic.";
	private static final String PREF_EVAL_CLASS_NAME_DOT = DYNAMIC_PACKAGE + "PreferantoEvaluatorImpl$";
	private static final String PREF_EVAL_CLASS_NAME_SLASH = PREF_EVAL_CLASS_NAME_DOT.replaceAll("\\.", "/");

	private static final String PREF_OBJECTIVES_CLASS_NAME_DOT = DYNAMIC_PACKAGE + "PreferantoObjectives$";
	private static final String PREF_OBJECTIVES_CLASS_NAME_SLASH = PREF_OBJECTIVES_CLASS_NAME_DOT.replaceAll("\\.", "/");

	private static AtomicInteger CURRENT_CLASS_ID = new AtomicInteger();

	private final int classId = CURRENT_CLASS_ID.getAndIncrement();
	private final String prefEvalClassNameDot = PREF_EVAL_CLASS_NAME_DOT + classId;
	private final String prefEvalClassNameSlash = PREF_EVAL_CLASS_NAME_SLASH + classId;

	private final String prefObjectivesClassNameDot = PREF_OBJECTIVES_CLASS_NAME_DOT + classId;
	private final String prefObjectivesClassNameSlash = PREF_OBJECTIVES_CLASS_NAME_SLASH + classId;
	
	
	private final Specification spec;
	private final Map<String, Type> idTypes = new HashMap<String, Type>();
	
	private final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	private MethodVisitor mv;

	private final ClassWriter cwObjectives = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
	private MethodVisitor mvObjectives;
	private FieldVisitor fvObjectives;

	private int currRuleIndex = 0;
	private int currVarIndex = 2;
	
	private int[] objVarIndex = {0, 0, 0};
	int resultVarIndex;

	private Label lbPreferenceRet;
	private List<Integer> goalResultVarIndexes = new ArrayList<>();

	public ByteCodeHelper(Specification spec) {
        this.spec = spec;
	}

	public String getPrefEvalClassNameDot() {
		return prefEvalClassNameDot;
	}
		
	public String getPrefEvalClassNameSlash() {
		return prefEvalClassNameSlash;
	}
	
	public String getPrefObjectivesClassNameDot() {
		return prefObjectivesClassNameDot;
	}
	
	public String getPrefObjectivesClassNameSlash() {
		return prefObjectivesClassNameSlash;
	}
	
	public void startImpl() {
		log.trace("Entering startImpl().");
		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, prefEvalClassNameSlash, null, "java/lang/Object", new String[] { "org/preferanto/core/PreferantoEvaluator" });

		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		log.trace("Exiting startImpl().");
	}

	public void endImpl() {
		log.trace("Entering endImpl().");

		mv = cw.visitMethod(ACC_PUBLIC, "getRuleCount", "()I", null, null);
		mv.visitCode();
		mv.visitIntInsn(SIPUSH, spec.preferences.size());
		mv.visitInsn(IRETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();		
		cw.visitEnd();
		
		int conditionalRuleCount = 0;
		for(Preference pref : spec.preferences) {
			if(pref.condition != null && pref.condition.prefOp != null) {
				conditionalRuleCount++;
			}
		}		
		mv = cw.visitMethod(ACC_PUBLIC, "getConditionalRuleCount", "()I", null, null);
		mv.visitCode();
		mv.visitIntInsn(SIPUSH, conditionalRuleCount);
		mv.visitInsn(IRETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();		
		cw.visitEnd();
		
		log.trace("Exiting endImpl().");
	}

	private int createDoubleVariable() {
		int varIndex = currVarIndex;
		mv.visitInsn(DCONST_0);
		mv.visitVarInsn(DSTORE, varIndex);
		currVarIndex += 2;
		return varIndex;
	}
	
	private int createObjVariable() {
		int varIndex = currVarIndex;
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, varIndex);
		currVarIndex ++;
		return varIndex;
	}
	
	private String getJavaType(Type type) {
		if(type == BuiltInTypeSymbol._BOOLEAN) return "Z";
		if(type == BuiltInTypeSymbol._REAL) return "D";
		if(type == BuiltInTypeSymbol._INTEGER) return "J";
		return "Ljava/lang/String;";
	}
	
	private String getJavaGetter(Type type) {
		if(type == BuiltInTypeSymbol._BOOLEAN) return "getBoolean";
		if(type == BuiltInTypeSymbol._REAL) return "getDouble";
		if(type == BuiltInTypeSymbol._INTEGER) return "getLong";
		return "getString;";
	}
	
	public void startPreferences() {
		log.trace("Entering startPreferences().");

		cwObjectives.visit(V1_6, ACC_PUBLIC + ACC_SUPER, prefObjectivesClassNameSlash, null, "java/lang/Object", new String[] {});
		
		int qCount = spec.quantities.size();
		for(int i=0; i<qCount; i++) {
			QuantitySymbol quantity = spec.quantities.get(i);			
			String name = quantity.getName();
			Type type = quantity.getType();
			idTypes.put(name, type);
			String fieldType = getJavaType(type);
			fvObjectives = cwObjectives.visitField(ACC_PUBLIC + ACC_FINAL, name, fieldType, null, null);
			fvObjectives.visitEnd();
		}
		
		mvObjectives = cwObjectives.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/preferanto/core/PreferantoContext;)V", null, null);
		mvObjectives.visitCode();		
		
		mvObjectives.visitVarInsn(ALOAD, 0);
		mvObjectives.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

		for(int i=0; i<qCount; i++) {
			QuantitySymbol quantity = spec.quantities.get(i);			
			Type type = quantity.getType();
			String name = quantity.getName();			
			String fieldType = getJavaType(type);
			String getterName = getJavaGetter(type);

			mvObjectives.visitVarInsn(ALOAD, 0);
			mvObjectives.visitVarInsn(ALOAD, 1);
			mvObjectives.visitLdcInsn(name);
			mvObjectives.visitMethodInsn(INVOKEINTERFACE, "org/preferanto/core/PreferantoContext", getterName, "(Ljava/lang/String;)" + fieldType);
			mvObjectives.visitFieldInsn(PUTFIELD, prefObjectivesClassNameSlash, name, fieldType);
		}
		
		mvObjectives.visitInsn(RETURN);
		
		mvObjectives.visitMaxs(0, 0);
		mvObjectives.visitEnd();

		cwObjectives.visitEnd();
		
		log.trace("Exiting startPreferences().");
	}

	public void endPreferences() {
		log.trace("Entering endPreferences().");
		mv = cw.visitMethod(ACC_PUBLIC, "compare", "(ILorg/preferanto/core/PreferantoContext;Lorg/preferanto/core/PreferantoContext;)[D", null, null);
		mv.visitCode();
		
		currVarIndex = 4;
		
		for(int arg = 1; arg <= 2; arg++) {
			objVarIndex[arg] = createObjVariable();
			mv.visitTypeInsn(NEW, prefObjectivesClassNameSlash);
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1 + arg); // push ctx[arg] on the stack
			mv.visitMethodInsn(INVOKESPECIAL, prefObjectivesClassNameSlash, "<init>", "(Lorg/preferanto/core/PreferantoContext;)V");
			mv.visitVarInsn(ASTORE, objVarIndex[arg]);			
		}		

		if(currRuleIndex > 0) {
			mv.visitVarInsn(ILOAD, 1); // push ruleIndex on the stack
			Label[] switchLabels = new Label[currRuleIndex];
			for(int i=0; i<currRuleIndex; i++) {
				switchLabels[i] = new Label();
			}
					
			Label lbSwitchExit = new Label();
			mv.visitTableSwitchInsn(0, currRuleIndex-1, lbSwitchExit, switchLabels);

			for(int i=0; i<currRuleIndex; i++) {
				mv.visitLabel(switchLabels[i]);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitVarInsn(ALOAD, objVarIndex[1]);
				mv.visitVarInsn(ALOAD, objVarIndex[2]);
				mv.visitMethodInsn(INVOKESPECIAL, prefEvalClassNameSlash, "compare" + i, "(L" + prefObjectivesClassNameSlash + ";L" + prefObjectivesClassNameSlash + ";)[D");
				mv.visitInsn(ARETURN);
			}
			mv.visitLabel(lbSwitchExit);
		}

		mv.visitInsn(ACONST_NULL);
		mv.visitInsn(ARETURN);

		mv.visitMaxs(0, 0);
		mv.visitEnd();
		
		log.trace("Exiting endPreferences().");
	}	

	public void startPreference() {
		log.trace("Entering startPreference(): constructing method compare" + currRuleIndex);
		
		mv = cw.visitMethod(ACC_PUBLIC, "compare" + currRuleIndex, "(L" + prefObjectivesClassNameSlash + ";L" + prefObjectivesClassNameSlash + ";)[D", null, null);
		mv.visitCode();

		objVarIndex[1] = 1; 
		objVarIndex[2] = 2; 
				
		currVarIndex = 3;		
		resultVarIndex = createObjVariable();

		log.trace("Exiting startPreference().");
	}

	public void preference(boolean hasCondition) {
		log.trace("Entering preference(" + hasCondition + ")");
		
		lbPreferenceRet = new Label();
		if(hasCondition) {
			mv.visitJumpInsn(IFEQ, lbPreferenceRet);
		}
		goalResultVarIndexes = new ArrayList<>();
	}
    
	public void goal(Type type, Direction dir) {
		log.trace("Entering goal(" + type.getName() + ", " + dir + ")");
		
		int goalResultVarIndex = createDoubleVariable();
		
		int var2Index = storeIntoVar(type);
		int var1Index = storeIntoVar(type);
		
		if(dir == Direction.HIGH) {
			loadFromVar(var2Index, type);
			loadFromVar(var1Index, type);
		} else {
			loadFromVar(var1Index, type);
			loadFromVar(var2Index, type);
		}
		
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			mv.visitMethodInsn(INVOKESTATIC, "org/preferanto/core/Utils", "boolDiff", "(ZZ)D");
		} else if(type == BuiltInTypeSymbol._REAL) {
			mv.visitInsn(DSUB);
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			mv.visitInsn(LSUB);
			mv.visitInsn(L2D);
		} else {
			mv.visitMethodInsn(INVOKESTATIC, "org/preferanto/core/Utils", "StringDiff", "(Ljava/lang/String;Ljava/lang/String;)D");
		}
		mv.visitVarInsn(DSTORE, goalResultVarIndex);
		goalResultVarIndexes.add(goalResultVarIndex);
	}
    
	public void endPreference(int goalCount) {
		log.trace("Entering endPreference(" + goalCount + ")");
		if(goalCount != goalResultVarIndexes.size()) throw new IllegalArgumentException("goalCount = " + goalCount + ", goalResultVarIndexes.size() = " + goalResultVarIndexes.size());
		
		mv.visitIntInsn(BIPUSH, goalCount);
		mv.visitIntInsn(NEWARRAY, T_DOUBLE);
		for(int i=0; i<goalCount; i++) {
			mv.visitInsn(DUP);
			mv.visitIntInsn(BIPUSH, i);
			mv.visitVarInsn(DLOAD, goalResultVarIndexes.get(i));
			mv.visitInsn(DASTORE);
		}
		mv.visitInsn(ARETURN);

		mv.visitLabel(lbPreferenceRet);
		mv.visitInsn(ACONST_NULL);
		mv.visitInsn(ARETURN);		

		mv.visitMaxs(0, 0);
		mv.visitEnd();

		currRuleIndex++;
	}
    
	public void pushBoolean(String value) {
		log.trace("Pushing boolean " + value);
		boolean b = Boolean.parseBoolean(value);
		int opCode = b ? ICONST_1 : ICONST_0;
		mv.visitInsn(opCode);
	}
	
	public void pushString(String value) {
		log.trace("Pushing string " + value);		
		mv.visitLdcInsn(value);
	}
	
	public void pushReal(String value) {
		log.trace("Pushing real " + value);
		mv.visitLdcInsn(new Double(value));
	}
	
	public void pushInteger(String value) {
		log.trace("Pushing integer " + value);
		mv.visitLdcInsn(new Long(value));
	}
	
	public void pushId(String name, int arg) {
		log.trace("Pushing the value of " + name + "#" + arg + " on the stack");		
		Type type = idTypes.get(name);
		String fieldType = getJavaType(type);
		
		log.trace("Reading ctx$" + arg + " from " + type + " / " + fieldType + " variable with index " + objVarIndex[arg]);		
		mv.visitVarInsn(ALOAD, objVarIndex[arg]);
		mv.visitFieldInsn(GETFIELD, prefObjectivesClassNameSlash, name, fieldType);
	}
	
	public void pushMathOne(String funcName, Type type) {
		log.trace("Pushing mathOne " + funcName + "(" + type.getName() + ")");
		
		Class<?>[] prmTypes = getPossibleParameterTypes(type);
		Method method = null;
		for(Class<?> prmType : prmTypes) {
			try {
				method = Math.class.getMethod(funcName, prmType);
				break;
			} catch(Exception e) {
				// continue searching
			}
		}
		if(method == null) {
			throw new PreferantoException("No Math method matches " + funcName + "(" + type.getName() + ")");
		}

		org.objectweb.asm.Type[] argTypes = org.objectweb.asm.Type.getArgumentTypes(method);		
		boolean l2dNeeded = ((type == BuiltInTypeSymbol._INTEGER) && (argTypes[0].getSort() == org.objectweb.asm.Type.DOUBLE));
		if(l2dNeeded) {
			mv.visitInsn(L2D);
		}

		String descriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", funcName, descriptor);	
	}
	
	public void pushMathTwo(String funcName, Type type1, Type type2) {
		log.trace("Pushing mathTwo " + funcName + "(" + type1.getName() + ", " + type2.getName() + ")");
		Class<?>[] prmTypes1 = getPossibleParameterTypes(type1);
		Class<?>[] prmTypes2 = getPossibleParameterTypes(type2);
		Method method = null;
		for(Class<?> prmType1 : prmTypes1) {
			for(Class<?> prmType2 : prmTypes2) {
				try {
					method = Math.class.getMethod(funcName, prmType1, prmType2);
					break;
				} catch(Exception e) {
					// continue searching
				}
			}			
		}
		if(method == null) {
			throw new PreferantoException("No Math method matches " + funcName + "(" + type1.getName() + ", " + type2.getName() + ")");
		}

		org.objectweb.asm.Type[] argTypes = org.objectweb.asm.Type.getArgumentTypes(method);		
		performNecessaryConversions(type1, argTypes[0], type2, argTypes[1]);

		String descriptor = org.objectweb.asm.Type.getMethodDescriptor(method);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", funcName, descriptor);	
	}

	private void performNecessaryConversions(Type type1, org.objectweb.asm.Type requiredType1, Type type2, org.objectweb.asm.Type requiredType2) {
		boolean l2dNeeded1 = ((type1 == BuiltInTypeSymbol._INTEGER) && (requiredType1.getSort() == org.objectweb.asm.Type.DOUBLE));
		boolean l2dNeeded2 = ((type2 == BuiltInTypeSymbol._INTEGER) && (requiredType2.getSort() == org.objectweb.asm.Type.DOUBLE));

		if(l2dNeeded1) {
			int arg2VarIndex = storeIntoVar(type2);
			mv.visitInsn(L2D);			
			loadFromVar(arg2VarIndex, type2);
		}
		if(l2dNeeded2) {
			mv.visitInsn(L2D);
		}
	}

	private int storeIntoVar(Type type) {
		int varIndex = currVarIndex;
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			mv.visitVarInsn(ISTORE, varIndex);
			currVarIndex ++;
		} else if(type == BuiltInTypeSymbol._REAL) {
			mv.visitVarInsn(DSTORE, varIndex);
			currVarIndex += 2;
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			mv.visitVarInsn(LSTORE, varIndex);
			currVarIndex += 2;
		} else {
			mv.visitVarInsn(ASTORE, varIndex);
			currVarIndex ++;
		}
		return varIndex;
	}
	
	private void loadFromVar(int varIndex, Type type) {
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			mv.visitVarInsn(ILOAD, varIndex);
		} else if(type == BuiltInTypeSymbol._REAL) {
			mv.visitVarInsn(DLOAD, varIndex);
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			mv.visitVarInsn(LLOAD, varIndex);
		} else {
			mv.visitVarInsn(ALOAD, varIndex);
		}			
	}
	
	private Type performTypePromotion(Type type1, Type type2) {
		if(type1 == type2) return type1;
		if((type1 != BuiltInTypeSymbol._REAL) && (type2 != BuiltInTypeSymbol._REAL)) return type1;
		performNecessaryConversions(type1, org.objectweb.asm.Type.DOUBLE_TYPE, type2, org.objectweb.asm.Type.DOUBLE_TYPE);
		return BuiltInTypeSymbol._REAL;
	}

	
    public void negate() {
        Label lbTrue = new Label();
        mv.visitJumpInsn(IFEQ, lbTrue);
        mv.visitInsn(ICONST_0);
        Label lbDone = new Label();
        mv.visitJumpInsn(GOTO, lbDone);
        mv.visitLabel(lbTrue);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(lbDone);
    }
	
    public void unaryAdd(String op, Type type) {
    	if("-".equals(op)) {
    		if(type == BuiltInTypeSymbol._REAL) {
    			mv.visitInsn(DNEG);
    		} else {
    			mv.visitInsn(LNEG);
    		}
    	}
    }
	
    public void arithmOp(String op, Type type1, Type type2) {    	
    	Type type = performTypePromotion(type1, type2);
    	if("+".equals(op)) {
    		if(type == BuiltInTypeSymbol._REAL) {
    			mv.visitInsn(DADD);
    		} else {
    			mv.visitInsn(LADD);
    		}
    	} else if("-".equals(op)) {
    		if(type == BuiltInTypeSymbol._REAL) {
    			mv.visitInsn(DSUB);
    		} else {
    			mv.visitInsn(LSUB);
    		}
    	} else if("*".equals(op)) {
    		if(type == BuiltInTypeSymbol._REAL) {
    			mv.visitInsn(DMUL);
    		} else {
    			mv.visitInsn(LMUL);
    		}
    	} else if("/".equals(op)) {
    		if(type == BuiltInTypeSymbol._REAL) {
    			mv.visitInsn(DDIV);
    		} else {
    			mv.visitInsn(LDIV);
    		}
    	} else if("%".equals(op)) {
    		if(type == BuiltInTypeSymbol._REAL) {
    			mv.visitInsn(DREM);
    		} else {
    			mv.visitInsn(LREM);
    		}
    	}
    }
  
    public void logicalOp(String op, Type type1, Type type2) {    	
    	Type type = performTypePromotion(type1, type2);
    	if("|".equals(op)) {
    		if(type == BuiltInTypeSymbol._INTEGER) {
    			mv.visitInsn(LOR);
    		} else {
    			mv.visitInsn(IOR);
    		}
    	} else if("&".equals(op)) {
    		if(type == BuiltInTypeSymbol._INTEGER) {
    			mv.visitInsn(LAND);
    		} else {
    			mv.visitInsn(IAND);
    		}
    	} else {
    		int ifOpcode = -1;
    		if(type == BuiltInTypeSymbol._BOOLEAN) {
        		ifOpcode = "=".equals(op) ? IF_ICMPNE : IF_ICMPEQ;
    		} else {
    			if((type == BuiltInTypeSymbol._BOOLEAN) || (type == BuiltInTypeSymbol._INTEGER) || (type == BuiltInTypeSymbol._REAL)) {
        			int cmpOpcode = (type == BuiltInTypeSymbol._INTEGER) ? LCMP : DCMPG;
    				mv.visitInsn(cmpOpcode);
    			} else {
    				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I");
    			}
        		if("=".equals(op)) {
        			ifOpcode = IFNE;
        		} else if("!=".equals(op) || "<>".equals(op)) {
        			ifOpcode = IFEQ;
        		} else if("<=".equals(op)) {
        			ifOpcode = IFGT;
        		} else if("<".equals(op)) {
        			ifOpcode = IFGE;
        		} else if(">=".equals(op)) {
        			ifOpcode = IFLT;
        		} else if(">".equals(op)) {
        			ifOpcode = IFLE;
        		}
    		}
			Label lbFalse = new Label();
			mv.visitJumpInsn(ifOpcode, lbFalse);
			mv.visitInsn(ICONST_1);
			Label lbDone = new Label();
			mv.visitJumpInsn(GOTO, lbDone);
			mv.visitLabel(lbFalse);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(lbDone);
    	}
    }

    public void prefOp(String prefOpName, Type type) {
    	PrefOp prefOp = PrefOp.fromName(prefOpName);
    	if(prefOp != null) {
    		switch(prefOp) {
				case DIFF: 
					if(type == BuiltInTypeSymbol._INTEGER) {
						mv.visitInsn(LSUB);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(J)J");					
					} else {
						mv.visitInsn(DSUB);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "abs", "(D)D");					
					}
					break;
				case ALL: mv.visitInsn(IAND); break;
				case EXACTLY_ONE: mv.visitInsn(IXOR); break;
				default: mv.visitInsn(IOR); break;
			}
    	}
    }    

	private static Class<?>[] getPossibleParameterTypes(Type type) {
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			return new Class<?>[] {boolean.class, Boolean.class};
		} else if(type == BuiltInTypeSymbol._REAL) {
			return new Class<?>[] {double.class, Double.class};
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			return new Class<?>[] {long.class, Long.class, double.class, Double.class};
		} else {
			return new Class<?>[] {String.class};
		}
	}
	
	public byte[] getEvalByteCode() {
		return cw.toByteArray();
	}
	
	public byte[] getObjectivesByteCode() {
		return cwObjectives.toByteArray();
	}
}
