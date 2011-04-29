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
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.TransactionFactoryBuilder;
import org.multiverse.api.exceptions.ControlFlowError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;

/**
 * @author Vaclav Pech
 */
public abstract class GParsStm {
    static final String THE_CODE_FOR_AN_ATOMIC_BLOCK_MUST_NOT_BE_NULL = "The code for an atomic block must not be null.";
    static final String AN_EXCEPTION_WAS_EXPECTED_TO_BE_THROWN_FROM_UNWRAP_STM_CONTROL_ERROR_FOR = "An exception was expected to be thrown from unwrapStmControlError for ";
    private static final String CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED = "Cannot create an atomic block. Some of the specified parameters are not supported. ";

    //todo transactional properties to avoid creating getters only to call atomic{}

    //todo reconsider need and accessibility
    public static final TransactionFactoryBuilder transactionFactory = getGlobalStmInstance().createTransactionFactoryBuilder();
    private static final AtomicBlock defaultAtomicBlock = getGlobalStmInstance().createTransactionFactoryBuilder().setFamilyName("GPars.Stm").buildAtomicBlock();

    public static AtomicBlock createAtomicBlock(final Map<String, Object> params) {
        TransactionFactoryBuilder localFactory = transactionFactory;

        final Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (final Map.Entry<String, Object> entry : entries) {
            if (entry.getValue() == null)
                throw new IllegalArgumentException("Cannot create an atomic block. The value for " + entry.getKey() + " is null.");
            if (entry.getKey() == null || "".equals(entry.getKey().trim()))
                throw new IllegalArgumentException("Cannot create an atomic block. Found an empty key.");
            final String key = "set" + Character.toUpperCase(entry.getKey().charAt(0)) + entry.getKey().substring(1);

            try {
                final Method method;
                if (entry.getValue().getClass().equals(Long.class)) {
                    method = TransactionFactoryBuilder.class.getDeclaredMethod(key, Long.TYPE);
                } else if (entry.getValue().getClass().equals(Integer.class)) {
                    method = TransactionFactoryBuilder.class.getDeclaredMethod(key, Integer.TYPE);
                } else if (entry.getValue().getClass().equals(Boolean.class)) {
                    method = TransactionFactoryBuilder.class.getDeclaredMethod(key, Boolean.TYPE);
                } else {
                    method = TransactionFactoryBuilder.class.getDeclaredMethod(key, entry.getValue().getClass());
                }
                localFactory = (TransactionFactoryBuilder) method.invoke(localFactory, entry.getValue());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED + entry.getKey(), e);
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException(CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED + entry.getKey(), e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(CANNOT_CREATE_AN_ATOMIC_BLOCK_SOME_OF_THE_SPECIFIED_PARAMETERS_ARE_NOT_SUPPORTED + entry.getKey(), e);
            }

        }
        return localFactory.buildAtomicBlock();
    }

    public static <T> T atomic(final Closure code) {
        return defaultAtomicBlock.execute(new GParsAtomicBlock<T>(code));
    }

    public static <T> T atomic(final AtomicBlock block, final Closure code) {
        return block.execute(new GParsAtomicBlock<T>(code));
    }

    public static int atomicWithInt(final Closure code) {
        return defaultAtomicBlock.execute(new GParsAtomicIntBlock(code));
    }

    public static long atomicWithLong(final Closure code) {
        return defaultAtomicBlock.execute(new GParsAtomicLongBlock(code));
    }

    public static boolean atomicWithBoolean(final Closure code) {
        return defaultAtomicBlock.execute(new GParsAtomicBooleanBlock(code));
    }

    public static double atomicWithDouble(final Closure code) {
        return defaultAtomicBlock.execute(new GParsAtomicDoubleBlock(code));
    }

    public static void atomicWithVoid(final Closure code) {
        defaultAtomicBlock.execute(new GParsAtomicVoidBlock(code));
    }

    static void unwrapStmControlError(final InvokerInvocationException e) {
        final Throwable cause = e.getCause();
        if (cause instanceof ControlFlowError) throw (Error) cause;
        else throw e;
    }

}
