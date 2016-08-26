/*******************************************************************************
 * Copyright (C) 2016 Mango Business Solutions Ltd, http://www.mango-solutions.com
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/agpl-3.0.html>.
 *******************************************************************************/

package com.mango.mif.client.api.rest;

import java.util.Iterator;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;

public class JobServiceResponseExceptionMapper implements
		ResponseExceptionMapper<JobServiceException> {

	@Override
	public JobServiceException fromResponse(Response response) {
		StringBuilder msg = new StringBuilder();
		if (response != null) {
			if (response.getMetadata() != null
					&& response.getMetadata().get("ETag") != null) {
				Iterator<Object> tags = response.getMetadata().get("ETag")
						.iterator();
				while (tags.hasNext()) {
					String tag = tags.next().toString();
					msg.append(tag);
					msg.append(String.format(" %n"));
				}
			} else {
				msg.append("Technical Failure: ");
				msg.append(response.getStatus());
			}
		}
		return new JobServiceException(msg.toString());
	}

}
