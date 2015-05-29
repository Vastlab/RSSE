/*
 * This class is a Makefile-generate-able class containing static defaults for the rest of
 * the program.
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class Defaults
{
    public static final String dbDir="/var/rsse/ess/db";
    public static final String configDir="/etc/rsse";
    public static final String tmpDir="/var/rsse/ess/tmp";
    public static final String respDir="/var/rsse/ess/resp";
    
    //Define some constants for the ConfigurationManager:
    public static final int nuggetServerPort=9002;
    public static final int responseServerPort=9003;
}
