function registerConfigurationFileContentHandle() {
    var handleClick = function(event) {
        var imgObj = $(event.target.id + '-img');
        var imgSrc = imgObj.src;
        var content  = event.target.id.gsub(/^handle-/, 'configuration-file-content-');
        var index = imgSrc.lastIndexOf('/') + 1;
        var file = imgSrc.substring(index);
        var prefix = imgSrc.substring(0, index);

        if (file == 'open.png') {
            Effect.SlideUp(content);
            imgObj.src = prefix + 'close.png';
        } else {
            Effect.SlideDown(content);
            imgObj.src = prefix + 'open.png';
        }
    };

    $$('a.handle-configuration-file-content').each( function(e) {
        e.observe('click', handleClick);
    });
}

document.observe("dom:loaded", function() {
    $$('.configuration-file-content').each( function(e) {
        e.hide();
    });

    registerConfigurationFileContentHandle();
});
