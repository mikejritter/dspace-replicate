package org.dspace.pack.bagit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dspace.TestContentServiceFactory.CONTENT_SERVICE_FACTORY;
import static org.dspace.TestDSpaceServicesFactory.DSPACE_SERVICES_FACTORY;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.dspace.TestConfigurationService;
import org.dspace.TestContentServiceFactory;
import org.dspace.TestDSpaceKernelImpl;
import org.dspace.TestDSpaceServicesFactory;
import org.dspace.TestServiceManager;
import org.dspace.kernel.DSpaceKernel;
import org.dspace.kernel.DSpaceKernelManager;
import org.dspace.kernel.ServiceManager;
import org.dspace.services.ConfigurationService;
import org.junit.Before;
import org.junit.Test;

public class BagInfoHelperTest {

    private static final String ARCH_FMT = "zip";
    private static final String ARCH_FMT_KEY = "replicate.packer.archfmt";
    private static final String SOURCE_ORG = "org.dspace.dspace-replicate";
    private static final String SOURCE_ORG_KEY = "replicate-bagit.tag.bag-info.source-organization";
    private static final String OTHER_INFO_MISC = "bag-info-helper-test";
    private static final String OTHER_INFO_MISC_KEY = "replicate-bagit.tag.other-info.misc";

    private static final String BAG_INFO = "bag-info.txt";
    private static final String OTHER_INFO = "other-info.txt";

    private ConfigurationService configurationService;

    @Before
    public void setup() {
        final ServiceManager serviceManager = new TestServiceManager();
        configurationService = new TestConfigurationService();
        configurationService.setProperty(ARCH_FMT_KEY, ARCH_FMT);
        configurationService.setProperty(SOURCE_ORG_KEY, SOURCE_ORG);
        configurationService.setProperty(OTHER_INFO_MISC_KEY, OTHER_INFO_MISC);

        serviceManager.registerService(ConfigurationService.class.getName(), configurationService);
        serviceManager.registerService(DSPACE_SERVICES_FACTORY, new TestDSpaceServicesFactory());
        serviceManager.registerService(CONTENT_SERVICE_FACTORY, new TestContentServiceFactory());

        final DSpaceKernel kernel = new TestDSpaceKernelImpl(serviceManager, configurationService);
        DSpaceKernelManager.registerMBean(kernel.getMBeanName(), kernel);
        DSpaceKernelManager.setDefaultKernel(kernel);
    }

    @Test
    public void getTagFiles() {
        final BagInfoHelper bagInfo = new BagInfoHelper();
        final Map<String, Map<String, String>> tagFiles = bagInfo.getTagFiles();

        assertThat(tagFiles).hasSize(2);
        assertThat(tagFiles).containsOnlyKeys(BAG_INFO, OTHER_INFO);
        assertThat(tagFiles).containsEntry(BAG_INFO, ImmutableMap.of("Source-Organization", SOURCE_ORG))
                            .containsEntry(OTHER_INFO, ImmutableMap.of("Misc", OTHER_INFO_MISC));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidKeyLength() {
        final String key = "replicate-bagit.tag.test.test-info.invalid-key";

        configurationService.setProperty(key, "");

        final BagInfoHelper bagInfo = new BagInfoHelper();
        bagInfo.getTagFiles();
    }
}