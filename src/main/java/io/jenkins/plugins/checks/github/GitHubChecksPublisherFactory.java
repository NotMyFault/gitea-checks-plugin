package io.jenkins.plugins.checks.github;

import java.util.Optional;

import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

import edu.hm.hafner.util.VisibleForTesting;
import io.jenkins.plugins.checks.api.ChecksPublisher;
import io.jenkins.plugins.checks.api.ChecksPublisherFactory;
import io.jenkins.plugins.util.PluginLogger;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * An factory which produces {@link GitHubChecksPublisher}.
 */
@Extension
public class GitHubChecksPublisherFactory extends ChecksPublisherFactory {
    private final GitHubSCMFacade scmFacade;

    /**
     * Creates a new instance of {@link GitHubChecksPublisherFactory}.
     */
    public GitHubChecksPublisherFactory() {
        this(new GitHubSCMFacade());
    }

    @VisibleForTesting
    GitHubChecksPublisherFactory(final GitHubSCMFacade scmFacade) {
        super();

        this.scmFacade = scmFacade;
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Run<?, ?> run, final TaskListener listener) {
        return createPublisher(run, DisplayURLProvider.get().getRunURL(run), listener);
    }

    @VisibleForTesting
    Optional<ChecksPublisher> createPublisher(final Run<?, ?> run, final String runURL, final TaskListener listener) {
        PluginLogger logger = createLogger(getListener(listener));
        
        GitSCMChecksContext gitSCMContext = new GitSCMChecksContext(run, runURL);
        if (gitSCMContext.isValid(logger)) {
            return Optional.of(new GitHubChecksPublisher(gitSCMContext, getListener(listener)));
        }

        return createPublisher(listener, logger, new GitHubSCMSourceChecksContext(run, runURL, scmFacade));
    }

    @Override
    protected Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final TaskListener listener) {
        return createPublisher(job, DisplayURLProvider.get().getJobURL(job), listener);
    }

    @VisibleForTesting
    Optional<ChecksPublisher> createPublisher(final Job<?, ?> job, final String jobURL, final TaskListener listener) {
        PluginLogger logger = createLogger(getListener(listener));

        return createPublisher(listener, logger, new GitHubSCMSourceChecksContext(job, jobURL, scmFacade));
    }

    private Optional<ChecksPublisher> createPublisher(final TaskListener listener, final PluginLogger logger, 
            final GitHubChecksContext gitHubSCMSourceContext) {
        if (gitHubSCMSourceContext.isValid(logger)) {
            return Optional.of(new GitHubChecksPublisher(gitHubSCMSourceContext, getListener(listener)));
        }
        return Optional.empty();
    }

    
    private TaskListener getListener(final TaskListener taskListener) {
        // FIXME: checks-API should use a Null listener
        if (taskListener == null) {
            return TaskListener.NULL;
        }
        return taskListener;
    }

    private PluginLogger createLogger(final TaskListener listener) {
        return new PluginLogger(listener.getLogger(), "GitHub Checks");
    }
}
