/**
 * Copyright (C) 2015  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mujava.op.basic;

import mujava.op.util.ExpressionAnalyzer;
import mujava.op.util.LogReduction;
import openjava.mop.FileEnvironment;
import openjava.ptree.*;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>
 * Generate AOIU (Arithmetic Operator Insertion (Unary)) mutants -- insert a
 * unary operator (arithmetic -) before each variable or expression
 * </p>
 *
 * @author Yu-Seung Ma
 * @version 1.0
 * <p>
 * Took out aor_flag for not clear about the reason of using it.
 * Lin Deng, Aug 23
 * <p>
 * Added code to generate mutants for logical expressions.
 * E.g., a < b  =>  -a < b
 * Lin Deng, Aug 28
 */

public class AOIU extends Arithmetic_OP {
  // boolean aor_flag = false;

  private java.util.List<String> allOperatorsSelected;

  public AOIU(FileEnvironment file_env, ClassDeclaration cdecl, CompilationUnit comp_unit) {
	super(file_env, comp_unit);
	allOperatorsSelected = new java.util.ArrayList<>();
  }

  public AOIU(FileEnvironment file_env, ClassDeclaration cdecl
	  , CompilationUnit comp_unit, java.util.List<String> allOperatorsSelected) {
	this(file_env, cdecl, comp_unit);
	this.allOperatorsSelected = allOperatorsSelected;
  }

  /**
   * Set an AOR flag
   *
   * @param p
   */
  // public void setAORflag(boolean b)
  // {
  // aor_flag = b;
  // }
  public void visit(UnaryExpression p) throws ParseTreeException {
	// NO OPERATION
  }

  /**
   * Generate AOIU mutant
   */
  public void visit(Variable p) throws ParseTreeException {
	if (isArithmeticType(p) && !isDuplicated(p)) {
	  outputToFile(p);
	}
  }

  /**
   * Generate AOIU mutant
   */
  public void visit(FieldAccess p) throws ParseTreeException {
	if (isArithmeticType(p)) {
	  outputToFile(p);
	}
  }

  /**
   * Generate AOIU mutant
   */
  public void visit(BinaryExpression p) throws ParseTreeException {
	// if (aor_flag && isArithmeticType(p))
	// not clear about the reason for using the flag.
	// take it out.

	// NOT SURE WHY IT IS SET TO ONLY ACCEPT ARITHMETIC TYPE,
	// HOW ABOUT a < b ?
	// Lin takes it out on 08/28
	// if (isArithmeticType(p))
	// {
	if ((p.getOperator() == BinaryExpression.MINUS) || (p.getOperator() == BinaryExpression.PLUS)
		|| (p.getOperator() == BinaryExpression.MOD)) {
	  Expression e1 = p.getLeft();
	  super.visit(e1);
	  // Ignore right expression because it produce equivalent mutants;
	  // Expression e2 = p.getRight();
	  //
	  // WHY??? (LIN 08/28)
	} else if ((p.getOperator() == BinaryExpression.DIVIDE) || (p.getOperator() == BinaryExpression.TIMES)) {
	  Expression e1 = p.getLeft();
	  Expression e2 = p.getRight();
	  if (((e1 instanceof Variable) || (e1 instanceof FieldAccess))
		  && ((e2 instanceof Variable) || (e2 instanceof FieldAccess))) {
		// Consider only one expression because it produces equivalent
		// mutants;
		//
		// WHY??? (LIN 08/28)
		super.visit(e1);
	  } else {
		super.visit(p);
	  }
	}
	// 08/28
	// Lin added to generate mutants for logical expressions
	// e.g.
	// a < b  => -a < b
	else if (((p.getOperator() == BinaryExpression.GREATER) || (p.getOperator() == BinaryExpression.GREATEREQUAL)
		|| (p.getOperator() == BinaryExpression.LESSEQUAL) || (p.getOperator() == BinaryExpression.EQUAL)
		|| (p.getOperator() == BinaryExpression.NOTEQUAL) || (p.getOperator() == BinaryExpression.LESS))
		&& !isEquivalent(p)) {
	  Expression e1 = p.getLeft();
	  Expression e2 = p.getRight();
	  super.visit(e1);
	  super.visit(e2);
	}
  }

  /**
   * Generate AOIU mutant
   */
  public void visit(AssignmentExpression p) throws ParseTreeException {
	// [ Example ]
	// int a=0;int b=2;int c=4;
	// Right Expression : a = b = -c;
	// Wrong Expression : a = -b = c;
	// Ignore left expression
	if (isEquivalent(p)) return;
	Expression rexp = p.getRight();
	rexp.accept(this);
  }

  /***
   * Write AOIU mutants to files
   *
   * @param original_field
   */
  public void outputToFile(FieldAccess original_field) {
	if (comp_unit == null)
	  return;

	String f_name;
	num++;
	f_name = getSourceName("AOIU");
	String mutant_dir = getMuantID("AOIU");

	try {
	  PrintWriter out = getPrintWriter(f_name);
	  AOIU_Writer writer = new AOIU_Writer(mutant_dir, out);
	  writer.setMutant(original_field);
	  writer.setMethodSignature(currentMethodSignature);
	  comp_unit.accept(writer);
	  out.flush();
	  out.close();
	} catch (IOException e) {
	  System.err.println("fails to create " + f_name);
	} catch (ParseTreeException e) {
	  System.err.println("errors during printing " + f_name);
	  e.printStackTrace();
	}
  }

  /**
   * Write AOIU mutants to files
   *
   * @param original_var
   */
  public void outputToFile(Variable original_var) {
	if (comp_unit == null)
	  return;

	String f_name;
	num++;
	f_name = getSourceName("AOIU");
	String mutant_dir = getMuantID("AOIU");

	try {
	  PrintWriter out = getPrintWriter(f_name);
	  AOIU_Writer writer = new AOIU_Writer(mutant_dir, out);
	  writer.setMutant(original_var);
	  writer.setMethodSignature(currentMethodSignature);
	  comp_unit.accept(writer);
	  out.flush();
	  out.close();
	} catch (IOException e) {
	  System.err.println("fails to create " + f_name);
	} catch (ParseTreeException e) {
	  System.err.println("errors during printing " + f_name);
	  e.printStackTrace();
	}
  }

  /**
   * Avoid equivalent mutants given following criteria:
   * ERule AOIU12
   * "term = while (exp) { int v1; ... v2 = v1; }
   * transformations = {
   *   AOIS(v1) = v1 op
   * }
   * constraints = {
   *   v1 has a local scope (inside the loop body),
   *   the use of v1 is the last one in the RHS of the loop,
   *   op ∈ {++, --}
   * }"
   * @param exp
   * @return true when matches criteria. False otherwise.
   * @author Pedro Pinheiro
   */
  boolean isEquivalent(AssignmentExpression exp) {
	boolean aoiu12 = false;
	if (exp.getOperator() == AssignmentExpression.MOD) {
	  aoiu12 = LogReduction.AVOID;
	  logReduction("AOIU", "Triggered ERule 12:" +exp + " => " +"++ or --");
	  System.out.println("Triggered ERule 12:" + exp.toFlattenString());
	}
	return aoiu12;
  }

    /**
   * Avoid equivalent mutants given following criteria:
   * ERule AOIU15
   *"term = while (exp) { int v1; ... v2 = v1; }
   * transformations = {
   *   AOIS(v1) = v1 op
   * }
   * constraints = {
   *   v1 has a local scope (inside the loop body),
   *   the use of v1 is the last one in the RHS of the loop,
   *   op ∈ {++, --}
   * }"
   * @param exp
   * @return true when matches criteria. False otherwise.
   * @author Pedro Pinheiro
   */
  boolean isEquivalent(BinaryExpression exp) {
	boolean aoiu15 = false;
	ExpressionAnalyzer aexp = new ExpressionAnalyzer(exp, this.getEnvironment());
	if (aexp.containsZeroLiteral() && aexp.isInsideIf()) {
	  aoiu15 = LogReduction.AVOID;
	  switch (aexp.getRootOperator()) {
		case EQUALS:
		case DIFFERENT:
		  logReduction("AOIU", "Triggered Erule 15: " + exp + " => " + "== or !=");
		  System.out.println("Triggered Erule 15: " + exp + " => " + "== or !=");
		  break;
		default:
		  aoiu15 = false;
		  break;
	  }
	}
	return aoiu15;
  }

  /**
   * DRrule AOIU_AOIU57
   * Avoid duplicated mutants that matches the following conditions:
   * term = type v := exp; ... return v;
   * transformations = {
   * AOIU(exp) = -exp,
   * AOIU(v) = -v
   * }
   * constraints = {
   * There is no definition of v between definition and the use in a return statement,
   * v can be any primitive numeric type
   * }
   *
   * @author Pedro Pinheiro
   */
  private boolean isDuplicated(Variable variable) {
	boolean isArithmeticType = false, d_aoiu_aoiu57 = false;
	try {
	  isArithmeticType = isArithmeticType(variable);
	} catch (ParseTreeException ignored) {
	}
	if (!isArithmeticType) return false;


	ParseTreeObject pto = variable;
	for (int limit = 3; pto != null && (limit >= 0) && !(pto instanceof ReturnStatement); limit--, pto = pto.getParent()) {
	}
	if (pto instanceof ReturnStatement) {
	  ReturnStatement rts = (ReturnStatement) pto;

	  ParseTreeObject pto2 = rts;
	  for (; pto2 != null && !(pto2 instanceof MethodDeclaration); pto2 = pto2.getParent()) {
	  }
	  if (pto2 != null) {
		MethodDeclaration md = (MethodDeclaration) pto2;
		StatementList sl = md.getBody();
		System.out.println(42);
		boolean variableWasDeclared = false;
		boolean variableIsNotModifiedUntilReturn = true;
		for (int i = 0; i < sl.size(); i++) {
		  Statement st = sl.get(i);
		  if (st.getClass().isAssignableFrom(ExpressionStatement.class)) {
			Expression exp = ((ExpressionStatement) st).getExpression();
			if (exp instanceof VariableDeclaration) {
			  if (((VariableDeclaration) exp).getVariableDeclarator().getVariable().equals(variable.toString()))
				variableWasDeclared = true;
			} else if (exp instanceof AssignmentExpression) {
			  if (variableWasDeclared && ((AssignmentExpression) exp).getLeft().equals(variable))
				variableIsNotModifiedUntilReturn = false;
			} else if (exp instanceof UnaryExpression) {
			  if (variableWasDeclared && ((UnaryExpression) exp).getExpression().equals(variable))
				variableIsNotModifiedUntilReturn = false;
			} else if (exp instanceof MethodCall) {
			  //Going conservative
//			ExpressionList args = ((MethodCall) st).getArguments();
//			for(int k = 0; k < args.size(); k++) {
//			  Expression exp = args.get(k);
//			  ParseTree pt = exp;
			  variableIsNotModifiedUntilReturn = false;
			  break;
			}
		  } else {
		  if (st instanceof VariableDeclaration) {
			if (((VariableDeclaration) st).getVariableDeclarator().getVariable().equals(variable.toString()))
			  variableWasDeclared = true;
		  } else if (st instanceof AssignmentExpression) {
			if (variableWasDeclared && ((AssignmentExpression) st).getLeft().equals(variable))
			  variableIsNotModifiedUntilReturn = false;
		  } else if (st instanceof UnaryExpression) {
			if (variableWasDeclared && ((UnaryExpression) st).getExpression().equals(variable))
			  variableIsNotModifiedUntilReturn = false;
		  } else if (st instanceof MethodCall) {
			//Going conservative
//			ExpressionList args = ((MethodCall) st).getArguments();
//			for(int k = 0; k < args.size(); k++) {
//			  Expression exp = args.get(k);
//			  ParseTree pt = exp;
			variableIsNotModifiedUntilReturn = false;
			break;
		  }
		}
	  }
	  if (variableWasDeclared && variableIsNotModifiedUntilReturn) {
		d_aoiu_aoiu57 = LogReduction.AVOID;
		logReduction("AOIU", "AOIU", "AOIU_AOIU57 => " + variable.toFlattenString());
		System.out.println("AOIU_AOIU57 => " + variable.toFlattenString());
	  }
	}
  }
	return d_aoiu_aoiu57;
}

  /**
   * Avoid duplicated mutants given following criteria:
   * "term = v1 += v2
   * transformations = {
   *   AOIU(v2) = -v2 ,
   *   ASRS(+=) = -=;
   * }
   * constraints = {
   *
   * }"
   * @param assignmentExpression
   * @author Pedro Pinheiro
   * @return
   */
  private boolean isDuplicated(AssignmentExpression assignmentExpression) {
	boolean d_aoiu_43 = false;

	if (assignmentExpression.getOperator() == AssignmentExpression.ADD) {
	  if (allOperatorsSelected.contains("ASRS")) {
		String desc = assignmentExpression.toFlattenString();
		logReduction("AOIU", "ASRS", desc);
		d_aoiu_43 = LogReduction.AVOID;
	  }
	}
	return d_aoiu_43;
  }
}
