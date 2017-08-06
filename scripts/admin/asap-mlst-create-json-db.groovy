// always invoke this script via $ASAP_HOME/bin/groovy

/**********************
 *** Script Imports ***
**********************/


import java.nio.file.*
import groovy.json.*
import groovy.util.CliBuilder

import static groovy.io.FileType.FILES




/************************
 *** Script Constants ***
************************/

ASAP_DB = System.getenv()['ASAP_DB']

MLST_DB = "${ASAP_DB}/mlst"



/*********************
 *** Script Params ***
*********************/


def cli = new CliBuilder( usage: '$ASAP_HOME/bin/groovy asap-create-mlst-json-db.groovy --scheme-path <scheme-path>' )
    cli.h( longOpt: 'help', 'Show usage information' )
    cli.p( longOpt: 'scheme-path', 'Path to schemes directory', args: 1, required: true )


def opt = cli.parse( args )
if( !opt  ||  opt.h ) {
    cli.usage()
    System.exit( 0 )
}


def schemesPath = Paths.get( opt.p ).toRealPath()



/************************
 *** Script Logic ******
************************/

def stProfiles = []
def nonGeneNames = [ 'scheme', 'ST', 'species', 'clonal_complex', 'mlst_clade', 'Lineage' ,'CC'] // some lociNames got '_' in their name maybe that needs to be removed
schemesPath.eachFileRecurse( FILES, { file ->

    String fileName = file.toFile().name
    if( fileName.endsWith( '.txt' )  &&  !fileName.contains( 'readme' ) ) { // skip readme files
        String scheme = fileName - '.txt'
        //println "scheme file: ${scheme}"
        def stProfileCols = []
        file.splitEachLine( '\t', { stProfileCols << it } )
        def headers = stProfileCols[0].collect( { it.charAt(0) == '_' ? it.substring( 1 ) : it } )
        int noCols = headers.size()
        def genes  = headers - nonGeneNames
        stProfileCols = stProfileCols.drop( 1 )
        //println "\t#: ${noCols}\t${headers}"
        stProfileCols.each( { cols ->
            def stProfile = [
                scheme: scheme,
                alleles: [:]
            ]
            def data = [:]
            for( int i=0; i<noCols; i++ ) {
                data[ (headers[i]) ] = cols[i]
            }
            stProfile.st = data['ST']
            for( gene in genes ) {
                stProfile.alleles[ (gene) ] = data[ (gene) ]
            }
            String ccToken = [ 'CC', 'cc', 'clonal_complex' ].find( {data[(it)]} )
            stProfile.cc = ccToken ? data[ (ccToken) ] : '-'

            String lineageToken = [ 'Lineage', 'mlst_clade'  ].find( {data[(it)]} )
            stProfile.lineage = lineageToken ? data[ (lineageToken) ] : '-'
            stProfiles << stProfile
            //println "\t\tprofile:\t${stProfile}"
        } )
    }

} )

//println JsonOutput.prettyPrint( JsonOutput.toJson( stProfiles ) )
println JsonOutput.toJson( stProfiles )