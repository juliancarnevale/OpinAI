package com.opinai.service;

import java.io.InputStream;

public interface StorageService {
    /**
     * Store content associated with a key and return the key.
     */
    String store(String key, byte[] content);

    /**
     * Retrieve content as InputStream for the given key.
     */
    InputStream retrieve(String key);

    /**
     * Delete content associated with a key.
     */
    void delete(String key);
}
