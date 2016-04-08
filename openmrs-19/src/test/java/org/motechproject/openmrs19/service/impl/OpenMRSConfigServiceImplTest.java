package org.motechproject.openmrs19.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.config.SettingsFacade;
import org.motechproject.openmrs19.config.Config;
import org.motechproject.openmrs19.exception.config.ConfigurationAlreadyExistsException;
import org.motechproject.openmrs19.exception.config.ConfigurationNotFoundException;
import org.motechproject.openmrs19.config.ConfigDummyData;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class OpenMRSConfigServiceImplTest {

    private static final String OPEN_MRS_CONFIGS_FILE_NAME = "openmrs-configs.json";
    private static final String EMPTY_CONFIGS = "json/empty-configs.json";
    private static final String ONE_CONFIG = "json/one-config.json";
    private static final String THREE_CONFIG = "json/three-configs.json";

    private static final String SUFFIX_ONE = "fooOne";
    private static final String SUFFIX_TWO = "fooTwo";
    private static final String SUFFIX_THREE = "fooThree";
    private static final String NON_EXISTENT = "nonExistent";

    @Mock
    private SettingsFacade settingsFacade;

    @InjectMocks
    private OpenMRSConfigServiceImpl configService;

    @Before
    public void setUp() throws Exception {
        configService = new OpenMRSConfigServiceImpl();
        initMocks(this);
    }

    @Test
    public void shouldAddValidConfig() throws Exception {
        loadConfig(EMPTY_CONFIGS);

        Config config = ConfigDummyData.prepareConfig(SUFFIX_ONE);

        configService.addConfig(config);

        verify(settingsFacade, times(1)).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

        assertThat(configService.getConfigs().size(), is(1));
        assertThat(configService.getConfigs(), hasItem(config));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfConfigIsInvalidDuringAdd() throws Exception {
        try {
            loadConfig(EMPTY_CONFIGS);

            Config config = ConfigDummyData.prepareConfig(SUFFIX_ONE);
            config.setName(null);

            configService.addConfig(config);
        } finally {
            verify(settingsFacade, never()).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

            assertThat(configService.getConfigs().size(), is(0));
        }
    }

    @Test(expected = ConfigurationAlreadyExistsException.class)
    public void shouldThrowConfigurationAlreadyExistsIfAddingDuplicate() throws Exception {
        try {
            loadConfig(ONE_CONFIG);

            Config config = ConfigDummyData.prepareConfig(SUFFIX_ONE);

            configService.addConfig(config);
        } finally {
            verify(settingsFacade, never()).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

            assertThat(configService.getConfigs().size(), is(1));
            assertThat(configService.getConfigs(), hasItem(ConfigDummyData.prepareConfig(SUFFIX_ONE)));
        }
    }

    @Test
    public void shouldUpdateConfigurationIfItExists() throws Exception {
        loadConfig(ONE_CONFIG);
        
        Config config = ConfigDummyData.prepareConfig(SUFFIX_ONE);
        config.setOpenMrsUrl("foo.openmrs.url");
        config.setUsername("fooUsername");
        config.setPassword("fooPassword");
        config.setMotechId("fooMotechId");

        configService.updateConfig(config);

        verify(settingsFacade).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

        assertThat(configService.getConfigs().size(), is(1));
        assertThat(configService.getConfigs(), hasItem(config));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfConfigIsInvalidDuringUpdate() throws Exception {
        try {
            loadConfig(ONE_CONFIG);

            Config config = ConfigDummyData.prepareConfig(SUFFIX_ONE);
            config.setName(null);

            configService.updateConfig(config);
        } finally {
            verify(settingsFacade, never()).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

            assertThat(configService.getConfigs().size(), is(1));
            assertThat(configService.getConfigs(), hasItem(ConfigDummyData.prepareConfig(SUFFIX_ONE)));
        }
    }

    @Test(expected = ConfigurationNotFoundException.class)
    public void shouldThrowConfigurationNotFoundExceptionIfThereIsNoConfigurationWithTheSameName() throws Exception {
        try {
            loadConfig(EMPTY_CONFIGS);

            Config config = ConfigDummyData.prepareConfig(SUFFIX_ONE);

            configService.updateConfig(config);
        } finally {
            verify(settingsFacade, never()).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

            assertThat(configService.getConfigs().size(), is(0));
        }
    }

    @Test
    public void shouldDeleteConfigurationIfItExists() throws Exception {
        loadConfig(ONE_CONFIG);

        configService.deleteConfig(ConfigDummyData.getName(SUFFIX_ONE));

        verify(settingsFacade, times(1)).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

        assertThat(configService.getConfigs().size(), is(0));
    }

    @Test(expected = ConfigurationNotFoundException.class)
    public void shouldThrowConfigurationNotFoundExceptionIfTryingToDeleteNonexistentConfiguration() throws Exception {
        try {
            loadConfig(ONE_CONFIG);

            configService.deleteConfig(ConfigDummyData.getName(NON_EXISTENT));
        } finally {
            verify(settingsFacade, never()).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

            assertThat(configService.getConfigs().size(), is(1));
            assertThat(configService.getConfigs(), hasItem(ConfigDummyData.prepareConfig(SUFFIX_ONE)));
        }
    }

    @Test
    public void shouldMarkConfigurationAsDefault() throws Exception {
        loadConfig(THREE_CONFIG);

        configService.markConfigAsDefault(ConfigDummyData.getName(SUFFIX_TWO));

        verify(settingsFacade, times(1)).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

        Config expectedDefaultConfig = ConfigDummyData.prepareConfig(SUFFIX_TWO);

        assertThat(configService.getDefaultConfig(), equalTo(expectedDefaultConfig));
        assertThat(configService.getConfigs().size(), is(3));
        assertThat(configService.getConfigs(), hasItems(ConfigDummyData.prepareConfig(SUFFIX_ONE),
                expectedDefaultConfig, ConfigDummyData.prepareConfig(SUFFIX_THREE)));
    }

    @Test(expected = ConfigurationNotFoundException.class)
    public void shouldThrowConfigurationNotFoundExceptionIfTryingToMarkNonexistentConfigurationAsDefault()
            throws Exception {
        try {
            loadConfig(THREE_CONFIG);

            configService.markConfigAsDefault(ConfigDummyData.getName(NON_EXISTENT));
        } finally {
            verify(settingsFacade, never()).saveRawConfig(eq(OPEN_MRS_CONFIGS_FILE_NAME), Matchers.any(Resource.class));

            Config expectedDefaultConfig = ConfigDummyData.prepareConfig(SUFFIX_ONE);

            assertThat(configService.getDefaultConfig(), equalTo(expectedDefaultConfig));
            assertThat(configService.getConfigs().size(), is(3));
            assertThat(configService.getConfigs(), hasItems(expectedDefaultConfig,
                    ConfigDummyData.prepareConfig(SUFFIX_TWO), ConfigDummyData.prepareConfig(SUFFIX_THREE)));
        }
    }

    @Test
    public void shouldGetConfigs() throws Exception {
        loadConfig(THREE_CONFIG);

        List<Config> configs = configService.getConfigs();

        assertThat(configs.size(), is(3));
        assertThat(configs, hasItems(ConfigDummyData.prepareConfig(SUFFIX_ONE), ConfigDummyData.prepareConfig(SUFFIX_TWO),
                ConfigDummyData.prepareConfig(SUFFIX_THREE)));
    }

    @Test
    public void shouldGetConfigByName() throws Exception {
        loadConfig(THREE_CONFIG);

        assertThat(configService.getConfigByName(ConfigDummyData.getName(SUFFIX_TWO)),
                equalTo(ConfigDummyData.prepareConfig(SUFFIX_TWO)));
    }

    @Test
    public void shouldGetDefaultConfig() throws Exception {
        loadConfig(THREE_CONFIG);

        assertThat(configService.getDefaultConfig(), equalTo(ConfigDummyData.prepareConfig(SUFFIX_ONE)));
    }

    private void loadConfig(String fileName) throws Exception {
        try(InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            when(settingsFacade.getRawConfig(OPEN_MRS_CONFIGS_FILE_NAME)).thenReturn(is);
            configService.postConstruct();
        }
    }
}