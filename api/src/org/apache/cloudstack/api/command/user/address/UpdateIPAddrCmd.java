// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.api.command.user.address;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCustomIdCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AccountResponse;
import org.apache.cloudstack.api.response.IPAddressResponse;
import org.apache.log4j.Logger;

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.IpAddress;
import com.cloud.user.Account;

@APICommand(name = "updatePublicIpAddress", description = "Updates an ip address", responseObject = IPAddressResponse.class)
public class UpdateIPAddrCmd extends BaseAsyncCustomIdCmd {
    public static final Logger s_logger = Logger.getLogger(UpdateIPAddrCmd.class.getName());
    private static final String s_name = "updateipaddressresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = IPAddressResponse.class, required = true, description = "the id of the public ip address"
            + " to update")
    private Long id;
    // unexposed parameter needed for events logging
    @Parameter(name = ApiConstants.ACCOUNT_ID, type = CommandType.UUID, entityType = AccountResponse.class, expose = false)
    private Long ownerId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getCommandName() {
        return s_name;
    }

    public Long getId() {
        return id;
    }


    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////
    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_IP_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return ("Updating ip address with id=" + id);
    }

    @Override
    public long getEntityOwnerId() {
        if (ownerId == null) {
            IpAddress ip = getIpAddress(id);
            if (ip == null) {
                throw new InvalidParameterValueException("Unable to find ip address by id=" + id);
            }
            ownerId = ip.getAccountId();
        }

        if (ownerId == null) {
            return Account.ACCOUNT_ID_SYSTEM;
        }
        return ownerId;
    }

    private IpAddress getIpAddress(long id) {
        IpAddress ip = _entityMgr.findById(IpAddress.class, id);

        if (ip == null) {
            throw new InvalidParameterValueException("Unable to find ip address by id=" + id);
        } else {
            return ip;
        }
    }

    @Override
    public void checkUuid() {
        if (this.getCustomId() != null) {
            _uuidMgr.checkUuid(this.getCustomId(), IpAddress.class);
        }
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException, ServerApiException, ConcurrentOperationException, ResourceAllocationException,
            NetworkRuleConflictException {

        IpAddress result = _networkService.updateIP(getId(), this.getCustomId());
        IPAddressResponse ipResponse = _responseGenerator.createIPAddressResponse(result);
        ipResponse.setResponseName(getCommandName());
        setResponseObject(ipResponse);
    }
}