function registerOddEvenRows() {
    $$('tr:nth-child(odd)').each(function (e) {
        e.observe('mouseover', function (event) {
            event.findElement('tr').className = 'row-highlight';
        });
        e.observe('mouseout', function (event) {
            event.findElement('tr').className = 'row-odd';
        });
        e.className = 'row-odd';
    });
    $$('tr:nth-child(even)').each(function (e) {
        e.observe('mouseover', function (event) {
            event.findElement('tr').className = 'row-highlight';
        });
        e.observe('mouseout', function (event) {
            event.findElement('tr').className = 'row-even';
        });
        e.className = 'row-even';
    });
}

document.observe("dom:loaded", function () {
    registerOddEvenRows();
});
