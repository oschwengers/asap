
package bio.comp.jlu.asap

import java.nio.file.*
import groovy.util.CliBuilder


// check options
def cli = new CliBuilder( usage: 'java -jar asap-cloud-setup.jar --project-path <project-path>' )
    cli.p( longOpt: 'project-path', args: 1, argName: 'project path', required: true, 'Path to project directory.' )

def opts = cli.parse( args )
if( !opts  ||  !opts.p )
    System.exit( 1 )
else if( opts.h )
    cli.usage()


// check project dir path
Path rawProjectPath = Paths.get( opts.p )
if( !Files.isExecutable( rawProjectPath ) )
    exit( "project directory (${rawProjectPath}) does not exist or wrong permissions (read/write/execute) set!" )
Path projectPath = rawProjectPath.toRealPath()
Path spreadsheetFilePath = projectPath.resolve( 'config.xls' )
if( !Files.exists( spreadsheetFilePath ) ) // test for valid spreadsheet config file
    exit( 'no config file available! Please, provide a valid config file using the Excel® 95 spreadsheet format (xls).' )


// check config file path
Path asapPath = Paths.get( System.getProperty( 'user.home' ), 'asap-cloud' )
Path configPath = asapPath.resolve( 'asap.properties' )
if( !Files.isReadable( configPath ) )
    exit( "config file (${configPath}) does not exist or is not readable!" )


// get number of genomes
TableBookAdapter tba = new ExcelTableBookAdapter()
if( !tba.acceptFile( spreadsheetFilePath.toFile() ) )
    exit( "wrong config file suffix (${spreadsheetFilePath})! Please, provide a valid Excel config file using the Excel '97 format (.xls)." )
TableBook tableBook = tba.importTableBook( spreadsheetFilePath.toFile() )
if( tableBook.getNoTables() < 2 )
    exit( "wrong number of tables (${tableBook.getNoTables()})!" )
Table strainTable  = tableBook.getTable( 1 )
int rowIdx = 1
String species = strainTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_SPECIES )
while( species != null  &&  !species.isEmpty() ) {
    rowIdx++
    if( rowIdx < strainTable.getNoRows() ) species = strainTable.getCellContent( rowIdx, ConfigTemplate.COLUMN_ID_SPECIES )
    else break
}
final int noGenomes = rowIdx


// read props
Properties props = new Properties()
configPath.toFile().withInputStream {
    props.load( it )
}


// calculate appropriate number of SGE slave nodes
int cpuProject = props.getProperty( 'cloud.quota.cpu' ) as int
int cpuMaster  = props.getProperty( 'master.cpu' ) as int
int cpuSlaves  = props.getProperty( 'slaves.cpu' ) as int
int maxAmountSlaves = (cpuProject - cpuMaster - 2) / cpuSlaves
int desiredAmountSlaves = noGenomes / (cpuSlaves / 8)
final int noSlaves = desiredAmountSlaves > maxAmountSlaves ? maxAmountSlaves : desiredAmountSlaves


// import cloud and BiBiGrid values
String region = props.getProperty( 'cloud.region' )
String zone = props.getProperty( 'cloud.zone' )
String subnet = props.getProperty( 'cloud.subnet' )
String masterFlavour = props.getProperty( 'master.instance' )
String slavesFlavour = props.getProperty( 'slaves.instance' )
String baseImageId = props.getProperty( 'base.image' )
String volumeIdAsap  = props.getProperty( 'volume.asap' )
String volumeIdData  = props.getProperty( 'volume.data' )


def bibigridTemplate = """
#use openstack
mode: openstack

#Access
sshPrivateKeyFile: ${asapPath.toString()}/asap.cluster.key
sshUser: ubuntu
keypair: asap-cluster
region: ${region}
availabilityZone: ${zone}

#Network
subnet: ${subnet}

#BiBiGrid-Master
masterInstance:
  type: ${masterFlavour}
  image: ${baseImageId}

#BiBiGrid-Slave
slaveInstances:
  - type: ${slavesFlavour}
    count: ${noSlaves}
    image: ${baseImageId}

#Mountpoints
masterMounts:
  - source: ${volumeIdAsap}
    target: /asap/
  - source: ${volumeIdData}
    target: /data/

#NFS-Shares
nfsShares:
  - /asap/
  - /data/

#Firewall/Security Group
ports:
  - type: TCP
    number: 80

#services
useMasterAsCompute: yes
nfs: yes
oge: yes
"""


File biBiGridPropFile = asapPath.resolve( 'bibigrid.yml' ).toFile()
biBiGridPropFile.text = bibigridTemplate.toString()


def exit( String msg ) {
    println( "Error: ${msg}" )
    System.exit( 1 )
}