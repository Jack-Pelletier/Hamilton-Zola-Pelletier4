package ast.nodes;

import ast.EvaluationException;
import ast.nodes.LambdaNode.Closure;
import ast.nodes.LambdaNode.FunType;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;
import environment.Environment;
import environment.TypeEnvironment;
import lexer.Token;

/**
 * Represents a function application (E1)(E2)
 */
public final class ApplyNode extends SyntaxNode {
    private final SyntaxNode function;
    private final SyntaxNode argument;

    public ApplyNode(SyntaxNode function, SyntaxNode argument, long line) {
        super(line);
        this.function = function;
        this.argument = argument;
    }

    /**
     * Evaluate the application following the semantics:
     *
     * [[ (E1)(E2) ]]e =
     * if [[E1]]e is a closure C:
     * evaluate argument, bind to C.var in copied env, eval C.body
     * else ⊥ (EvaluationException)
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException {
        Object f = function.evaluate(env);

        if (!(f instanceof Closure))
            throw new EvaluationException();

        Closure closure = (Closure) f;

        // Evaluate argument
        Object argVal = argument.evaluate(env);

        // Create new environment from closure environment
        Environment newEnv = closure.getEnvironment().copy();
        newEnv.updateEnvironment(closure.getParameter(), argVal);

        // Evaluate body in closure's environment
        return closure.getBody().evaluate(newEnv);
    }

    /**
     * typeOf:
     *
     * 1. funType = typeOf(function)
     * 2. argType = typeOf(argument)
     * 3. resultType = fresh type variable α
     * 4. unify funType with (argType → resultType)
     * 5. return Substitutions.apply(resultType)
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException {
        Type funType = function.typeOf(tenv, inferencer);
        Type argType = argument.typeOf(tenv, inferencer);

        // fresh α
        VarType resultType = tenv.getTypeVariable();

        // unify funType with argType -> α
        FunType expected = new FunType(argType, resultType);
        inferencer.unify(funType, expected, null);

        // Return the unified result type
        return inferencer.getSubstitutions().apply(resultType);
    }

    /**
     * Display subtree for debugging / AST visualization.
     */
    @Override
    public void displaySubtree(int indentAmt) {
        printIndented("ApplyNode(", indentAmt);

        printIndented("function:", indentAmt + 2);
        function.displaySubtree(indentAmt + 4);

        printIndented("argument:", indentAmt + 2);
        argument.displaySubtree(indentAmt + 4);

        printIndented(")", indentAmt);
    }
}
