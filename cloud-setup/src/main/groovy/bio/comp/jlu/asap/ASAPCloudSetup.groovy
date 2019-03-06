
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

// import cloud values
String region = props.getProperty( 'cloud.region' )
String zone = props.getProperty( 'cloud.zone' )

// import project values
String subnetId = props.getProperty( 'project.subnet.id' )
int quotaCpu = props.getProperty( 'project.cpu' ) as int
int quotaMem = props.getProperty( 'project.mem' ) as int

// import cluster values
String imageFlavour = props.getProperty( 'vm.flavour' )
String baseImageId = props.getProperty( 'vm.image.id' )
int vmCpu = props.getProperty( 'vm.cpu' ) as int
int vmMem = props.getProperty( 'vm.mem' ) as int
String volumeIdAsap  = props.getProperty( 'volume.asap.id' )
String volumeIdData  = props.getProperty( 'volume.data.id' )


// calculate appropriate number of SGE slave nodes
int maxVmsCpu = (quotaCpu - vmCpu - 2) / vmCpu  // substract cores for 1 master and the gateway instance
int maxVmsMem = (quotaMem - vmMem - 4) / vmMem  // substract mem [Gb] for 1 master and the gateway instance
int maxVms = Math.min( maxVmsCpu, maxVmsMem )
int desiredVms = noGenomes / (vmCpu / 8)
final int noSlaves = desiredVms > maxVms ? maxVms : desiredVms


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
subnet: ${subnetId}

#BiBiGrid-Master
masterInstance:
  type: ${imageFlavour}
  image: ${baseImageId}

#BiBiGrid-Slave
slaveInstances:
  - type: ${imageFlavour}
    image: ${baseImageId}
    count: ${noSlaves}

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