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
package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.FunType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * This node represents a lambda (fn) expression.
 */
public final class LambdaNode extends SyntaxNode
{
    private Token variable;        // parameter
    private SyntaxNode body;       // function body

    /**
     * Constructs a new lambda node.
     *
     * @param variable the parameter of the lambda.
     * @param body     the body of the lambda.
     * @param line     the line of code the node is associated with.
     */
    public LambdaNode(Token variable, SyntaxNode body, long line)
    {
        super(line);
        this.variable = variable;
        this.body = body;
    }

    /**
     * Evaluate the node: produce a closure that remembers the current env.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Environment capturedEnv = env.copy();
        return new Closure(variable, body, capturedEnv);
    }

    /**
     * Type of a lambda: (paramType -> bodyType).
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // Fresh type var for the parameter
        VarType paramType = tenv.getTypeVariable();

        // Extended environment with parameter binding
        TypeEnvironment newTenv = tenv.copy();
        newTenv.updateEnvironment(variable, paramType);

        // Type of body under extended env
        Type bodyType = body.typeOf(newTenv, inferencer);

        // Apply any accumulated substitutions
        Type finalParamType = inferencer.getSubstitutions().apply(paramType);
        Type finalBodyType  = inferencer.getSubstitutions().apply(bodyType);

        // Return function type paramType -> bodyType
        return new FunType(finalParamType, finalBodyType);
    }

    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("Lambda(", indentAmt);
        printIndented("param: " + variable.getValue(), indentAmt + 2);
        printIndented("body:", indentAmt + 2);
        body.displaySubtree(indentAmt + 4);
        printIndented(")", indentAmt);
    }

    /**
     * Runtime closure value for a lambda.
     */
    public static final class Closure
    {
        private final Token parameter;
        private final SyntaxNode body;
        private final Environment env;

        public Closure(Token parameter, SyntaxNode body, Environment env)
        {
            this.parameter = parameter;
            this.body = body;
            this.env = env;
        }

        public Token getParameter()
        {
            return parameter;
        }

        public SyntaxNode getBody()
        {
            return body;
        }

        public Environment getEnvironment()
        {
            return env;
        }

        @Override
        public String toString()
        {
            return "<closure " + parameter.getValue() + " -> ... >";
        }
    }
}
