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
import org.osgi.framework.dto.ServiceReferenceDTO;

/**
 * A representation of a bound reference to a service.
 * 
 * @since 1.3
 * @NotThreadSafe
 * @author $Id: 1ba28863312f0c0784e4a5596f991a6a6a68c147 $
 */
public class BoundReferenceDTO extends DTO {
	/**
	 * The name of the declared reference.
	 * 
	 * <p>
	 * This is declared in the {@code name} attribute of the {@code reference}
	 * element of the component description.
	 * 
	 * @see ComponentDescriptionDTO#name
	 */
	public String					name;

	/**
	 * The target property of the bound reference.
	 * 
	 * <p>
	 * This is the value of the {@link ComponentConfigurationDTO#properties
	 * component property} whose name is the concatenation of the
	 * {@link ReferenceDTO#name declared reference name} and
	 * &quot;.target&quot;. This will be {@code null} if no target property is
	 * set for the reference.
	 */
	public String					target;

	/**
	 * The bound services.
	 * 
	 * <p>
	 * Each {@link ServiceReferenceDTO} in the array represents a service bound
	 * to the component configuration.
	 */
	public ServiceReferenceDTO[]	serviceReferences;
}
