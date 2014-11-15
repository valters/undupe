package vingolds.v2014.undupe;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.google.common.collect.Lists;

/**
 * @author Valters Vingolds
 */
public class Undupe {

    public static class Args {
        @Parameter(description = "<list of folders to scan>")
        public final List<String> folders = Lists.newArrayList();

        public int      fileSizeTooSmall = 1000*1000*100; // 100 mb
    }

    private final Args args;

    public Undupe( final Args args ) {
        this.args = args;
    }

    public void run() {
        final List<Path> paths = Lists.newArrayList();

        for( final String folder : args.folders ) {
            final Path path = FileSystems.getDefault().getPath( folder );
            paths.add( path );
        }

        final List<Report> reports = Lists.newArrayList();

        for( final Path path : paths ) {
            final MyVisitor visitor = new MyVisitor( new Report( path.toString() ) );
            try {
                Files.walkFileTree( path, visitor );
                reports.add( visitor.report );
            }
            catch( final IOException e ) {
                e.printStackTrace();
            }
        }

        for( final Report report : reports ) {
            report.print();
            report.printCombined( reports );
        }
    }


    public class MyVisitor extends SimpleFileVisitor<Path> {
        private final Report report;

        public MyVisitor( final Report report ) {
            super();
            this.report = report;
        }

        @Override
        public FileVisitResult visitFile( final Path file, final BasicFileAttributes attr ) throws IOException {
            if (attr.isSymbolicLink()) {
                System.out.format("Symbolic link: %s ", file);
            } else if (attr.isRegularFile()) {
                if( attr.size() > args.fileSizeTooSmall ) {
                    // only add if file big enough: don't care for small files
                    //~System.out.format("Regular file: %s ", file);
                    //~System.out.println("(" + attr.size() + " bytes)");
                    report.addFile( file );
                }
            } else {
                System.out.format("Other: %s ", file);
            }
            return FileVisitResult.CONTINUE;
        }

    }
}
