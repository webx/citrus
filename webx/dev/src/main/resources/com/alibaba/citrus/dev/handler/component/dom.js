function registerDomElementHandle() {
    var handleClick = function(event) {
        var imgObj = event.target;
        var imgSrc = imgObj.src;
        var collapsed = imgObj.id.gsub(/^handle-/, 'collapsed-');
        var expanded  = imgObj.id.gsub(/^handle-/, 'expanded-');
        var index = imgSrc.lastIndexOf('/') + 1;
        var file = imgSrc.substring(index);
        var prefix = imgSrc.substring(0, index);

        if (file == 'open.gif') {
            $(expanded).hide();
            $(collapsed).style.display = 'inline';
            imgObj.src = prefix + 'close.gif';
        } else {
            $(expanded).show();
            $(collapsed).style.display = 'none';
            imgObj.src = prefix + 'open.gif';
        }
    };

    $$('img.handle-element').each( function(e) {
        e.observe('click', handleClick);
    });
}

document.observe("dom:loaded", function() {
    registerDomElementHandle();
});