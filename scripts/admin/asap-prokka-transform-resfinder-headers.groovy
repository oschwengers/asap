
import java.nio.file.*

Path resFinderNotesPath = Paths.get( args[0] ).toRealPath()

println "parse ResFinder notes.txt: $resFinderNotesPath"
def headers = [:]
resFinderNotesPath.eachLine( {
    if( it.charAt( 0 ) != '#' ) {
        def cols = it.split( ':' )
        headers[ (cols[0]) ] = [ gene: cols[0], product: cols[1] ]
    }
} )
//println headers


Path workDirPath = resFinderNotesPath.getParent()
Path curatedResFinderPath = workDirPath.resolve( 'ResFinder.ffa' )

println "scan ResFinder dir ($workDirPath) for .fsa files"
workDirPath.eachFileMatch( ~/.+\.fsa/, {
    println "\t parse file: $it"
    it.eachLine( { line ->
        line = line.trim()
        if( !line.isEmpty() ) {
            if( line.charAt( 0 ) == '>' ) {
                def fastaHeader = line.split( '_' )[0].substring(1)
                def header = headers[ fastaHeader ]
                if( header ) {
                    def newHeader = "${line} ~~~${header.gene}~~~${header.product}"
                    println "\t\treplaced header: $newHeader"
                    curatedResFinderPath << newHeader + '\n'
                } else {
                    println "\t\toriginal header: $fastaHeader"
                    curatedResFinderPath << '>' + fastaHeader + '\n'
                }
            } else curatedResFinderPath << line + '\n'
        }
    } )
} )
