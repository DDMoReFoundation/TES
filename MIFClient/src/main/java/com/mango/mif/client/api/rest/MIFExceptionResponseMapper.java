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

import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;

import com.mango.mif.exception.MIFException;

/**
 * Unmarshalls MIFExceptoin from a Response
 */
public class MIFExceptionResponseMapper implements
		ResponseExceptionMapper<MIFException> {
    private final static String ERROR_TAG = "ETag";
	@Override
	public MIFException fromResponse(Response response) {
		StringBuilder msg = new StringBuilder();
		if (response != null) {
			if (response.getMetadata() != null
					&&  response.getMetadata().get(ERROR_TAG)!= null) {
				msg.append(retrieveErrorMessage(response.getMetadata().get(ERROR_TAG)));
			} else {
				msg.append("Technical Failure: ");
				msg.append(response.getStatus());
			}
		}
		return new MIFException(msg.toString());
	}

    private String retrieveErrorMessage(List<Object> tags) {
        StringBuilder msg = new StringBuilder();
        for (Object tagObject : tags) {
            String tag = tagObject.toString();
            tag = StringUtils.trimToEmpty(tag);
            if(tag.startsWith("\"") && tag.endsWith("\"")) {
                tag = tag.substring(1, tag.length()-1);
            }
            msg.append(tag);
        }
        return msg.toString();
    }

}
