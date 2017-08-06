
package bio.comp.jlu.asap;


import java.io.File;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Blank;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class ExcelTableBookAdapter implements TableBookAdapter {

    private static final String FILE_EXTENSION = "xls";

    private static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter( "MS Excel Files", FILE_EXTENSION );


    @Override
    public boolean acceptFile( File file ) {

        return file.getName().toLowerCase().endsWith( "." + FILE_EXTENSION );

    }


    @Override
    public FileNameExtensionFilter getFileExtensionFilter() {

        return FILE_FILTER;

    }


    @Override
    public TableBook importTableBook( final File file ) throws IOException {

        try {

            Workbook wb = Workbook.getWorkbook( file );
            TableBook tblBk = new TableBook( file.getName(), wb.getNumberOfSheets() );

            for( int nrTbl = 0; nrTbl < wb.getNumberOfSheets(); nrTbl++ ) {

                final Sheet sh = wb.getSheet( nrTbl );
                final int noRows = sh.getRows();
                final int noCols = sh.getColumns();
                final Table tbl = new Table( sh.getName(), noRows, noCols );
                for( int nrRow = 0; nrRow < noRows; nrRow++ ) {
                    for( int nrCol = 0; nrCol < noCols; nrCol++ ) {
                        String val = sh.getCell( nrCol, nrRow ).getContents();
                        if( val != null && !val.isEmpty() )
                            tbl.setCellContent( nrRow, nrCol, val );
                    }
                }
                tblBk.setTable( nrTbl, tbl );

            }

            return tblBk;

        }
        catch( BiffException be ) {
            throw new IOException( be );
        }

    }


    @Override
    public void exportTableBook( final TableBook tblBk, final File file ) throws IOException {

        try {

            WritableWorkbook wwb = Workbook.createWorkbook( file );
            for( int nrTbl = 0; nrTbl < tblBk.getNoTables(); nrTbl++ ) {

                final Table tbl = tblBk.getTable( nrTbl );
                final WritableSheet ws = wwb.createSheet( tbl.getName(), nrTbl );

                final int noRows = tbl.getNoRows();
                final int noCols = tbl.getNoColumns();
                for( int nrRow = 0; nrRow < noRows; nrRow++ ) {
                    for( int nrCol = 0; nrCol < noCols; nrCol++ ) {

                        WritableCell wc;
                        final String cellCntnt = tbl.getCellContent( nrRow, nrCol );
                        if( cellCntnt == null || cellCntnt.isEmpty() ) {
                            wc = new Blank( nrCol, nrRow );
                        }
                        else {
                            try {
                                double val = Double.parseDouble( cellCntnt );
                                wc = new jxl.write.Number( nrCol, nrRow, val );
                            }
                            catch( NumberFormatException nfe ) {
                                wc = new Label( nrCol, nrRow, cellCntnt );
                            }
                        }

                        ws.addCell( wc );

                    }
                }

            }

            wwb.write();
            wwb.close();

        }
        catch( WriteException ex ) {
            throw new IOException( ex );
        }

    }


}
