
import java.nio.file.*
import groovy.json.*

final def pHeader = ~/(VFG\d{6})(?:\(gb\|[\w\.]+?\))? \(([\w\/\.\-\\']{2,20})\) (.+)(?:\[.+\]) (?:\[.+\])/

Path vfDataPath = Paths.get( args[0] ).toRealPath()
vfDataPath.text
	.replaceAll( '<i>', '' )
	.replaceAll( '</i>', '' )
	.replaceAll( '<alpha>', '' )
	.replaceAll( '<beta>', '' )
	.replaceAll( '<gamma>', '' )
	.replaceAll( '<delta>', '' )
	.split( '>' )
	.each( { entry ->

	List<String> lines = entry.split( '\n' )
	def header = lines[0]
	def m = header =~ pHeader
	if( m ) {
		def v = m[0]
		def geneID   = v[1]
		def geneName = v[2]
		def product  = v[3]
		if( geneName.indexOf('/') != -1 ) {
			geneName = geneName.split('/')[0]
		}
		println ">${geneID} ~~~${geneName}~~~${product}"
		lines.remove(0)
		println lines.join('')
	} else {
		//println header
	}
} )
