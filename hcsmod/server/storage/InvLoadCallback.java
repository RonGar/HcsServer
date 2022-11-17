/*
 * Decompiled with CFR 0.150.
 */
package hcsmod.server.storage;

import hcsmod.server.storage.StorageInventory;

public interface InvLoadCallback {
    public void loadingDone(StorageInventory var1);

    default public void loadingFailed(String description) {
    }
}

