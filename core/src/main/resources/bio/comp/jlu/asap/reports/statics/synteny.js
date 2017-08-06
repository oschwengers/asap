/**
 * @param {Object} global The global-object.
 * @param {Object} d3 The D3-framework.
 * @param {Object} $ The JQuery-framework.
 */
;(function (global, d3, $) {

    const OPACITY_LOW   = 0.4;
    const OPACITY_HIGH  = 1;
    const STROKE_COLORS = { "+":"#2196f3", "-":"#ff9800" }; // material colors by material.io
    const INBOX_PADDING = 8;

    let svgContainer    = null;
    let plotCounter = 0;

    // optionals
    let STROKE_WIDTH    = 3;

    $(() => { // add objects to global-object
        global.drawDotPlot = drawDotPlot;
    });

    /**
     * Creates an interactive dotplot from json like this:
     * { contig: <string>, rStart: <NUM>, rEnd: <NUM>, cStart: <NUM>, cEnd: <NUM>, strand: <string: + or -> }
     * and renders it into selector.
     *
     * @param {string} data The data: [{"1.1": "1.2", "2.1": 2.2, ...}].
     * @param {string} SELECTOR The seletor.
     * @param {string} REF_LAB The name of x-axis-label.
     * @param {string} QRY_LAB The name of y-axis-label.
     * @param {map} optionals The optionals (see let-variables above).
     */
    let drawDotPlot = function (data, SELECTOR, TITLE, REF_LAB, QRY_LAB, optionals = {}) {

        plotCounter++;

        Object.keys(optionals).forEach(key => {
            value = optionals[key];
            switch (key) {
                case "stroke_width"   : STROKE_WIDTH    = value; break;
            }
        });

        let margin = {top: 20, right: 40, bottom: 50, left: 80},
            WIDTH = document.querySelector(SELECTOR).clientWidth - margin.left - margin.right, // 3rem
            HEIGHT = WIDTH / 3 - margin.top - margin.bottom;

        /*
         * scale - optionalss value to a visual display encoding, such as a pixel position.   value -> display
         * axis - sets up axis
         */
        const xScale = d3.scaleLinear().range([0, WIDTH]),
            xAxis = d3.axisBottom(xScale);
        const yScale = d3.scaleLinear().range([HEIGHT, 0]),
            yAxis = d3.axisLeft(yScale);

        svgContainer = d3.select(SELECTOR).append('svg')
            .attr('id', 'dotPlot')
            .attr("width", WIDTH + margin.left + margin.right)
            .attr("height", HEIGHT + margin.top + margin.bottom)
            .style('width', '100%')
            .style('height', 'auto')
            .append("g")
            .attr("transform", `translate(${margin.left}, ${margin.top})`)

        const xMin     = d3.min(data, d => d.rStart);
        const xMax     = d3.max(data, d => d.rEnd);
        const xPadding = (xMax - xMin) / WIDTH * (STROKE_WIDTH + INBOX_PADDING);

        let yMin     = -1;
        let yMax     = 0;
        Array.from(data, (e) => {
            if (yMin == -1) {
                yMin = e.cStart;
                yMax = e.cStart;
            }
            yMax += e.cEnd - e.cStart;
        })
        const yPadding = (yMax - yMin) / HEIGHT * (STROKE_WIDTH + INBOX_PADDING);

        // title
        svgContainer.append("text")
            .attr("x", (WIDTH / 2))
            .attr("y", 0 - (margin.top / 3))
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .text( TITLE );

        // x-axis
        xScale.domain([xMin - xPadding, xMax + xPadding]);
        svgContainer.append("g")
            .attr('id', 'xxx')
            .attr("transform", "translate(0," + HEIGHT + ")")
            .call(xAxis);

        // x-label
        svgContainer.append("text")
            .attr('id', 'yyy')
            .attr("transform", "translate(" + (WIDTH / 2) + " ," + (HEIGHT + margin.top + 20) + ")")
            .style("text-anchor", "middle")
            .text(`${REF_LAB}`);

        // x-axis-2
        svgContainer.append("line")
            .attr("x1", 0)
            .attr("y1", 0.5)
            .attr("x2", WIDTH)
            .attr("y2", 0.5)
            .style("stroke", 'black');

        // y-axis
        yScale.domain([yMin - yPadding, yMax + yPadding]);
        svgContainer.append("g")
            .call(yAxis);

        // y-label
        svgContainer.append("text")
            .attr("transform", "rotate(-90)")
            .attr("y", 0 - margin.left)
            .attr("x", 0 - (HEIGHT / 2))
            .attr("dy", "1em")
            .style("text-anchor", "middle")
            .text(`${QRY_LAB}`);

        // y-axis-2
        svgContainer.append("line")
            .attr("x1", WIDTH + 0.5)
            .attr("y1", 0)
            .attr("x2", WIDTH + 0.5)
            .attr("y2", HEIGHT)
            .style("stroke", 'black');

        // memory for main-strokes
        let memory = -1;
        const reset = function() { memory = -1; }

        // main-strokes
        svgContainer.selectAll(".line" + plotCounter)
            .data(data)
            .enter()
            .append("line")
            .attr("class", "line" + plotCounter)
            .attr("x1", d => {
                return xScale(d.rStart) + STROKE_WIDTH / 2;
            })
            .attr("y1", d => {
                if (memory == -1) {
                    memory = d.cEnd;
                    return yScale(d.strand == '+' ? d.cStart : d.cEnd);
                }
                dif = d.cEnd - d.cStart;
                out = d.strand == '+' ? memory : memory + dif;
                memory += dif;
                return yScale(out);
            })
            .call(reset)
            .attr("x2", d => {
                return xScale(d.rEnd) - STROKE_WIDTH / 2;
            })
            .attr("y2", d => {
                if (memory == -1) {
                    memory = d.cEnd;
                    return yScale(d.strand == '+' ? d.cEnd : d.cStart);
                }
                dif = d.cEnd - d.cStart;
                out = d.strand == '+' ? memory + dif : memory;
                memory += dif;
                return yScale(out);
            })
            .style("stroke", d => STROKE_COLORS[ d.strand ] )
            .style("stroke-width", STROKE_WIDTH)
            .style("stroke-linecap", "round")
            .style("opacity", OPACITY_LOW)
            .on('mouseover', function (element) {
                this.style.opacity = OPACITY_HIGH;
            })
            .on('mouseout', function (element) {
                this.style.opacity = OPACITY_LOW;
            })

        // tooltip
        Array.from(d3.selectAll(".line" + plotCounter)._groups[0]).forEach( e => {

            const item = $(e)[0].__data__;

            const queryLen = (item.cEnd > item.cStart) ?
                (item.cEnd - item.cStart).toLocaleString():
                (item.cStart - item.cEnd).toLocaleString();

            $(e).tooltip({
                animation: false,
                html     : true,
                placement: (item.rEnd < xMax * 0.5) ? 'right': 'left',
                container: 'body',
                title    : `
                    <span style='font-weight:bold'>Contig</span> ${item.contig}<br>
                    <span style='font-weight:bold'>Length</span> ${queryLen}<br>
                    <span style='font-weight:bold'>Strand</span> ${item.strand}<br>
                    <span style='font-weight:bold'>Position</span> ${item.rStart.toLocaleString()} - ${item.rEnd.toLocaleString()}<br>
                `,
            });

            let xPosition, yPosition;

            const drawTip = function (event) {

                const item = event.currentTarget.__data__;
                const tip = $('.tooltip');

                if (item.rEnd < xMax * 0.5) { // right
                    xPosition = event.pageX + 20;
                    yPosition = event.pageY;
                }
                else { // left
                    xPosition = event.pageX - tip.width() - 20;
                    yPosition = event.pageY;
                }

                $('.tooltip').css({
                    left: xPosition,
                    top: yPosition,
                    transform: "translateY(-50%)",
                    opacity: 0.8,
                })
                $('.tooltip-inner').css({
                    "max-width": "none",
                    "text-align": "left",
                    "font-family": "consolas",
                })
                $('.tooltip-arrow').css({
                    opacity: 0,
                })
            }

            $(e).on("mouseenter mousemove", (event) => drawTip(event) );
            $(e).mouseleave( (event)                => $('.tooltip').css("opacity", 0));

        }); // tooltip

    }; // drawDotPlot(..)

}(window, d3, jQuery));
