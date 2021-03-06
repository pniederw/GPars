// GPars - Groovy Parallel Systems
//
// Copyright © 2008-11  The original author or authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package groovyx.gpars.stm;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicIntClosure;

/**
 * A default implementation of org.multiverse.api.closures.AtomicIntClosure properly handling exception propagation
 *
 * @author Vaclav Pech
 */
final class GParsAtomicIntBlock implements AtomicIntClosure {
    private final Closure code;

    GParsAtomicIntBlock(final Closure code) {
        if (code == null) throw new IllegalArgumentException(GParsStm.THE_CODE_FOR_AN_ATOMIC_BLOCK_MUST_NOT_BE_NULL);
        this.code = code;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public int execute(final Transaction transaction) {
        try {
            return (Integer) code.call(transaction);
        } catch (InvokerInvocationException e) {
            GParsStm.unwrapStmControlError(e);
            throw new IllegalStateException(GParsStm.AN_EXCEPTION_WAS_EXPECTED_TO_BE_THROWN_FROM_UNWRAP_STM_CONTROL_ERROR_FOR + e, e);
        }
    }
}
