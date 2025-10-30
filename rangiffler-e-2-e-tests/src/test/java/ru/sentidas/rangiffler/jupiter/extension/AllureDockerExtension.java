package ru.sentidas.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.model.allure.AllureResults;
import ru.sentidas.rangiffler.model.allure.DecodedAllureFile;
import ru.sentidas.rangiffler.service.impl.AllureDockerApiClient;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

public class AllureDockerExtension implements SuiteExtension {

    private static final Logger LOG = LoggerFactory.getLogger(AllureDockerExtension.class);

    private static final boolean inDocker = "docker".equals(System.getProperty("test.env"));
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Path allureResultsDirectory = Path.of("./rangiffler-e-2-e-tests/build/allure-results");
    private static final String projectId = Config.projectId();

    private static final AllureDockerApiClient allureDockerApiClient = new AllureDockerApiClient();
    private boolean allureBroken = false;

    @Override
    public void beforeSuite(ExtensionContext context) {
        if (inDocker) {
            try {
                allureDockerApiClient.createProjectIfNotExist(projectId);
                allureDockerApiClient.clean(projectId);
            } catch (Throwable e) {
                allureBroken = true;
                // do nothing
            }
        }
    }

    @Override
    public void afterSuite() {
        if (inDocker && !allureBroken) {
            try (Stream<Path> paths = Files.walk(allureResultsDirectory).filter(Files::isRegularFile)) {
                List<DecodedAllureFile> filesToSend = new ArrayList<>();
                for (Path allureResult : paths.toList()) {
                    try (InputStream is = Files.newInputStream(allureResult)) {
                        filesToSend.add(
                                new DecodedAllureFile(
                                        allureResult.getFileName().toString(),
                                        encoder.encodeToString(is.readAllBytes())
                                )
                        );
                    }
                }
                allureDockerApiClient.sendResultsToAllure(
                        projectId,
                        new AllureResults(
                                filesToSend
                        )
                );
                allureDockerApiClient.generateReport(
                        projectId,
                        System.getenv("HEAD_COMMIT_MESSAGE"),
                        System.getenv("BUILD_URL"),
                        System.getenv("EXECUTION_TYPE")
                );
            } catch (Throwable e) {
                // do nothing
            }
        }
    }
}
