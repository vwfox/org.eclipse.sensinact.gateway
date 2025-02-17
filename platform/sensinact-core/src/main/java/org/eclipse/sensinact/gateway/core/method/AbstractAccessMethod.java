/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Fixed;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract {@link AccessMethod} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractAccessMethod<T, R extends AccessMethodResponse<T>> implements AccessMethod<T, R> {
	private static final Logger LOG=LoggerFactory.getLogger(AbstractAccessMethod.class);

	/**
	 * Creates and returns an extended {@link AccessMethodResponseBuilder} of the
	 * appropriate type
	 * 
	 * @param parameters
	 * 
	 * @return the extended {@link AccessMethodResponseBuilder} type instance
	 */
	protected abstract <A extends AccessMethodResponseBuilder<T, R>> A createAccessMethodResponseBuilder(
			Object[] parameters);

	protected final AccessMethodExecutor preProcessingExecutor;
	protected final AccessMethodExecutor postProcessingExecutor;

	protected final AccessMethod.Type type;
	protected final Map<Signature, Deque<AccessMethodExecutor>> map;
	protected final Map<Shortcut, Signature> shortcuts;
	protected final String uri;

	protected final Mediator mediator;

	private final ErrorHandler errorHandler;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the {@link AccessMethod.Type} of the extended {@link AccessMethod}
	 *            type to instantiate
	 */
	protected AbstractAccessMethod(Mediator mediator, String uri, String type,
			AccessMethodExecutor preProcessingExecutor) {
		this(mediator, uri, type, preProcessingExecutor, null, null);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the {@link AccessMethod.Type} of the extended {@link AccessMethod}
	 *            type to instantiate
	 */
	protected AbstractAccessMethod(Mediator mediator, String uri, String type,
			AccessMethodExecutor preProcessingExecutor, ErrorHandler errorHandler) {
		this(mediator, uri, type, preProcessingExecutor, null, errorHandler);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the {@link AccessMethod.Type} of the extended {@link AccessMethod}
	 *            type to instantiate
	 */
	protected AbstractAccessMethod(Mediator mediator, String uri, String type,
			AccessMethodExecutor preProcessingExecutor, AccessMethodExecutor postProcessingExecutor,
			ErrorHandler errorHandler) {
		this.uri = uri;
		this.type = AccessMethod.Type.valueOf(type);
		this.mediator = mediator;
		this.map = new IdentityHashMap<Signature, Deque<AccessMethodExecutor>>();
		this.shortcuts = new IdentityHashMap<Shortcut, Signature>();
		this.preProcessingExecutor = preProcessingExecutor;
		this.postProcessingExecutor = postProcessingExecutor;
		this.errorHandler = errorHandler;
	}

	/**
	 * Returns the {@link Signature} of this {@link AccessMethod} whose parameter
	 * types are the same, in order, as the ones specified in the array argument
	 * 
	 * @param parameterTypes
	 *            the array of types of the searched {@link Signature}
	 * @return the {@link Signature} of this method whose parameter types are the
	 *         same as the specified ones
	 */
	protected Signature getSignature(Class<?>[] parameterTypes) {
		Set<Signature> signatures = this.getSignatures();
		Iterator<Signature> iterator = signatures.iterator();

		while (iterator.hasNext()) {
			Signature signature = iterator.next();
			if (signature.equals(this.type.name(), parameterTypes == null ? new Class<?>[0] : parameterTypes)) {
				return signature;
			}
		}
		return null;
	}

	/**
	 * Returns a new {@link Signature} instance whose parameter types are the same
	 * as the ones specified in the array argument and whose type is the one of this
	 * {@link AccessMethod}
	 * 
	 * @param parameterTypes
	 *            the array of types of the {@link Signature} to create
	 * @return a new {@link Signature} instance
	 * @throws InvalidValueException 
	 */
	private Signature createSignature(Class<?>[] parameterTypes) throws InvalidValueException {
		Class<?>[] types = parameterTypes == null ? new Class<?>[0] : parameterTypes;

		String[] names = new String[types.length];

		int index = 0;
		int length = types.length;

		for (; index < length; index++) {
			names[index] = new StringBuilder().append("arg").append(index).toString();
		}
		return createSignature(types, names);
	}

	/**
	 * Returns a new {@link Signature} instance whose parameter types are the same
	 * as the ones specified in the types array argument, whose parameter names are
	 * the same as the ones specified in the strings array argument and whose type
	 * is the one of this {@link AccessMethod}
	 * 
	 * @param parameterTypes
	 *            the array of parameter types of the {@link Signature} to create
	 * @param names
	 *            the array of parameter string names of the {@link Signature} to
	 *            create
	 * @return a new {@link Signature} instance
	 * 
	 * @throws InvalidValueException 
	 */
	private Signature createSignature(Class<?>[] parameterTypes, String[] parameterNames) throws InvalidValueException {
		Signature signature = null;
		Class<?>[] types = parameterTypes == null ? new Class<?>[0] : parameterTypes;
		String[] names = parameterNames == null ? new String[0] : parameterNames;
		if (types.length > names.length) 
			return signature;
		Parameter[] parameters = new Parameter[types.length];
		int index = 0;
		int length = types.length;
		for (; index < length; index++) {
			try {
				parameters[index] = new Parameter(this.mediator, names[index], types[index]);
			} catch (InvalidValueException e) {
				// cannot happen
				e.printStackTrace();
			}
		}
		signature = new Signature(this.mediator, type.name(), parameters);
		return signature;
	}
	
	/**
	 * Creates a {@link Signature} using the parameter types array argument and maps
	 * it to the {@link AccessMethodExecutor} passed as parameter
	 * 
	 * @param parameterTypes
	 *            the parameter types array
	 * @param executor
	 *            the {@link AccessMethodExecutor} to map to the {@link Signature}
	 *            to create
	 * @param policy
	 *            the {@link AccessMethodExecutor.Execu tionPolicy} of the specified
	 *            {@link AccessMethodExecutor}
	 * @throws InvalidValueException 
	 */
	public void addSignature(Class<?>[] parameterTypes, AccessMethodExecutor executor, AccessMethodExecutor.ExecutionPolicy policy) 
		throws InvalidValueException {
		Signature signature = this.getSignature(parameterTypes);
		if (signature != null) {
			this.addSignature(signature, executor, policy);
			return;
		}
		this.addSignature(this.createSignature(parameterTypes), executor, policy);
	}

	/**
	 * Creates a {@link Signature} using the parameter types and parameter names
	 * arrays arguments and maps it to the {@link AccessMethodExecutor} passed as
	 * parameter
	 * 
	 * @param parameterNames
	 *            the parameter names array
	 * @param parameterTypes
	 *            the parameter types array
	 * @param executor
	 *            the {@link AccessMethodExecutor} to map to the {@link Signature}
	 *            to create
	 * @param policy
	 *            the {@link AccessMethodExecutor.ExecutionPolicy} of the specified
	 *            {@link AccessMethodExecutor}
	 * @throws InvalidValueException 
	 * @throws InvalidConstraintDefinitionException
	 */
	public void addSignature(Class<?>[] parameterTypes, String[] parameterNames, AccessMethodExecutor executor,
			AccessMethodExecutor.ExecutionPolicy policy) throws InvalidValueException {	
		Signature signature = this.getSignature(parameterTypes);
		if (signature != null) {
			this.addSignature(signature, executor, policy);
			return;
		}
		this.addSignature(this.createSignature(parameterTypes, parameterNames), executor, policy);
	}

	/**
	 * Adds and maps the {@link Signature} argument to the
	 * {@link AccessMethodExecutor} passed as parameter
	 * 
	 * @param signat
	 *            the {@link Signature} to add
	 * @param executor
	 *            the {@link AccessMethodExecutor} to map to the {@link Signature}
	 * @param policy
	 *            the {@link AccessMethodExecutor.ExecutionPolicy} of the specified
	 *            {@link AccessMethodExecutor}
	 * @throws InvalidConstraintDefinitionException
	 */
	public void addSignature(Signature signature, AccessMethodExecutor executor,
			AccessMethodExecutor.ExecutionPolicy policy) {
		if (signature == null || signature.getName().intern() != this.type.name().intern()) {
			return;
		}
		Deque<AccessMethodExecutor> executors = this.map.get(signature);

		Map<Integer, Parameter> fixedParameters = new HashMap<Integer, Parameter>();

		if (executors == null) {
			Signature shortcut = signature;
			Signature reference = null;
			while ((reference = this.shortcuts.get(shortcut)) != null) {
				fixedParameters.putAll(((Shortcut) shortcut).getFixedParameters());
				shortcut = reference;
			}
			if ((executors = this.map.get(shortcut)) == null) {
				if ((shortcut = this.getSignature(signature.getParameterTypes())) != null) {
					this.addSignature(shortcut, executor, policy);
					return;
				}
				executors = new LinkedList<AccessMethodExecutor>();
				this.map.put(signature, executors);
			}
		}
		if (executor == null) {
			return;
		}
		AccessMethodExecutor.ExecutionPolicy executionPolicy = policy == null
				? AccessMethodExecutor.ExecutionPolicy.AFTER
				: policy;
		AccessMethodExecutor methodExecutor = executor;

		if (fixedParameters != null && !fixedParameters.isEmpty()) {
			methodExecutor = new AccessMethodExecutorWrapper(executor);
			Iterator<Map.Entry<Integer, Parameter>> iterator = fixedParameters.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<Integer, Parameter> entry = iterator.next();
				Parameter parameter = entry.getValue();
				try {
					((AccessMethodExecutorWrapper) methodExecutor).put(entry.getKey(), new Fixed(
						 parameter.getType(), parameter.getValue(), false));

				} catch (InvalidConstraintDefinitionException e) {
					if (LOG.isErrorEnabled()) {
						LOG.error(e.getMessage(),e);
					}
					return;
				}
			}
		}
		switch (executionPolicy) {
		case AFTER:
			executors.addLast(methodExecutor);
			break;
		case BEFORE:
			executors.addFirst(methodExecutor);
			break;
		case REPLACE:
			executors.clear();
			executors.addFirst(methodExecutor);
			break;
		default:
			break;
		}
	}

	/**
	 * Registers the specified {@link Shortcut} mapped to the already registered
	 * {@link Signature}, passed as parameter
	 * 
	 * @param shortcut
	 *            the {@link Shortcut} to register
	 * @param signature
	 *            the {@link Signature} already registered to map to the specified
	 *            {@link Shortcut}
	 */
	public void addShortcut(Shortcut shortcut, Signature signature) {
		if (shortcut == null || signature == null || (this.map.get(signature) == null && this.shortcuts.get(signature) == null)) 
			return;		
		this.shortcuts.put(shortcut, signature);
	}

	@Override
	public AccessMethodDescription getDescription() {
		return new AccessMethodDescription(this);
	}

	@Override
	public String getName() {
		return this.type.name();
	}

	@Override
	public String getPath() {
		return this.uri;
	}

	@Override
	public int size() {
		return this.map.size() + this.shortcuts.size();
	}

	@Override
	public R invoke(Object[] parameters) {
		List<Signature> signatures = new ArrayList<>();
		
		signatures.addAll(this.shortcuts.keySet().stream().sorted(
			(s1,s2)->{return s1.length()<s2.length()?-1:(s1.length()>s2.length()?1:0);}
			).collect(Collectors.toList()));
		
		signatures.addAll(this.map.keySet().stream().sorted(
			(s1,s2)->{return s1.length()<s2.length()?-1:(s1.length()>s2.length()?1:0);}
			).collect(Collectors.toList()));
		
		Iterator<Signature> iterator = signatures.iterator();		
		while (iterator.hasNext()) {
			final Signature signature = iterator.next();
			final Parameter[] validatedParams = signature.validParameters(parameters);
			if (validatedParams != null) {
				return this.invoke(signature, validatedParams);
			}
		}
		
		return this.error(AccessMethodResponse.NOT_FOUND_ERROR_CODE, "Unknown signature");
	}

	/**
	 * Invokes this method using the specified {@link Signature}'s parameter values
	 * to parameterize the call
	 * 
	 * @param signature  the {@link Signature} of this method parameterizing the invocation
	 * @param validatedPrameters the parameters given to the method, validated for the given signature
	 * @return the resulting {@link SnaMessage}
	 */
	public synchronized <A extends AccessMethodResponseBuilder<T, R>> R invoke(Signature signature, Parameter[] validatedPrameters) {
		if (signature == null)
			return this.error(SnaErrorfulMessage.BAD_REQUEST_ERROR_CODE, "Null signature");

		Deque<AccessMethodExecutor> executors = null;
		Signature current = signature;
		Signature previous = null;
		while (true) {
			previous = current;
			current = this.shortcuts.get(current);

			if (current == null) {
				executors = this.map.get(previous);
				break;
			}
			if (!Shortcut.class.isAssignableFrom(current.getClass())) {
				executors = this.map.get(current);
				break;
			}
			((Shortcut) signature).push((Shortcut) current);
		};
		if (executors == null) 
			return this.error(SnaErrorfulMessage.NOT_FOUND_ERROR_CODE, "Unknown signature");

		final Object[] parameters = signature.values(validatedPrameters);
		A result = this.createAccessMethodResponseBuilder(parameters);
		
		if (preProcessingExecutor != null) 
			executors.addFirst(preProcessingExecutor);		
		
		if (postProcessingExecutor != null) 
			executors.addLast(postProcessingExecutor);
		
		Iterator<AccessMethodExecutor> iterator = executors.iterator();

		while (iterator.hasNext()) {
			AccessMethodExecutor executor = iterator.next();
			if (executor == null) 
				continue;			
			try {
				executor.execute(result);

			} catch (Exception exception) {
				result.registerException(exception);
				if (result.exitOnError()) {
					break;
				}
			}
		}
		if (preProcessingExecutor != null) 
			executors.removeFirst();
		
		if (postProcessingExecutor != null) 
			executors.removeLast();
		
		return result.createAccessMethodResponse();
	}

	private R error(int errorCode, String message) {
		return AccessMethodResponse.<T, R>error(mediator, this.getPath(), this.getType(), errorCode, message, null);
	}

	@Override
	public Set<Signature> getSignatures() {
		Set<Signature> signatures = new HashSet<Signature>(this.map.keySet());
		signatures.addAll(this.shortcuts.keySet());
		return Collections.unmodifiableSet(signatures);
	}

	@Override
	public AccessMethod.Type getType() {
		return this.type;
	}

	/**
	 * Deletes all registered {@link Signature}s and {@link Shortcut}s
	 */
	public void stop() {
		this.map.clear();
		this.shortcuts.clear();
	}

	@Override
	public ErrorHandler getErrorHandler() {
		return this.errorHandler;
	}
}
