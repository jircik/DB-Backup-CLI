package com.databackupcli.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "dbbackup")
public class BackupProperties {

    private Map<String, ProfileProperties> profiles = new HashMap<>();
    private Storage storage = new Storage();
    private Notifications notifications = new Notifications();

    public Map<String, ProfileProperties> getProfiles() { return profiles; }
    public void setProfiles(Map<String, ProfileProperties> profiles) { this.profiles = profiles; }

    public Storage getStorage() { return storage; }
    public void setStorage(Storage storage) { this.storage = storage; }

    public Notifications getNotifications() { return notifications; }
    public void setNotifications(Notifications notifications) { this.notifications = notifications; }

    public static class Storage {
        private Local local = new Local();
        private S3 s3 = new S3();

        public Local getLocal() { return local; }
        public void setLocal(Local local) { this.local = local; }

        public S3 getS3() { return s3; }
        public void setS3(S3 s3) { this.s3 = s3; }

        public static class Local {
            private String path = System.getProperty("user.home") + "/.dbbackup/backups";

            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
        }

        public static class S3 {
            private String bucket;
            private String region = "us-east-1";

            public String getBucket() { return bucket; }
            public void setBucket(String bucket) { this.bucket = bucket; }

            public String getRegion() { return region; }
            public void setRegion(String region) { this.region = region; }
        }
    }

    public static class Notifications {
        private Slack slack = new Slack();

        public Slack getSlack() { return slack; }
        public void setSlack(Slack slack) { this.slack = slack; }

        public static class Slack {
            private String webhookUrl = "";

            public String getWebhookUrl() { return webhookUrl; }
            public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        }
    }
}
