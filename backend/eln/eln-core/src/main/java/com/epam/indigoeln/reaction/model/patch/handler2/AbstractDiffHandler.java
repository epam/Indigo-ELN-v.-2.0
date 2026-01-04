package com.epam.indigoeln.reaction.model.patch.handler2;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public abstract class AbstractDiffHandler<T, P> implements DiffHandler<T, P> {

    @Override
    @Nullable
    public final Patched<P> compare(@Nullable T a, @Nullable T b) {
        if (a == b || isEmpty(a) && isEmpty(b)) { // no change, simple check
            return null;
        }
        if (isEmpty(b)) { // deleted
            return doDeleted(a);
        }
        if (isEmpty(a)) { // created
            return doCreated(b);
        }
        if (doEquals(a, b)) { // no change, advanced check
            return null;
        }
        return doCompare(a, b);
    }

    protected P doVerbatim(T value) {
        Patched<P> patched = doCompare(null, value);
        Preconditions.checkState(patched != null && patched.value() != null);
        return patched.value();
    }

    protected boolean isEmpty(@Nullable T value) {
        return value == null;
    }

    protected boolean doEquals(T a, T b) {
        return false;
    }

    protected Patched<P> doDeleted(T a) {
        return Patched.deleted(doVerbatim(a));
    }

    protected Patched<P> doCreated(T b) {
        return Patched.created(doVerbatim(b));
    }

    @Nullable
    protected abstract Patched<P> doCompare(@Nullable T a, T b);
}
