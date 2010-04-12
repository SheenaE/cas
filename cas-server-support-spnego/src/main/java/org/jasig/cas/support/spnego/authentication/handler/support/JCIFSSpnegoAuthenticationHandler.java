/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.spnego.authentication.handler.support;

import jcifs.spnego.Authentication;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.BadCredentialsAuthenticationException;
import org.jasig.cas.server.authentication.AbstractPreAndPostProcessingAuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.support.spnego.authentication.principal.SpnegoCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.regex.Pattern;

/**
 * Implementation of an AuthenticationHandler for SPNEGO supports. This Handler
 * support both NTLM and Kerberos. NTLM is disabled by default.
 * 
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class JCIFSSpnegoAuthenticationHandler extends
        AbstractPreAndPostProcessingAuthenticationHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Authentication authentication;

    /**
     * Principal contains the DomainName ? (true by default).
     */
    private boolean principalWithDomainName = true;

    /**
     * Allow SPNEGO/NTLM Token as valid credentials. (false by default)
     */
    private boolean isNTLMallowed = false;

    protected boolean doAuthentication(final Credentials credentials)
        throws AuthenticationException {
        final SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;
        Principal principal;
        byte[] nextToken;
        try {
            // proceed authentication using jcifs
            synchronized (this) {
                this.authentication.reset();
                this.authentication.process(spnegoCredentials.getInitToken());
                principal = this.authentication.getPrincipal();
                nextToken = this.authentication.getNextToken();
            }
        } catch (jcifs.spnego.AuthenticationException e) {
            throw new BadCredentialsAuthenticationException(e);
        }
        // evaluate jcifs response
        if (nextToken != null) {
            logger.debug("Setting nextToken in credentials");
            spnegoCredentials.setNextToken(nextToken);
        } else {
            logger.debug("nextToken is null");
        }

        if (principal != null) {
            if (spnegoCredentials.isNtlm()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("NTLM Credentials is valid for user ["
                        + principal.getName() + "]");
                }
                spnegoCredentials.setPrincipal(getSimplePrincipal(principal
                    .getName(), true));
                return this.isNTLMallowed;
            }
            // else => kerberos
            if (logger.isDebugEnabled()) {
                logger.debug("Kerberos Credentials is valid for user ["
                    + principal.getName() + "]");
            }
            spnegoCredentials.setPrincipal(getSimplePrincipal(principal
                .getName(), false));
            return true;

        }

        logger
            .debug("Principal is null, the processing of the SPNEGO Token failed");
        return false;
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null
            && SpnegoCredentials.class.equals(credentials.getClass());
    }

    public void setAuthentication(final Authentication authentication) {
        this.authentication = authentication;
    }

    public void setPrincipalWithDomainName(final boolean principalWithDomainName) {
        this.principalWithDomainName = principalWithDomainName;
    }

    public void setNTLMallowed(final boolean isNTLMallowed) {
        this.isNTLMallowed = isNTLMallowed;
    }

    protected SimplePrincipal getSimplePrincipal(final String name,
        final boolean isNtlm) {
        if (this.principalWithDomainName) {
            return new SimplePrincipal(name);
        }
        if (isNtlm) {
        	return Pattern.matches("\\S+\\\\\\S+", name) ? new SimplePrincipal
        			(name.split("\\\\")[1]) : new SimplePrincipal(name);
        }
        return new SimplePrincipal(name.split("@")[0]);
    }
}
