package com.databackupcli.batch.tasklets;

import com.databackupcli.notification.SlackNotifier;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class NotifyTasklet implements Tasklet {

    private final SlackNotifier slackNotifier;

    public NotifyTasklet(SlackNotifier slackNotifier) {
        this.slackNotifier = slackNotifier;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        String outputKey = jobExecution.getExecutionContext().getString("outputKey", "unknown");
        String status = jobExecution.getExitStatus().getExitCode();

        slackNotifier.notify(status, outputKey);

        return RepeatStatus.FINISHED;
    }
}
