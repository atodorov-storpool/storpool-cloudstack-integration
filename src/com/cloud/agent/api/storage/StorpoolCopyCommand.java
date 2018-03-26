//
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
//

package com.cloud.agent.api.storage;

import org.apache.cloudstack.storage.command.StorageSubSystemCommand;

import com.cloud.agent.api.to.DataTO;

public class StorpoolCopyCommand<S extends DataTO, D extends DataTO> extends StorageSubSystemCommand {
    private S srcTO;
    private D dstTO;
    private boolean executeInSequence = false;

    public StorpoolCopyCommand(final DataTO srcTO, final DataTO dstTO, final int timeout, final boolean executeInSequence) {
        super();
        this.srcTO = (S)srcTO;
        this.dstTO = (D)dstTO;
        setWait(timeout);
        this.executeInSequence = executeInSequence;
    }

    public S getSrcTO() {
        return srcTO;
    }

    public D getDstTO() {
        return dstTO;
    }

    public int getWaitInMillSeconds() {
        return getWait() * 1000;
    }

    @Override
    public boolean executeInSequence() {
        return executeInSequence;
    }

    @Override
    public void setExecuteInSequence(final boolean inSeq) {
        executeInSequence = inSeq;
    }
}
