/*
 * Copyright 2016-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */
package org.forgerock.openicf.connectors.mParticle;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

/**
 * Extends the {@link AbstractConfiguration} class to provide all the necessary
 * parameters to initialize the mParticle Connector.
 */
public class mParticleConfiguration extends AbstractConfiguration implements StatefulConfiguration {


    // Exposed configuration properties.

    /**
     * API Endpoint
     */
    private String apiEndpoint;

    /**
     * Server to Server Key
     */
    private String serverKey = null;

    /**
     * Server to Server Secret
     */
    private GuardedString serverSecret = null;

    /**
     * Production Environment
     */
    private boolean productionEnvironment = false;

    /**
     * mParticle Attributes
     */
    private String[] mParticleAttributes = {};

    private HttpClient client;


    /**
     * Constructor.
     */
    public mParticleConfiguration() {

    }


    @ConfigurationProperty(order = 1, displayMessageKey = "apiEndpoint.display",
            groupMessageKey = "basic.group", helpMessageKey = "apiEndpoint.help",
            required = true)
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }


    @ConfigurationProperty(order = 2, displayMessageKey = "serverKey.display",
            groupMessageKey = "basic.group", helpMessageKey = "serverKey.help",
            required = true)
    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    @ConfigurationProperty(order = 3, displayMessageKey = "serverSecret.display",
            groupMessageKey = "basic.group", helpMessageKey = "serverSecret.help",
            required = true, confidential = true)
    public GuardedString getServerSecret() {
        return serverSecret;
    }

    public void setServerSecret(GuardedString serverSecret) {
        this.serverSecret = serverSecret;
    }

    @ConfigurationProperty(order = 4, displayMessageKey = "productionEnvironment.display",
            groupMessageKey = "basic.group", helpMessageKey = "productionEnvironment.help")
    public boolean getProductionEnvironment() {
        return productionEnvironment;
    }

    public void setProductionEnvironment(boolean productionEnvironment) {
        this.productionEnvironment = productionEnvironment;
    }

    @ConfigurationProperty(order = 5, displayMessageKey = "mParticleAttributes.display",
            groupMessageKey = "basic.group", helpMessageKey = "mParticleAttributes.help")
    public String[] getmParticleAttributes() {
        return mParticleAttributes.clone();
    }

    public void setmParticleAttributes(String... mParticleAttributes) {
        this.mParticleAttributes = mParticleAttributes.clone();
    }


    HttpClient getClient() {
        synchronized (this) {
            if (this.client == null) {
                this.client = new HttpClient(this);
            }
        }
        return this.client;
    }

    /**
     * {@inheritDoc}
     */
    public void validate() {
        if (StringUtil.isBlank(apiEndpoint)) {
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }

        if (StringUtil.isBlank(serverKey)) {
            throw new IllegalArgumentException("Server Key cannot be null or empty.");
        }

        if (StringUtil.isBlank(SecurityUtil.decrypt(serverSecret))) {
            throw new IllegalArgumentException("Server Secret cannot be null or empty.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void release() {
    }

}
