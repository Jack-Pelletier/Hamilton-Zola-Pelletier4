/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ast;

import java.util.LinkedList;

import ast.nodes.LambdaNode.Closure;
import ast.nodes.SyntaxNode;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * This is the FoldNode for Phase 3.
 * This is: foldl / foldr f init xs
 *
 * If rightFold is true, we do a right fold (foldr).
 * If rightFold is false, we do a left fold (foldl).
 */
public class FoldNode extends SyntaxNode
{
    private final SyntaxNode func;
    private final SyntaxNode init;
    private final SyntaxNode listExpr;
    private final boolean    rightFold;

    public FoldNode(SyntaxNode func, SyntaxNode init, SyntaxNode listExpr,
                    boolean rightFold, long lineNumber)
    {
        super(lineNumber);
        this.func      = func;
        this.init      = init;
        this.listExpr  = listExpr;
        this.rightFold = rightFold;
    }

    /**
     * This is the runtime semantics:
     *
     * For a left fold (foldl):
     *   foldl f acc [x1, x2, ..., xn]
     *   ==> f (... (f (f acc x1) x2) ...) xn
     *
     * For a right fold (foldr):
     *   foldr f acc [x1, x2, ..., xn]
     *   ==> f x1 (f x2 (... (f xn acc)...))
     *
     * The function f is curried and takes two arguments.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        // This is where we evaluate the function expression
        Object fVal = func.evaluate(env);
        if (!(fVal instanceof Closure))
        {
            logError("fold: first argument must be a function.");
            throw new EvaluationException();
        }
        Closure fClosure = (Closure) fVal;

        // This is where we evaluate the initial accumulator
        Object acc = init.evaluate(env);

        // This is where we evaluate the list expression
        Object listVal = listExpr.evaluate(env);
        if (!(listVal instanceof LinkedList<?>))
        {
            logError("fold: third argument must be a list.");
            throw new EvaluationException();
        }

        LinkedList<?> xs = (LinkedList<?>) listVal;

        if (rightFold)
        {
            // This is the right fold case (foldr)
            for (int i = xs.size() - 1; i >= 0; i--)
            {
                Object elem = xs.get(i);
                acc = applyTwoArgs(fClosure, elem, acc);
            }
        }
        else
        {
            // This is the left fold case (foldl)
            for (Object elem : xs)
            {
                acc = applyTwoArgs(fClosure, acc, elem);
            }
        }

        return acc;
    }

    /**
     * This is a helper that applies a curried function to two arguments:
     *
     *   ((f arg1) arg2)
     */
    private Object applyTwoArgs(Closure fClosure, Object arg1, Object arg2)
            throws EvaluationException
    {
        // This is: first application f arg1
        Object first = applyClosure(fClosure, arg1);

        if (!(first instanceof Closure))
        {
            logError("fold: function must take two arguments (curried).");
            throw new EvaluationException();
        }

        Closure secondClosure = (Closure) first;

        // This is: second application (f arg1) arg2
        return applyClosure(secondClosure, arg2);
    }

    /**
     * This is a helper that applies a single closure to one argument.
     */
    private Object applyClosure(Closure clo, Object arg)
            throws EvaluationException
    {
        Environment newEnv = clo.getEnvironment().copy();
        newEnv.updateEnvironment(clo.getParameter(), arg);
        return clo.getBody().evaluate(newEnv);
    }

    /**
     * This is the typing rule (informally):
     *
     *   f   : a -> b -> b     (curried function)
     *   init: b
     *   xs  : list[a]
     *   ----------------
     *   fold f init xs : b
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // This is where we get the types of the subexpressions
        Type fType   = func.typeOf(tenv, inferencer);
        Type initTy  = init.typeOf(tenv, inferencer);
        Type xsType  = listExpr.typeOf(tenv, inferencer);

        // This is where we create fresh type variables a, b
        VarType a = tenv.getTypeVariable();
        VarType b = tenv.getTypeVariable();

        // This is enforcing xs : list[a]
        ListType expectedList = new ListType(a);
        inferencer.unify(xsType, expectedList,
                buildErrorMessage("fold: third argument must be a list."));

        // This is enforcing init : b
        inferencer.unify(initTy, b,
                buildErrorMessage("fold: initial value has wrong type."));

        // This is enforcing f : a -> b -> b  (curried)
        FunType expectedFun = new FunType(a, new FunType(b, b));
        inferencer.unify(fType, expectedFun,
                buildErrorMessage("fold: function must have type a -> b -> b."));

        // This is where we return the result type b with substitutions applied
        Type finalB = inferencer.getSubstitutions().apply(b);
        return finalB;
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("FoldNode(" + (rightFold ? "foldr" : "foldl") + ",", indentAmt);
        func.displaySubtree(indentAmt + 2);
        init.displaySubtree(indentAmt + 2);
        listExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
