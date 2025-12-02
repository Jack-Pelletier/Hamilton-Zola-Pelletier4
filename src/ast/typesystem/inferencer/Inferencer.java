/*
 *   Copyright (C) 2022 -- 2025  Zachary A. Kissel
 *
 *   This program is free software: you can redistribute it and or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY or MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ast.typesystem.inferencer;

import ast.typesystem.TypeException;
import ast.typesystem.types.BoolType;
import ast.typesystem.types.FunType;
import ast.typesystem.types.IntType;
import ast.typesystem.types.ListType;
import ast.typesystem.types.RealType;
import ast.typesystem.types.Type;
import ast.typesystem.types.VarType;

/**
 * Represents the core type infrencer. It amasses a set of type equations and
 * solves them through unification. The solutions can then be applied to type
 * variables by calling {@code applySubstitution}. To add new constraints the
 * consumer must call {@code unify}.
 *
 * @author Zach Kissel
 */
public class Inferencer
{
    // The current type equation solutions.
    private Substitutions subst;

    /**
     * The default constructor builds a new type substitution map.
     */
    public Inferencer()
    {
        subst = new Substitutions();
    }

    /**
     * Get the string form of infrerencer.
     *
     * @return A string representation of the known substitutions.
     */
    @Override
    public String toString()
    {
        return subst.toString();
    }

    /**
     * Get the substitution map from the inferencer.
     *
     * @return the substitution map of the inferencer.
     */
    public Substitutions getSubstitutions()
    {
        return subst;
    }

    /**
     * Unifies the first and second type updating the substitution map if
     * needed. In particular this method attempts to find a set of substitutions
     * that makes type1 and type2 equal. If no such set of substitutions
     * exists, a type exception is thrown.
     *
     * @param type1 the first type
     * @param type2 the second type
     * @param msg   the text to include in the error message, in case of error.
     * @throws TypeException if the types can not be unified.
     */
    public void unify(Type type1, Type type2, String msg) throws TypeException
    {
        // Apply the known substitutions first.
        type1 = subst.apply(type1);
        type2 = subst.apply(type2);

        if (type1 == null || type2 == null)
        {
            throw new TypeException("Invalid type or unknown value.");
        }

        // If they are already equal, nothing to do.
        if (type1.equals(type2))
            return;

        // Try to bind type1 to type2 if it is a type variable.
        if (type1 instanceof VarType)
        {
            VarType tv = (VarType) type1;
            if (tv.checkConstraint(type2) && noOccurrence(tv, type2))
            {
                subst.updateSubstitutions(tv, type2);
                return;
            }
            else
            {
                throw new TypeException("Unification error: " + msg);
            }
        }

        // Try to bind type2 to type1 if it is a type variable.
        if (type2 instanceof VarType)
        {
            VarType tv = (VarType) type2;
            if (tv.checkConstraint(type1) && noOccurrence(tv, type1))
            {
                subst.updateSubstitutions(tv, type1);
                return;
            }
            else
            {
                throw new TypeException("Unification error: " + msg);
            }
        }

        // Handle list types: element types must unify.
        if (type1 instanceof ListType && type2 instanceof ListType)
        {
            ListType l1 = (ListType) type1;
            ListType l2 = (ListType) type2;
            unify(l1.getElementType(), l2.getElementType(), msg);
            return;
        }

        // Handle function types: parameter and return types must both unify.
        if (type1 instanceof FunType && type2 instanceof FunType)
        {
            FunType f1 = (FunType) type1;
            FunType f2 = (FunType) type2;

            unify(f1.getParamType(), f2.getParamType(), msg);
            unify(f1.getReturnType(), f2.getReturnType(), msg);
            return;
        }

        // At this point they are different concrete types that do not match.
        throw new TypeException("Unification error: " + msg);
    }

    /**
     * Makes sure that tv does not appear in ty. This is used by unification to
     * ensure that a type variable on the left hand side of an equation does not
     * appear on the right hand side of the equation.
     *
     * @param tv the type variable.
     * @param ty the type we want it bound to.
     * @return true if tv does not appear in ty.
     */
    private boolean noOccurrence(VarType tv, Type ty)
    {
        if (ty instanceof IntType || ty instanceof RealType
                || ty instanceof BoolType)
        {
            return true;
        }
        else if (ty instanceof VarType)
        {
            return !tv.equals((VarType) ty);
        }
        else if (ty instanceof ListType)
        {
            return noOccurrence(tv, ((ListType) ty).getElementType());
        }
        else if (ty instanceof FunType)
        {
            FunType f = (FunType) ty;
            return noOccurrence(tv, f.getParamType())
                    && noOccurrence(tv, f.getReturnType());
        }

        return false;
    }
}
