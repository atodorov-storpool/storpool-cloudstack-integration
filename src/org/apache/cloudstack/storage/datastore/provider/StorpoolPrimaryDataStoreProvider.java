/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.storage.datastore.provider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cloud.utils.component.ComponentContext;

import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreLifeCycle;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStoreDriver;
import org.apache.cloudstack.engine.subsystem.api.storage.HypervisorHostListener;
import org.apache.cloudstack.engine.subsystem.api.storage.PrimaryDataStoreProvider;
import org.apache.cloudstack.storage.datastore.driver.StorpoolPrimaryDataStoreDriver;
import org.apache.cloudstack.storage.datastore.lifecycle.StorpoolPrimaryDataStoreLifeCycle;
import org.apache.cloudstack.storage.datastore.util.StorpoolUtil;


public class StorpoolPrimaryDataStoreProvider implements PrimaryDataStoreProvider {

    protected DataStoreLifeCycle lifecycle;
    protected DataStoreDriver driver;
    protected HypervisorHostListener listener;

    StorpoolPrimaryDataStoreProvider() {
    }

    @Override
    public String getName() {
        return StorpoolUtil.SP_PROVIDER_NAME;
    }

    @Override
    public DataStoreLifeCycle getDataStoreLifeCycle() {
        return lifecycle;
    }

    @Override
    public DataStoreDriver getDataStoreDriver() {
        return driver;
    }

    @Override
    public HypervisorHostListener getHostListener() {
        return listener;
    }

    @Override
    public boolean configure(Map<String, Object> params) {
        lifecycle = ComponentContext.inject(StorpoolPrimaryDataStoreLifeCycle.class);
        driver = ComponentContext.inject(StorpoolPrimaryDataStoreDriver.class);
        listener = ComponentContext.inject(StorpoolHostListener.class);
        return true;
    }

    @Override
    public Set<DataStoreProviderType> getTypes() {
        Set<DataStoreProviderType> types = new HashSet<DataStoreProviderType>();
        types.add(DataStoreProviderType.PRIMARY);
        return types;
    }
}
