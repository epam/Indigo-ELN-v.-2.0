package com.epam.indigoeln.reaction.model.patch.handler2;

import org.jspecify.annotations.Nullable;

public abstract class AbstractDiffHandler<T, P> implements DiffHandler<T, P> {

    @Override
    @Nullable
    public final Patched<T, P> compare(@Nullable T a, @Nullable T b) {
        if (a == b || isEmpty(a) && isEmpty(b)) { // no change, simple check
            return null;
        }
        if (isEmpty(b)) { // deleted
            return Patched.deleted(a);
        }
        if (isEmpty(a)) { // created
            return Patched.created(b);
        }
        if (doEquals(a, b)) { // no change, advanced check
            return null;
        }
        return doCompare(a, b);
    }

    protected boolean isEmpty(@Nullable T value) {
        return value == null;
    }

    protected boolean doEquals(T a, T b) {
        return false;
    }

    @Nullable
    protected abstract Patched<T, P> doCompare(@Nullable T a, T b);
}
