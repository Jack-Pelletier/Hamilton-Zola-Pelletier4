package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * This node represents an if-then-else expression.
 */
public final class IfNode extends SyntaxNode
{
    private SyntaxNode condition;   // The condition expression.
    private SyntaxNode trueBranch; // The true branch.
    private SyntaxNode falseBranch; // The false branch.

    /**
     * Constructs a new if-then-else node.
     * 
     * @param cond    the condition expression.
     * @param tBranch the expression evaluated when the condition is true.
     * @param fBranch the expression evaluated when the condition is false.
     * @param line    the line of code the node is associated with.
     */
    public IfNode(SyntaxNode cond, SyntaxNode tBranch, SyntaxNode fBranch, long line)
    {
        super(line);
        this.condition = cond;
        this.trueBranch = tBranch;
        this.falseBranch = fBranch;
    }

    /**
     * Evaluate the node.
     * 
     * @param env the executional environment we should evaluate the node under.
     * @return the object representing the result of the evaluation.
     * @throws EvaluationException if the evaluation fails.
     */
    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        Object condVal = condition.evaluate(env);

        // Make sure the condition evaluates to a Boolean.
        if (!(condVal instanceof Boolean))
        {
            logError("Boolean condition expected in if expression.");
            throw new EvaluationException();
        }

        if ((Boolean) condVal)
            return trueBranch.evaluate(env);
        else
            return falseBranch.evaluate(env);
    }

    /**
     * Determine the type of the syntax node. In particular bool, int, real,
     * generic, or function.
     * 
     * The type of an if-then-else expression is the (unified) type of the
     * true and false branches, provided the condition is of type bool.
     * 
     * @param tenv       the type environment.
     * @param inferencer the type inferencer.
     * @return The type of the syntax node.
     * @throws TypeException if there is a type error.
     */
    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer)
            throws TypeException
    {
        // Type-check the condition and ensure it is BoolType.
        Type condType = condition.typeOf(tenv, inferencer);
        inferencer.unify(condType, new BoolType(),
                buildErrorMessage("Boolean type expected in if condition."));

        // Type-check branches.
        Type tType = trueBranch.typeOf(tenv, inferencer);
        Type fType = falseBranch.typeOf(tenv, inferencer);

        // Ensure the branch types unify.
        inferencer.unify(tType, fType,
                buildErrorMessage("Types of then and else branches must match."));

        // Return the (possibly substituted) type of the true branch.
        return inferencer.getSubstitutions().apply(tType);
    }

    /**
     * Display an AST subtree with the indentation specified.
     * 
     * @param indentAmt the amount of indentation to perform.
     */
    @Override
    public void displaySubtree(int indentAmt)
    {
        printIndented("If(", indentAmt);

        printIndented("cond:", indentAmt + 2);
        condition.displaySubtree(indentAmt + 4);

        printIndented("then:", indentAmt + 2);
        trueBranch.displaySubtree(indentAmt + 4);

        printIndented("else:", indentAmt + 2);
        falseBranch.displaySubtree(indentAmt + 4);

        printIndented(")", indentAmt);
    }
}
