package io.jshift.kit.common.util;

import java.io.File;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 * @author roland
 * @since 28/07/16
 */
public class GitUtil {


    public static Repository getGitRepository(File currentDir) throws IOException {

        if (currentDir == null) {
            // TODO: Why is this check needed ?
            currentDir = new File(System.getProperty("basedir", "."));
        }
        File gitFolder = findGitFolder(currentDir);
        if (gitFolder == null) {
            // No git repository found
            return null;
        }
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder
                .readEnvironment()
                .setGitDir(gitFolder)
                .build();
        return repository;
    }

    public static File findGitFolder(File basedir) {
        File gitDir = new File(basedir, ".git");
        if (gitDir.exists() && gitDir.isDirectory()) {
            return gitDir;
        }
        File parent = basedir.getParentFile();
        if (parent != null) {
            return findGitFolder(parent);
        }
        return null;
    }

    public static String getGitCommitId(Repository repository) throws GitAPIException {
        try {
            if (repository != null) {
                Iterable<RevCommit> logs = new Git(repository).log().call();
                for (RevCommit rev : logs) {
                    return rev.getName();
                }
            }
        } finally {

        }
        return null;
    }
}
