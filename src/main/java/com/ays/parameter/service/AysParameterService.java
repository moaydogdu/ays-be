package com.ays.parameter.service;

import com.ays.parameter.model.AysParameter;

import java.util.Set;

/**
 * A service interface for managing AysParameters.
 */
public interface AysParameterService {

    /**
     * Retrieves a set of AysParameters that their name starts with the specified prefix.
     *
     * @param prefixOfName the prefix of the name to search for
     * @return a set of AysParameter entities
     */
    Set<AysParameter> getParameters(String prefixOfName);

}
