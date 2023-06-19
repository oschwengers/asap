
package bio.comp.jlu.asap


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.*
import groovy.json.JsonOutput
import org.slf4j.Logger
import bio.comp.jlu.asap.api.FileType

import static java.nio.file.StandardCopyOption.*
import static bio.comp.jlu.asap.api.AnalysesSteps.*
import static bio.comp.jlu.asap.ASAPConstants.*


/**
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
class ConfigWriter {


    public static void writeConfig( def config ) {

        // get config.json path
        Path projectPath = Paths.get( config.project.path )
        Path configPath  = projectPath.resolve( 'config.json' )

        def json = JsonOutput.toJson( config )
        File tmpConfigFile = projectPath.resolve( 'tmp.json' ).toFile()
        tmpConfigFile << JsonOutput.prettyPrint( json )
        Files.move( tmpConfigFile.toPath(), configPath, REPLACE_EXISTING, ATOMIC_MOVE  )

    }


    public static void convertConfig( Logger log, Path projectPath ) throws Exception {

        // test if JSON config file already exists. Skip upon existing JSON config file.
        Path jsonPath = projectPath.resolve( 'config.json' )
        if( Files.exists( jsonPath ) ) {
            log.info( 'JSON config file already exists! Skip config file conversion.' )
            return
        }


        Path spreadsheetFilePath = null
        [
            'config.xls'//, 'config.xlsx', 'config.ods'
        ].each( {
            Path tmpConfigFile = projectPath.resolve( it )
            if( Files.exists( tmpConfigFile ) )
                spreadsheetFilePath = tmpConfigFile
        } )

        if( spreadsheetFilePath == null ) // test for valid spreadsheet config file
            Misc.exit( log, "Project directory (${projectPath}) does not contain a valid spreadsheet configuration file (config.xls)!", null )

        final TableBookAdapter tba = new ExcelTableBookAdapter()
        if( !tba.acceptFile( spreadsheetFilePath.toFile() ) )
            Misc.exit( log, "Configuration file has a wrong file suffix (${spreadsheetFilePath})! Please, provide a valid Excel configuration file using the Excel '97 format (.xls).", null )

        final TableBook tableBook = tba.importTableBook( spreadsheetFilePath.toFile() )
        if( tableBook.getNoTables() < 2 )
            Misc.exit( log, "Configuration file has a wrong number of tables (${tableBook.getNoTables()})! There must be 2 tables. Please, use the provided Excel template.", null )

        final def config = [:]
        final Table projectTable = tableBook.getTable( 0 )
        final Table genomeTable  = tableBook.getTable( 1 )

        // runtime
        config.runtime = [
            version:     ASAP_VERSION
        ]

        // project
        config.project = [
            name:        projectTable.getCellContent( ConfigTemplate.ROW_ID_PROJECT_NAME, 1 ),
            description: projectTable.getCellContent( ConfigTemplate.ROW_ID_PROJECT_DESCRIPTION, 1 ),
            genus:       projectTable.getCellContent( ConfigTemplate.ROW_ID_PROJECT_GENUS, 1 ),
            version:     ASAP_VERSION
        ]

        // user
        config.user = [
            name:    projectTable.getCellContent( ConfigTemplate.ROW_ID_USER_NAME, 1 ),
            surname: projectTable.getCellContent( ConfigTemplate.ROW_ID_USER_SURNAME, 1 ),
            email:   projectTable.getCellContent( ConfigTemplate.ROW_ID_USER_EMAIL, 1 )
        ]


        // dates
        Instant i = Files.getLastModifiedTime( spreadsheetFilePath ).toInstant()
        config.dates = [
            config: OffsetDateTime.ofInstant( i, ZoneId.systemDefault() ).toString()
        ]


        // filters
        config.filters = [ // TODO: tmp solution until a better solution is implemented
            'phiX'
        ]


        // references
        config.references = []
        int refRowIdx = ConfigTemplate.ROW_ID_REFERENCES
        String ref = projectTable.getCellContent( refRowIdx, 1 )
        while( ref != null  &&  !ref.isEmpty() ) {
            config.references << ref
            refRowIdx++
            if( refRowIdx < projectTable.getNoRows() ) ref = projectTable.getCellContent( refRowIdx, 1 )
            else break
        }


        // genomes
        config.genomes = []
        int rowIdx = 1
        String strain = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_STRAIN )
        while( strain != null  &&  !strain.isEmpty() ) {

            def genome = [
                id: rowIdx,
                species: genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_SPECIES ),
                strain: strain,
                data: []
            ]
            def datum = [
                type: genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_INPUT ),
                files: []
            ]

            FileType ft = FileType.getEnum( datum.type )
            if( ft == null ) {
                datum.type = null
                genome.data << datum
            } else if( ft == FileType.READS_NANOPORE_PAIRED_END ) {
                datum.type = FileType.READS_NANOPORE.toString()
                String file1 = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_FILE_1 );
                if( file1  &&  !file1.isEmpty() )
                    datum.files << file1
                genome.data << datum
                datum = [
                    type: FileType.READS_ILLUMINA_PAIRED_END.toString(),
                    files: []
                ]
                String file2 = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_FILE_2 );
                String file3 = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_FILE_3 );
                if( file2  &&  !file2.isEmpty()  &&  file3  &&  !file3.isEmpty() ) {
                    datum.files << file2
                    datum.files << file3
                }
                genome.data << datum
            } else {
                String file1 = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_FILE_1 );
                if( file1  &&  !file1.isEmpty() )
                    datum.files << file1
                String file2 = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_FILE_2 );
                if( file2  &&  !file2.isEmpty() )
                    datum.files << file2
                String file3 = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_FILE_3 );
                if( file3  &&  !file3.isEmpty() )
                    datum.files << file3
                genome.data << datum
            }

            config.genomes << genome
            rowIdx++
            if( rowIdx < genomeTable.getNoRows() ) strain = genomeTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_STRAIN )
            else break

        }


        // write config object to JSON config file
        String jsonConfigContent = JsonOutput.toJson( config )
        jsonPath.toFile() << JsonOutput.prettyPrint( jsonConfigContent )

    }

}

