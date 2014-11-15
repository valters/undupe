package vingolds.v2014.undupe;

import static java.lang.System.out;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author Valters Vingolds
 */
public class Report {

    public static class StringLengthComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 1L;

        public static final StringLengthComparator INSTANCE = new StringLengthComparator();

        public int compare( final String s1, final String s2 ) {
            return s1.length() - s2.length();
        }
    }

    final String parentFolder;

    final Map<String, List<String>> dupesDetected = Maps.newHashMap();

    final Map<String, List<String>> dupePaths = Maps.newHashMap();

    public Report( final String parentFolder ) {
        this.parentFolder = parentFolder;
    }

    public void addFile( final Path file ) {
        final String name = file.getName( file.getNameCount()-1 ).toString();

        final String path = file.toString();

        final List<String> dupes = dupeListFor( name );
        dupes.add( path );
        if( dupes.size() == 2 ) {
            out.print( "." );
            if( 0 == dupesDetected.size() % 80 ) { out.println(); }
        }
    }

    private List<String> dupeListFor( final String fileName ) {
        final List<String> list = dupesDetected.get( fileName );
        if( list != null ) {
            return list;
        }

        dupesDetected.put( fileName, Lists.<String>newArrayList() );
        return dupeListFor( fileName );
    }

    public void print() {
        out.println( "===== report for: ["+parentFolder+"] scanned "+dupesDetected.size()+" files ====" );

        for( final Map.Entry<String, List<String>> entry : dupesDetected.entrySet() ) {
            if( entry.getValue().size() > 1 ) {
                final List<String> list = shortestFirst( entry.getValue() );

                dupePaths.put( list.get( 0 ), ImmutableList.copyOf( list.subList( 1, list.size() ) ) );
            }
        }


        final List<String> keys = Lists.newArrayList( dupePaths.keySet() );
        Collections.sort( keys );
        for( final String key : keys ) {

            out.println( "# dupes of "+key );
            for( final String file : dupePaths.get( key ) ) {
                out.println( asDeleteCommand( file ) );
            }
        }
    }

    private String asDeleteCommand( final String file ) {
        return "rm --verbose -- \"" + file + "\"";
    }

    private List<String> shortestFirst( final List<String> list ) {
        Collections.sort( list, StringLengthComparator.INSTANCE );
        return list;
    }

    public void printCombined( final List<Report> reports ) {
        out.println( "===== combined report for: ["+parentFolder+"] =====" );
        for( final Report otherReport : reports ) {
            if( otherReport == this ) {
                continue; // don't compare to self
            }

            for( final Map.Entry<String, List<String>> entry : dupesDetected.entrySet() ) {
                final String name = entry.getKey();
                if( otherReport.dupesDetected.containsKey( name ) ) {
                    for( final String file : entry.getValue() ) {
                        out.println( asDeleteCommand( file ) );
                    }
                    out.println( "# other -> "+otherReport.dupesDetected.get( name ) );
                }
            }
        }
    }
}
