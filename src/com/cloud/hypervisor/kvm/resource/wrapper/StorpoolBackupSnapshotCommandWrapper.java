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

package com.cloud.hypervisor.kvm.resource.wrapper;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import org.apache.cloudstack.storage.command.CopyCmdAnswer;
import org.apache.cloudstack.storage.to.SnapshotObjectTO;
import org.apache.cloudstack.utils.qemu.QemuImg;
import org.apache.cloudstack.utils.qemu.QemuImg.PhysicalDiskFormat;
import org.apache.cloudstack.utils.qemu.QemuImgFile;

import com.cloud.agent.api.storage.StorpoolBackupSnapshotCommand;
import com.cloud.agent.api.to.DataStoreTO;
import com.cloud.agent.api.to.NfsTO;
import com.cloud.hypervisor.kvm.resource.LibvirtComputingResource;
import com.cloud.hypervisor.kvm.storage.KVMStoragePool;
import com.cloud.hypervisor.kvm.storage.KVMStoragePoolManager;
import com.cloud.hypervisor.kvm.storage.StorpoolStorageAdaptor;
import com.cloud.resource.CommandWrapper;
import com.cloud.resource.ResourceWrapper;

import static com.cloud.hypervisor.kvm.storage.StorpoolStorageAdaptor.SP_LOG;

@ResourceWrapper(handles = StorpoolBackupSnapshotCommand.class)
public final class StorpoolBackupSnapshotCommandWrapper extends CommandWrapper<StorpoolBackupSnapshotCommand, CopyCmdAnswer, LibvirtComputingResource> {

    private static final Logger s_logger = Logger.getLogger(StorpoolBackupSnapshotCommandWrapper.class);

    @Override
    public CopyCmdAnswer execute(final StorpoolBackupSnapshotCommand cmd, final LibvirtComputingResource libvirtComputingResource) {
        String srcPath = null;
        KVMStoragePool secondaryPool = null;

        try {
            final SnapshotObjectTO src = cmd.getSrcTO();
            final SnapshotObjectTO dst = cmd.getDstTO();
            final KVMStoragePoolManager storagePoolMgr = libvirtComputingResource.getStoragePoolMgr();

            SP_LOG("StorpoolBackupSnapshotCommandWrapper.execute: src=" + src.getPath() + "dst=" + dst.getPath());
            StorpoolStorageAdaptor.attachOrDetachVolume("attach", "snapshot", src.getPath());
            srcPath = src.getPath();

            final QemuImgFile srcFile = new QemuImgFile(srcPath, PhysicalDiskFormat.RAW);

            final DataStoreTO dstDataStore = dst.getDataStore();
            if (!(dstDataStore instanceof NfsTO)) {
                return new CopyCmdAnswer("Backup Storpool snapshot: Only NFS secondary supported at present!");
            }

            secondaryPool = storagePoolMgr.getStoragePoolByURI(dstDataStore.getUrl());

            final String dstDir = secondaryPool.getLocalPath() + File.separator + dst.getPath();
            FileUtils.forceMkdir(new File(dstDir));

            final String dstPath = dstDir + File.separator + dst.getName();
            final QemuImgFile dstFile = new QemuImgFile(dstPath, PhysicalDiskFormat.RAW);

            final QemuImg qemu = new QemuImg(cmd.getWaitInMillSeconds());
            qemu.convert(srcFile, dstFile);

            final File snapFile = new File(dstPath);
            final long size = snapFile.exists() ? snapFile.length() : 0;

            final SnapshotObjectTO snapshot = new SnapshotObjectTO();
            snapshot.setPath(dst.getPath() + File.separator + dst.getName());
            snapshot.setPhysicalSize(size);

            return new CopyCmdAnswer(snapshot);
        } catch (final Exception e) {
            final String error = "failed to backup snapshot: " + e.getMessage();
            SP_LOG(error);
            s_logger.debug(error);
            return new CopyCmdAnswer(error);
        } finally {
            if (srcPath != null) {
                StorpoolStorageAdaptor.attachOrDetachVolume("detach", "snapshot", srcPath);
            }

            if (secondaryPool != null) {
                try {
                    secondaryPool.delete();
                } catch (final Exception e) {
                    s_logger.debug("Failed to delete secondary storage", e);
                }
            }
        }
    }
}
