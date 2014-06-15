/*
 * Copyright (c) OSGi Alliance (2013, 2014). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.service.component.runtime.dto;

import org.osgi.dto.DTO;

/**
 * A representation of a declared reference to a service.
 * 
 * @since 1.3
 * @NotThreadSafe
 * @author $Id: 2fc8a3deac2ece6b9fff4878dbe3f082faadd5f8 $
 */
public class ReferenceDTO extends DTO {
	/**
	 * The name of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code name} attribute of the {@code reference}
	 * element. This will be the default name if the component description does
	 * not declare a name for the reference.
	 */
	public String	name;

	/**
	 * The service interface of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code interface} attribute of the
	 * {@code reference} element.
	 */
	public String	interfaceName;

	/**
	 * The cardinality of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code cardinality} attribute of the
	 * {@code reference} element. This will be the default cardinality if the
	 * component description does not declare a cardinality for the reference.
	 */
	public String	cardinality;

	/**
	 * The policy of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code policy} attribute of the {@code reference}
	 * element. This will be the default policy if the component description
	 * does not declare a policy for the reference.
	 */
	public String	policy;

	/**
	 * The policy option of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code policy-option} attribute of the
	 * {@code reference} element. This will be the default policy option if the
	 * component description does not declare a policy option for the reference.
	 */
	public String	policyOption;

	/**
	 * The target of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code target} attribute of the {@code reference}
	 * element. This will be {@code null} if the component description does not
	 * declare a target for the reference.
	 */
	public String	target;

	/**
	 * The name of the bind method of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code bind} attribute of the {@code reference}
	 * element. This will be {@code null} if the component description does not
	 * declare a bind method for the reference.
	 */
	public String	bind;

	/**
	 * The name of the unbind method of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code unbind} attribute of the {@code reference}
	 * element. This will be {@code null} if the component description does not
	 * declare an unbind method for the reference.
	 */
	public String	unbind;

	/**
	 * The name of the updated method of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code updated} attribute of the
	 * {@code reference} element. This will be {@code null} if the component
	 * description does not declare an updated method for the reference.
	 */
	public String	updated;

	/**
	 * The scope of the reference.
	 * 
	 * <p>
	 * This is declared in the {@code scope} attribute of the {@code reference}
	 * element. This will be the default scope if the component description does
	 * not declare a scope for the reference.
	 */
	public String	scope;
}
