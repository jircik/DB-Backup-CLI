package com.databackupcli.batch;

import com.databackupcli.batch.tasklets.CompressTasklet;
import com.databackupcli.batch.tasklets.DumpTasklet;
import com.databackupcli.batch.tasklets.NotifyTasklet;
import com.databackupcli.batch.tasklets.StoreTasklet;
import com.databackupcli.batch.tasklets.ValidateConnectionTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BackupJobConfig {

    // -----------------------------------------------------------------------
    // Steps
    // -----------------------------------------------------------------------

    @Bean
    public Step validateConnectionStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager,
                                       ValidateConnectionTasklet tasklet) {
        return new StepBuilder("validateConnectionStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step dumpStep(JobRepository jobRepository,
                         PlatformTransactionManager transactionManager,
                         DumpTasklet tasklet) {
        return new StepBuilder("dumpStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step compressStep(JobRepository jobRepository,
                             PlatformTransactionManager transactionManager,
                             CompressTasklet tasklet) {
        return new StepBuilder("compressStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step storeStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          StoreTasklet tasklet) {
        return new StepBuilder("storeStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step notifyStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           NotifyTasklet tasklet) {
        return new StepBuilder("notifyStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // -----------------------------------------------------------------------
    // Job
    // -----------------------------------------------------------------------

    @Bean
    public Job backupJob(JobRepository jobRepository,
                         BackupJobListener listener,
                         Step validateConnectionStep,
                         Step dumpStep,
                         Step compressStep,
                         Step storeStep,
                         Step notifyStep) {
        return new JobBuilder("backupJob", jobRepository)
                .listener(listener)
                .start(validateConnectionStep)
                .next(dumpStep)
                .next(compressStep)
                .next(storeStep)
                .next(notifyStep)
                .build();
    }
}
