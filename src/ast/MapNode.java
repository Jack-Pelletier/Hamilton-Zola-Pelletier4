/*
 *   Copyright (C) 2022  2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it andor modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <httpswww.gnu.orglicenses>.
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
 * this is MapNode for Phase 3.
 * this is the node that represents map f xs
 */
public class MapNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode listExpr;

    /**
     * this is where we build a new MapNode with a function and a list expr
     */
    public MapNode(SyntaxNode func, SyntaxNode listExpr, long lineNumber)
    {
        super(lineNumber);
        this.func = func;
        this.listExpr = listExpr;
    }

    /**
     * this is the runtime semantics
     * this is map f x1 x2  xn becomes a list of f x1 f x2  fn
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        // this is where we evaluate the function expression
        Object fVal = func.evaluate(env);
        if (!(fVal instanceof Closure))
        {
            logError("map first argument must be a function");
            throw new EvaluationException();
        }

        Closure clo = (Closure) fVal;

        // this is where we evaluate the list expression
        Object listVal = listExpr.evaluate(env);
        if (!(listVal instanceof LinkedList))
        {
            logError("map second argument must be a list");
            throw new EvaluationException();
        }

        LinkedList inputList = (LinkedList) listVal;
        LinkedList<Object> result = new LinkedList<>();

        // this is where for each element x in the list we apply f x
        for (Object elem   inputList)
        {
            Environment newEnv = clo.getEnvironment().copy();
            newEnv.updateEnvironment(clo.getParameter(), elem);
            Object mapped = clo.getBody().evaluate(newEnv);
            result.add(mapped);
        }

        return result;
    }

    /**
     * this is the typing rule in words
     *
     * this is if f has type a to b and xs has type list of a
     * this is then map f xs has type list of b
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // this is where we get the types of the subexpressions
        Type fType  = func.typeOf(tenv, inferencer);
        Type xsType = listExpr.typeOf(tenv, inferencer);

        // this is where we create fresh type variables a and b
        VarType a = tenv.getTypeVariable();
        VarType b = tenv.getTypeVariable();

        // this is enforcing xs has type list of a
        ListType expectedList = new ListType(a);
        inferencer.unify(xsType, expectedList,
                buildErrorMessage("map second argument must be a list"));

        // this is enforcing f has type a to b using the shared FunType
        FunType expectedFun = new FunType(a, b);
        inferencer.unify(fType, expectedFun,
                buildErrorMessage("map first argument must be a function from the element type to a result type"));

        // this is where we return the result type list of b with substitutions applied
        Type finalB = inferencer.getSubstitutions().apply(b);
        return new ListType(finalB);
    }

    /**
     * this is where we print the subtree for debugging
     */
    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("MapNode(", indentAmt);
        func.displaySubtree(indentAmt + 2);
        listExpr.displaySubtree(indentAmt + 2);
        printIndented(")", indentAmt);
    }
}
