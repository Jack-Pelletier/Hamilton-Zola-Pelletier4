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
 * Represents foldl / foldr.
 *   foldl(f, init, [x1,...,xn])
 *   foldr(f, init, [x1,...,xn])
 */
public class FoldNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode initExpr;
    private SyntaxNode listExpr;
    private boolean isLeft;   // true = foldl, false = foldr

    public FoldNode(SyntaxNode func, SyntaxNode initExpr,
                    SyntaxNode listExpr, boolean isLeft, long lineNumber)
    {
        super(lineNumber);
        this.func = func;
        this.initExpr = initExpr;
        this.listExpr = listExpr;
        this.isLeft = isLeft;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object fVal = func.evaluate(env);
        if (!(fVal instanceof Closure))
        {
            logError("fold: first argument must be a function.");
            throw new EvaluationException();
        }
        Closure f = (Closure) fVal;

        Object acc = initExpr.evaluate(env);

        Object listVal = listExpr.evaluate(env);
        if (!(listVal instanceof LinkedList<?>))
        {
            logError("fold: third argument must be a list.");
            throw new EvaluationException();
        }
        LinkedList<?> input = (LinkedList<?>) listVal;

        if (isLeft)
        {
            // foldl: (((init f x1) f x2) ... f xn)
            for (Object elem : input)
            {
                Object tmp = applyClosure(f, acc);
                if (!(tmp instanceof Closure))
                {
                    logError("foldl: function must take two arguments (curried).");
                    throw new EvaluationException();
                }
                acc = applyClosure((Closure) tmp, elem);
            }
        }
        else
        {
            // foldr: (x1 f (x2 f (... (xn f init)...)))
            for (int i = input.size() - 1; i >= 0; i--)
            {
                Object elem = input.get(i);
                Object tmp = applyClosure(f, elem);
                if (!(tmp instanceof Closure))
                {
                    logError("foldr: function must take two arguments (curried).");
                    throw new EvaluationException();
                }
                acc = applyClosure((Closure) tmp, acc);
            }
        }

        return acc;
    }

    /**
     * Helper to apply a closure to one argument.
     */
    private Object applyClosure(Closure clo, Object arg) throws EvaluationException
    {
        Environment newEnv = clo.getEnvironment().copy();
        newEnv.updateEnvironment(clo.getParameter(), arg);
        return clo.getBody().evaluate(newEnv);
    }

    /**
     * Typing rules (informal):
     *
     * foldl:
     *   f   : a -> b -> a
     *   init: a
     *   xs  : list[b]
     *   ----------------
     *   foldl f init xs : a
     *
     * foldr:
     *   f   : a -> b -> b
     *   init: b
     *   xs  : list[a]
     *   ----------------
     *   foldr f init xs : b
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        Type fType   = func.typeOf(tenv, inferencer);
        Type initType = initExpr.typeOf(tenv, inferencer);
        Type xsType   = listExpr.typeOf(tenv, inferencer);

        if (isLeft)
        {
            // foldl: (a -> b -> a), init : a, xs : list[b]
            VarType a = tenv.getTypeVariable();
            VarType b = tenv.getTypeVariable();

            inferencer.unify(initType, a,
                    buildErrorMessage("foldl: init value has wrong type."));

            inferencer.unify(xsType, new ListType(b),
                    buildErrorMessage("foldl: third argument must be a list."));

            FunType expectedFun = new FunType(a, new FunType(b, a));
            inferencer.unify(fType, expectedFun,
                    buildErrorMessage("foldl: function must have type a -> b -> a."));

            return inferencer.getSubstitutions().apply(a);
        }
        else
        {
            // foldr: (a -> b -> b), init : b, xs : list[a]
            VarType a = tenv.getTypeVariable();
            VarType b = tenv.getTypeVariable();

            inferencer.unify(xsType, new ListType(a),
                    buildErrorMessage("foldr: third argument must be a list."));

            inferencer.unify(initType, b,
                    buildErrorMessage("foldr: init value has wrong type."));

            FunType expectedFun = new FunType(a, new FunType(b, b));
            inferencer.unify(fType, expectedFun,
                    buildErrorMessage("foldr: function must have type a -> b -> b."));

            return inferencer.getSubstitutions().apply(b);
        }
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented(isLeft ? "FoldNode(foldl" : "FoldNode(foldr", indentAmt);
        func.displaySubtree(indentAmt + 2);
        initExpr.displaySubtree(indentAmt + 2);
        listExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
