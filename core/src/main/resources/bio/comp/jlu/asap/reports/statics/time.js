$(document).ready( () => {
    $('.time').each( (idx,e) => {$(e).text(moment(e.dataset.time, moment.ISO_8601).format("YYYY-MM-DD HH:mm:ss Z"))} )
    $('.time-duration').each( (idx,e) => {
        let start = moment(e.dataset.start, moment.ISO_8601)
        let end = moment(e.dataset.end, moment.ISO_8601)
        let diff = moment.duration(end.diff(start)).asSeconds()
        $(e).text(numeral(diff).format('00:00:00'))
    } )
})


