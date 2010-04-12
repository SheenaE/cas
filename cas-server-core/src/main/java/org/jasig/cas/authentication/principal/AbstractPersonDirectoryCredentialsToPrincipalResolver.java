/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.cas.server.authentication.AttributePrincipal;
import org.jasig.cas.server.authentication.AttributePrincipalFactory;
import org.jasig.cas.server.authentication.Credential;
import org.jasig.cas.server.authentication.CredentialToPrincipalResolver;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public abstract class AbstractPersonDirectoryCredentialsToPrincipalResolver implements CredentialToPrincipalResolver {

    /** Log instance. */
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    private boolean returnNullIfNoAttributes = false;

    @NotNull
    private final AttributePrincipalFactory attributePrincipalFactory;
    
    /** Repository of principal attributes to be retrieved */
    @NotNull
    private IPersonAttributeDao attributeRepository = new StubPersonAttributeDao(new HashMap<String, List<Object>>());

    protected AbstractPersonDirectoryCredentialsToPrincipalResolver(final AttributePrincipalFactory attributePrincipalFactory) {
        this.attributePrincipalFactory = attributePrincipalFactory;
    }

    public final AttributePrincipal resolve(final Credential credentials) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to resolve a principal...");
        }

        final String principalId = extractPrincipalId(credentials);
        
        if (principalId == null) {
            return null;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Creating SimplePrincipal for ["
                + principalId + "]");
        }

        final Map<String, List<Object>> attributes = this.attributeRepository.getPerson(principalId).getAttributes();

        // TODO this may be able to be simplified or removed because of the way the new principal stuff works
        if (attributes == null & !this.returnNullIfNoAttributes) {
            return this.attributePrincipalFactory.getAttributePrincipal(principalId);
        }

        if (attributes == null && this.returnNullIfNoAttributes) {
            return null;
        }
        
        final Map<String, Object> convertedAttributes = new HashMap<String, Object>();
        
        for (final Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue().size() == 1 ? entry.getValue().get(0) : entry.getValue();
            convertedAttributes.put(key, value);
        }
        return this.attributePrincipalFactory.getAttributePrincipal(principalId);
    }
    
    /**
     * Extracts the id of the user from the provided credentials.
     * 
     * @param credentials the credentials provided by the user.
     * @return the username, or null if it could not be resolved.
     */
    protected abstract String extractPrincipalId(Credential credentials);
    
    public final void setAttributeRepository(final IPersonAttributeDao attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    public final void setReturnNullIfNoAttributes(final boolean returnNullIfNoAttributes) {
        this.returnNullIfNoAttributes = returnNullIfNoAttributes;
    }
}
