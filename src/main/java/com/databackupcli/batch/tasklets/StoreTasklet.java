package com.databackupcli.batch.tasklets;

import com.databackupcli.storage.StorageProviderRegistry;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@StepScope
public class StoreTasklet implements Tasklet {

    private final StorageProviderRegistry storageRegistry;

    @Value("#{jobParameters['storage']}")   private String storage;
    @Value("#{jobParameters['database']}") private String database;

    public StoreTasklet(StorageProviderRegistry storageRegistry) {
        this.storageRegistry = storageRegistry;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        var executionContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        String compressedPath = executionContext.getString("compressedFilePath");
        if (compressedPath == null) {
            throw new IllegalStateException(
                "compressedFilePath missing from ExecutionContext — CompressTasklet must run before StoreTasklet");
        }

        Path compressedFile = Path.of(compressedPath);
        String key = database + "/" + compressedFile.getFileName().toString();

        storageRegistry.get(storage).store(compressedFile, key);

        executionContext.putString("outputKey", key);

        return RepeatStatus.FINISHED;
    }
}
