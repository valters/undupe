package vingolds.v2014.undupe;

import static java.lang.System.out;

import com.beust.jcommander.JCommander;
import com.google.common.base.Stopwatch;

/**
 * @author Valters Vingolds
 */
public class Main {
    public static void main( final String[] commandLine ) {
        out.println( "hello world" );
        final Stopwatch timer = Stopwatch.createStarted();
        final Undupe.Args args = new Undupe.Args();
        final JCommander cmd = new JCommander( args, commandLine );
        cmd.setProgramName( "Undupe" );
        out.println( "running with: " + args.folders );
        if( args.folders.isEmpty() ) {
            cmd.usage();
        }
        else {
            new Undupe( args ).run();
            out.println( "+ done in " + timer.stop());
        }
    }
}
