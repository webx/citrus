function setQuery(resourceName, submit) {
    var form = $('webx-resource-query-form');
    var textbox = $('webx-resource-query');

    textbox.value = resourceName;

    if (submit) {
        form.submit();
    }
}

function registerResolvableDepValueHandle() {
    var handleClick = function (event) {
        var imgObj = $(event.target.id);
        var imgSrc = imgObj.src;
        var content = event.target.id.gsub(/^handle-/, 'resolvable-deps-value-');
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

    $$('img.webx-resolvable-deps-value-handle').each(function (e) {
        e.observe('click', handleClick);
    });
}

function registerConfigurationFileContentHandle() {
    var handleClick = function (event) {
        var imgObj = $(event.target.id + '-img');
        var imgSrc = imgObj.src;
        var content = event.target.id.gsub(/^handle-/, 'configuration-file-content-');
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

    $$('a.handle-configuration-file-content').each(function (e) {
        e.observe('click', handleClick);
    });
}

function registerPullToolValueHandle() {
    var handleClick = function (event) {
        var imgObj = $(event.target.id + '-img');
        var imgSrc = imgObj.src;
        var content = event.target.id.gsub(/^handle-/, 'pull-tool-value-');
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

    $$('a.webx-pull-tool').each(function (e) {
        e.observe('click', handleClick);
    });
}

document.observe("dom:loaded", function () {
    $$('.webx-resolvable-deps-value').each(function (e) {
        e.hide();
    });

    registerResolvableDepValueHandle();

    $$('.configuration-file-content').each(function (e) {
        e.hide();
    });

    registerConfigurationFileContentHandle();

    $$('.pull-tool-value').each(function (e) {
        e.hide();
    });

    registerPullToolValueHandle();
});
