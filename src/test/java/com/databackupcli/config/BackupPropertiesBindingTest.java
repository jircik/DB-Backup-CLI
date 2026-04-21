package com.databackupcli.config;

import com.databackupcli.model.DbType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class BackupPropertiesBindingTest {

    @Autowired
    private BackupProperties props;

    @Test
    void binds_test_profile_from_yaml() {
        ProfileProperties pg = props.getProfiles().get("test-pg");
        assertThat(pg).isNotNull();
        assertThat(pg.getDbType()).isEqualTo(DbType.POSTGRES);
        assertThat(pg.getHost()).isEqualTo("localhost");
        assertThat(pg.getPort()).isEqualTo(5432);
        assertThat(pg.getDatabase()).isEqualTo("testdb");
        assertThat(pg.getUsername()).isEqualTo("testuser");
    }

    @Test
    void binds_local_storage_path() {
        assertThat(props.getStorage().getLocal().getPath()).isNotBlank();
    }

    @Test
    void slack_webhook_defaults_to_blank() {
        assertThat(props.getNotifications().getSlack().getWebhookUrl()).isBlank();
    }
}
