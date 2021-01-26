/*
 * Copyright 2016-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

package org.forgerock.openicf.connectors.mParticle;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.impl.api.local.LocalConnectorFacadeImpl;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.test.common.PropertyBag;
import org.identityconnectors.test.common.TestHelpers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Attempts to test the {@link mParticleConnector} with the framework.
 *
 */
public class mParticleConnectorTests {

    /**
    * Setup logging for the {@link mParticleConnectorTests}.
    */
    private static final Log logger = Log.getLog(mParticleConnectorTests.class);

    private ConnectorFacade connectorFacade = null;

    /*
    * Example test properties.
    * See the Javadoc of the TestHelpers class for the location of the public and private configuration files.
    */
    private static final PropertyBag PROPERTIES = TestHelpers.getProperties(mParticleConnector.class);

    @BeforeClass
    public void setUp() {
        //
        //other setup work to do before running tests
        //

        //Configuration config = new mParticleConfiguration();
        //Map<String, ? extends Object> configData = (Map<String, ? extends Object>) PROPERTIES.getProperty("configuration",Map.class)
        //TestHelpers.fillConfiguration(
    }

    @AfterClass
    public void tearDown() {
        //
        // clean up resources
        //
        if (connectorFacade instanceof LocalConnectorFacadeImpl) {
            ((LocalConnectorFacadeImpl) connectorFacade).dispose();
        }
    }

    @Test
    public void exampleTest1() {
        logger.info("Running Test 1...");
        //You can use TestHelpers to do some of the boilerplate work in running a search
        //TestHelpers.search(theConnector, ObjectClass.ACCOUNT, filter, handler, null);
    }

    @Test
    public void exampleTest2() {
        logger.info("Running Test 2...");
        //Another example using TestHelpers
        //List<ConnectorObject> results = TestHelpers.searchToList(theConnector, ObjectClass.GROUP, filter);
    }


    @Test
    public void createTest() {
        logger.info("Running Create Test");
//        final ConnectorFacade facade = getFacade(mParticleConnector.class, null);
//        final OperationOptionsBuilder builder = new OperationOptionsBuilder();
//        Set<Attribute> createAttributes = new HashSet<Attribute>();
//        createAttributes.add(new Name("Foo"));
//        createAttributes.add(AttributeBuilder.buildPassword("Password".toCharArray()));
//        createAttributes.add(AttributeBuilder.buildEnabled(true));
//        Uid uid = facade.create(ObjectClass.ACCOUNT, createAttributes, builder.build());
//        Assert.assertEquals(uid.getUidValue(), "foo");
    }








    @Test
    public void testTest() {
        logger.info("Running Test Test");
//        final ConnectorFacade facade = getFacade(mParticleConnector.class, null);
//        facade.test();
    }

    @Test
    public void validateTest() {
        logger.info("Running Validate Test");
//        final ConnectorFacade facade = getFacade(mParticleConnector.class, null);
//        facade.validate();
    }

    @Test
    public void updateTest() {
        logger.info("Running Update Test");
//        final ConnectorFacade facade = getFacade(mParticleConnector.class, null);
//        final OperationOptionsBuilder builder = new OperationOptionsBuilder();
//        Set<Attribute> updateAttributes = new HashSet<Attribute>();
//        updateAttributes.add(new Name("Foo"));
//
//        Uid uid = facade.update(ObjectClass.ACCOUNT, new Uid("Foo"), updateAttributes, builder.build());
//        Assert.assertEquals(uid.getUidValue(), "foo");
    }


    protected ConnectorFacade getFacade(mParticleConfiguration config) {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        // **test only**
        APIConfiguration impl = TestHelpers.createTestConfiguration(mParticleConnector.class, config);
        return factory.newInstance(impl);
    }

    protected ConnectorFacade getFacade(Class<? extends Connector> clazz, String environment) {
        if (null == connectorFacade) {
            synchronized (this) {
                if (null == connectorFacade) {
                    connectorFacade = createConnectorFacade(clazz, environment);
                }
            }
        }
        return connectorFacade;
    }

    public ConnectorFacade createConnectorFacade(Class<? extends Connector> clazz,
        String environment) {
        PropertyBag propertyBag = TestHelpers.getProperties(clazz, environment);

        APIConfiguration impl =
            TestHelpers.createTestConfiguration(clazz, propertyBag, "configuration");
        impl.setProducerBufferSize(0);
        impl.getResultsHandlerConfiguration().setEnableAttributesToGetSearchResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableCaseInsensitiveFilter(false);
        impl.getResultsHandlerConfiguration().setEnableFilteredResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableNormalizingResultsHandler(false);

        //impl.setTimeout(CreateApiOp.class, 25000);
        //impl.setTimeout(UpdateApiOp.class, 25000);
        //impl.setTimeout(DeleteApiOp.class, 25000);

        return ConnectorFacadeFactory.getInstance().newInstance(impl);
    }
}
