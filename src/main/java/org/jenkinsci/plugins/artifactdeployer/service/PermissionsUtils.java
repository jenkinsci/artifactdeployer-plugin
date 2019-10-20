package org.jenkinsci.plugins.artifactdeployer.service;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.jruby.ext.posix.POSIX;
import org.jruby.ext.posix.POSIX.ERRORS;
import org.jruby.ext.posix.POSIXFactory;
import org.jruby.ext.posix.POSIXHandler;

/**
 * Provides convenience methods for manipulating file permissions, as best as is achievable with the Java 6 API.
 */
public class PermissionsUtils
{
    private static POSIX posix = POSIXFactory.getPOSIX(new PermissionsUtils.AntPOSIXHandler(), true);

    public static int getPermissions(File file)
    {
        return posix.stat(file.getAbsolutePath()).mode() & 0777;
    }

    public static void setPermissions(File file, int perms)
    {
        posix.chmod(file.getAbsolutePath(), perms);
    }

    /**
     * Minimal POSIX handler for Ant tasks. There's scope for improvement here, like redirecting warnings through the Ant
     * log, failing on errors, etc...
     */
    public static class AntPOSIXHandler implements POSIXHandler
    {

        public File getCurrentWorkingDirectory()
        {
            return new File(".");
        }

        public String[] getEnv()
        {
            return new String[]{};
        }

        public PrintStream getErrorStream()
        {
            return System.err;
        }

        public InputStream getInputStream()
        {
            return System.in;
        }

        public PrintStream getOutputStream()
        {
            return System.out;
        }

        public int getPID()
        {
            return 0;
        }

        public boolean isVerbose()
        {
            return false;
        }

        public void unimplementedError(String message)
        {
            throw new RuntimeException(message);
        }

        public void warn(WARNING_ID arg0, String arg1, Object... arg2)
        {
            System.err.println(arg0 + ": " + String.format(arg1, arg2));
        }


        public void error(ERRORS error, String extraData)
        {
            System.err.println(error + ": " + extraData);
        }

    }

}
