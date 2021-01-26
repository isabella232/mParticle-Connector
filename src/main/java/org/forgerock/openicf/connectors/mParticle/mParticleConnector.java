/*
 * Copyright 2016-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

package org.forgerock.openicf.connectors.mParticle;


import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.AttributesAccessor;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Main implementation of the mParticle Connector.
 */
@ConnectorClass(
        displayNameKey = "mParticle.connector.display",
        configurationClass = mParticleConfiguration.class)
public class mParticleConnector implements PoolableConnector, CreateOp, TestOp, UpdateOp, SchemaOp, SearchOp<Filter> {

    /**
     * Setup logging for the {@link mParticleConnector}.
     */
    private static final Log logger = Log.getLog(mParticleConnector.class);
    private static final String REGISTRATION = "registration";
    private static final String UPDATE = "update";

    /**
     * Place holder for the {@link Configuration} passed into the init() method
     * {@link mParticleConnector#init(org.identityconnectors.framework.spi.Configuration)}.
     */
    private mParticleConfiguration configuration;

    /**
     * Gets the Configuration context for this connector.
     *
     * @return The current {@link Configuration}
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Callback method to receive the {@link Configuration}.
     *
     * @param configuration the new {@link Configuration}
     * @see org.identityconnectors.framework.spi.Connector#init(org.identityconnectors.framework.spi.Configuration)
     */
    public void init(final Configuration configuration) {
        this.configuration = (mParticleConfiguration) configuration;
    }

    /**
     * Disposes of the {@link mParticleConnector}'s resources.
     *
     * @see org.identityconnectors.framework.spi.Connector#dispose()
     */
    public void dispose() {
        configuration = null;
    }

    /**
     * {@inheritDoc}
     */
    public void checkAlive() {
        this.configuration.getClient().testConnection();
    }


    /**
     * {@inheritDoc}
     */
    public Uid create(final ObjectClass objectClass, final Set<Attribute> createAttributes,
                      final OperationOptions options) {
        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            return mParticleEvent(createAttributes, REGISTRATION);
        } else {
            logger.warn("Create of type {0} is not supported", configuration.getConnectorMessages()
                                                                            .format(objectClass.getDisplayNameKey(),
                                                                                    objectClass.getObjectClassValue()));
            throw new UnsupportedOperationException(
                    "Create of type" + objectClass.getObjectClassValue() + " is not supported");
        }
    }

    private Uid mParticleEvent(Set<Attribute> createAttributes, String eventName) {
        JSONObject payload = new JSONObject();
        if (configuration.getProductionEnvironment()) {
            payload.put("environment", "production");
        } else {
            payload.put("environment", "development");
        }
        JSONArray eventsArray = new JSONArray();
        JSONObject userIdentities = new JSONObject();
        JSONObject event = new JSONObject().put("event_type", "custom_event");
        JSONObject data = new JSONObject().put("custom_event_type", "other").put("event_name", eventName);
        Name name = AttributeUtil.getNameFromAttributes(createAttributes);
        JSONObject attributes = new JSONObject();
        for (Attribute attribute : createAttributes) {
            if (attribute.getName().equals(Name.NAME)) {
                userIdentities.put("customer_id", attribute.getValue().get(0));
            } else if (attribute.getName().equals("email")) {
                userIdentities.put("email", attribute.getValue().get(0));
            } else {
                attributes.put("forgeRock_" + attribute.getName(), attribute.getValue().get(0));
            }
        }
        payload.put("events", eventsArray.put(event.put("data", data)));
        payload.put("user_attributes", attributes);
        payload.put("user_identities", userIdentities);
        if (name != null) {
            try {
                configuration.getClient().postRequest(configuration.getApiEndpoint(), payload);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new Uid(AttributeUtil.getStringValue(name).toLowerCase(Locale.US));
        } else {
            throw new InvalidAttributeValueException("Name attribute is required");
        }
    }


    /**
     * {@inheritDoc}
     */
    public void test() {
        logger.ok("Test works well");
    }

    /**
     * {@inheritDoc}
     */
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes,
                      OperationOptions options) {
        AttributesAccessor attributesAccessor = new AttributesAccessor(replaceAttributes);
        Name newName = attributesAccessor.getName();
        if (newName != null) {
            logger.info("Rename the object {0}:{1} to {2}", objectClass.getObjectClassValue(), uid
                    .getUidValue(), newName.getNameValue());
        }

        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            return mParticleEvent(replaceAttributes, UPDATE);
        } else {
            logger.warn("Update of type {0} is not supported", configuration.getConnectorMessages()
                                                                            .format(objectClass.getDisplayNameKey(),
                                                                                    objectClass.getObjectClassValue()));
            throw new UnsupportedOperationException("Update of type"
                                                            + objectClass.getObjectClassValue() + " is not supported");
        }
    }

    public Schema schema() {
        final SchemaBuilder builder = new SchemaBuilder(mParticleConnector.class);
        ObjectClassInfoBuilder accountInfoBuilder = new ObjectClassInfoBuilder();
        for (String attribute : configuration.getmParticleAttributes()) {
            accountInfoBuilder.addAttributeInfo(AttributeInfoBuilder.build(attribute));
        }
        builder.defineObjectClass(accountInfoBuilder.build());
        return builder.build();
    }

    @Override
    public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return new FilterTranslator<Filter>() {
            @Override
            public List<Filter> translate(Filter filter) {
                return CollectionUtil.newList(filter);
            }
        };
    }

    @Override
    public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
        if (ObjectClass.ACCOUNT.equals(objectClass)) {
            if (query != null) {
                Uid uid = FrameworkUtil.getUidIfGetOperation(query);
                ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
                cob.setUid(uid.getUidValue());
                cob.setObjectClass(ObjectClass.ACCOUNT);
                handler.handle(cob.build());
            }
        }
    }

}
