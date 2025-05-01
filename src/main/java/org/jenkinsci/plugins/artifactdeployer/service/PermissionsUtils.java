package org.jenkinsci.plugins.artifactdeployer.service;

import java.io.File;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import jnr.posix.util.DefaultPOSIXHandler;

/**
 * Provides convenience methods for manipulating file permissions, as best as is achievable with the Java 6 API.
 */
public class PermissionsUtils
{
    private static POSIX posix = POSIXFactory.getPOSIX(new DefaultPOSIXHandler(), true);

    public static int getPermissions(File file)
    {
        return posix.stat(file.getAbsolutePath()).mode() & 0777;
    }

    public static void setPermissions(File file, int perms)
    {
        posix.chmod(file.getAbsolutePath(), perms);
    }
}
