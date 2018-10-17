
package bio.comp.jlu.asap

import groovy.io.FileType
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


public final class Misc {

    public static final String ZIP_EXTENSION = '.zip'

    public static String formatRuntimes( int runtime ) {

        def times = []

        int hours = runtime / (60 * 60 * 1000);
        times << hours
        runtime -= hours * 60 * 60 * 1000;

        int mins  = runtime / (60 * 1000);
        times << mins
        runtime -= mins * 60 * 1000;

        int secs  = runtime / 1000;
        times << secs

        runtime -= secs * 1000;

        int msecs = runtime;
        times << msecs

        return String.format('%02d',times[0]) + ':' + String.format('%02d',times[1]) + ':' + String.format('%02d',times[2]) + '.' + String.format('%03d',times[3])

    }


    /**
     * Unzips this file. As a precondition, this file has to refer to a *.zip file. If the <tt>destination</tt>
     * directory is not provided, it will fall back to this file's parent directory.
     *
     * @param self
     * @param destination (optional), the destination directory where this file's content will be unzipped to.
     * @return a {@link java.util.Collection} of unzipped {@link java.io.File} objects.
     */
    static Collection<File> unzip( File self, File destination, Closure<Boolean> filter ) {

        if( !self.isFile() )
            throw new IllegalArgumentException( "'${self}' is not a file!" )

        def fileName = self.name
        if( !fileName.toLowerCase().endsWith( ZIP_EXTENSION ) )
            throw new IllegalArgumentException( "File#unzip() has to be called on a *.zip file! (${fileName})" )

        if( destination  &&  !destination.isDirectory() )
            throw new IllegalArgumentException( "'${destination}' has to be a directory!" )


        // if destination directory is not given, we'll fall back to the parent directory of 'self'
        if( destination == null )
            destination = new File( self.parent )

        def unzippedFiles = []

        final zipInput = new ZipInputStream( new FileInputStream( self ) )
        zipInput.withStream{
            def entry
            while( entry = zipInput.nextEntry )  {
                if( !entry.isDirectory() )  {
                    final file = new File( destination, entry.name )
                    if( filter == null  ||  filter( file ) ) {
                        file.parentFile?.mkdirs()
                        def output = new FileOutputStream( file )
                        output.withStream{
                            output << zipInput
                        }
                        unzippedFiles << file
                    }
                }
                else {
                    final dir = new File( destination, entry.name )
                    if( filter == null  ||  filter( dir ) ) {
                        dir.mkdirs()
                        unzippedFiles << dir
                    }
                }
            }
        }

        return unzippedFiles

    }


    public static void exit( def log, String msg, Exception ex ) {

        if( ex )
            log.error( msg, ex )
        else
            log.error( msg )

        println( "Error: ${msg}" )
        System.exit( 1 )

    }

}