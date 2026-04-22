package com.databackupcli.notification;

import com.databackupcli.config.BackupProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SlackNotifier {

    private static final Logger log = LoggerFactory.getLogger(SlackNotifier.class);
    private final String webhookUrl;

    public SlackNotifier(BackupProperties properties) {
        this.webhookUrl = properties.getNotifications().getSlack().getWebhookUrl();
    }

    public void notify(String status, String outputKey) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        // Phase 2: POST JSON payload to webhookUrl via java.net.http.HttpClient
        log.info("Slack notification pending implementation — status={} key={}", status, outputKey);
    }
}
