package com.databackupcli.batch.tasklets;

import com.databackupcli.util.CompressionUtil;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@StepScope
public class CompressTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        var executionContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        String dumpFilePath = executionContext.getString("dumpFilePath");
        if (dumpFilePath == null) {
            throw new IllegalStateException(
                "dumpFilePath missing from ExecutionContext — DumpTasklet must run before CompressTasklet");
        }

        Path compressed = CompressionUtil.gzip(Path.of(dumpFilePath));

        executionContext.putString("compressedFilePath", compressed.toString());

        return RepeatStatus.FINISHED;
    }
}
