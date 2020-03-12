function findlink(linkText) {
  // find fasta link
  let links = document.getElementsByTagName('a')
  let selected_links = Array.from(links).filter(el => el.text === linkText)
  let link = null;
  if (selected_links.length == 1) {
    link = selected_links[0].getAttribute("href")
    return link
  } else {
    throw "Multiple faa links on page"
  }
}

function loadExternalJsLib(url, callback) {
  let tag = document.createElement('script')
  tag.src = url
  document.body.appendChild(tag);
  tag.addEventListener("load", callback)
}

// link of psos server
var psosUrl = 'https://psos-staging.computational.bio'
function initPsos(x) {
  psos.init(psosUrl + '/api/v1', psosUrl)
  console.log('psos initialized')
}

// reference to biojs-io-fasta object. Is initialized when lib is added
var Fasta = null;
function initFasta(x) {
  Fasta = require('biojs-io-fasta');
  console.log('fasta-io initialized')
}

// index sequenceid -> sequenceobject. 
function generateIndex(sequences) {
  let idx = {}
  sequences.forEach(s => idx[s.name] = s)
  return idx
}

// submits job to psos based on the locus name. requires psos.js,
// biojs-io-fasta and index to be initialized
function submitJob(locus) {
  
  let link = findlink('faa')
  // read sequences
  Fasta.read(link, 
    function(err, model) {
      console.log("Fasta read")
      index = generateIndex(model)
      console.log("Index created")

      let request = {
        'configuration': {'profile': 'bacteria-gram-'},
        'sequence': Fasta.write([index[locus]])
      }
      
      psos.submit(request,
       psos.redirect,
       function (error) {
         // replace with your code when an error occured in the job submission
         console.log(error)
       }
     )
    }
  ) 
}

// retrieves the locus name from a row
function getLocus(row) {
  let tds = row.getElementsByTagName('td')
  return $(tds[1]).text()
}

// Adds 'Analyze with psos' buttons to each row in the table
function updateAnnotationTableWithAnalyzeButton() {
    // disable datatables to see all available rows
    $('#annotationTable').DataTable().destroy()

    // add psos button for each row
    let rows = document.getElementById("annotationTable").getElementsByTagName("tr")
    for (let rowN = 0; rowN < rows.length; rowN++) {
      if (rowN > 0) {
        let cell = document.createElement("td")
        let btn = document.createElement("button")
        let locus = getLocus(rows[rowN])

        btn.appendChild(document.createTextNode("Analyze with Psos"))
        btn.onclick = function() {submitJob(locus)}
        cell.appendChild(btn)
       
        rows[rowN].appendChild(cell)
      } else {
        rows[rowN].appendChild(document.createElement("th"))
      }
    }

    // restart datatables
    $('#annotationTable').DataTable( {
      paging:   true,
      ordering: true,
      info:     true,
      dom: "<'row'<'col-md-3'l><'col-md-3 col-md-offset-5'f><'col-md-1'B>><'row'<'col-md-12't>><'row'<'col-md-6'i><'col-md-6'p>>",
      buttons: [
        {
          extend: 'csv',
          text: 'csv',
          filename: 'annotation-E_coli_pb-m141013-rs2',
          exportOptions: {
            columns: [ 0, 1, 2, 3, 4, 5, 6, 7, 8 ]
          }
        }
      ],
      order: [[ 2, "asc" ],[ 1, "asc" ]]
    } );
}

// assume jquery is available
$(document).ready(function () {

    // load biojs-io-fasta, read fasta and initialize index
    loadExternalJsLib('https://wzrd.in/bundle/biojs-io-fasta@latest', x => initFasta(x))

    // load psos lib and init it with psos url
    loadExternalJsLib( psosUrl + '/api/v1/psos.js', x=> initPsos(x))

    updateAnnotationTableWithAnalyzeButton();
})
