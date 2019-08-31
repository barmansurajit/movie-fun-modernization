package org.superbiz.moviefun.blobstore;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

public class CloudStorageStore implements BlobStore {

    private final Tika tika = new Tika();
    private final Storage storage;
    private final String bucketName;

    public CloudStorageStore(Storage storage, String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        storage.create(BlobInfo.newBuilder(bucketName, blob.name)
                .build(), IOUtils.toByteArray(blob.inputStream));
    }

    @Override
    public Optional<Blob> get(String name)  {
        com.google.cloud.storage.Blob blob = storage.get(BlobId.of(bucketName, name));
        if (null == blob)
            return Optional.empty();

        byte[] bytes = blob.getContent();

        return Optional.of(new Blob(
                name,
                new ByteArrayInputStream(bytes),
                tika.detect(bytes)
        ));
    }

    public void deleteAll() {
        Page<com.google.cloud.storage.Blob> blobs = storage.list(bucketName);
        for (com.google.cloud.storage.Blob blob : blobs.getValues()) {
            storage.delete(blob.getBlobId());
        }
    }
}