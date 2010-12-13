function registerToggleTargetNamespace() {
    var checkbox = $('toggleTargetNamespace');
    var checkboxOnClick = function(event) {
        $$("li.no-target-namespace").each( function(e) {
            if (checkbox.checked) {
                e.show();
            } else {
                e.hide();
            }
        });
    };

    checkbox.observe('click', checkboxOnClick);
    checkboxOnClick();
}

function registerToggleXmlTemplate() {
    var checkbox = $('toggleXmlTemplate');
    var checkboxOnClick = function(event) {
        if (checkbox.checked) {
            $('xml-template').show();
        } else {
            $('xml-template').hide();
        }
    };

    checkbox.observe('click', checkboxOnClick);
    checkbox.checked = true;
    checkboxOnClick();
}

function registerUpdateXmlTemplate() {
    var checkboxes = $$('li input[type="checkbox"]');
    var updateXmlTemplate = function(event) {
        var checkbox = event == null ? null : event.target;
        var currentNs = checkbox != null && checkbox.checked ? checkbox.name
                .split(';')[0] : null;
        var beans = false;
        var beansNames = null;
        var xml = '<pre class="xml-template">';
        var locations = '';

        xml += '<?xml version="1.0" encoding="UTF-8" ?>\n'.escapeHTML();
        xml += '<beans:beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n'
                .escapeHTML();

        checkboxes
                .each( function(e) {
                    var names = e.name.split(';');

                    if (beansNames == null
                            && names[0] == 'http://www.springframework.org/schema/beans') {
                        beans = e.checked;
                        beansNames = names;
                    }

                    if (e.checked) {
                        var decl = ('             xmlns:' + names[1] + '="'
                                + names[0] + '"\n').escapeHTML();
                        var loc = ('               ' + names[0] + ' '
                                + names[2] + '\n').escapeHTML();

                        if (currentNs == names[0]) {
                            xml += '<em>' + decl + '</em>';
                            locations += '<em>' + loc + '</em>';
                        } else {
                            xml += decl;
                            locations += loc;
                        }
                    }
                });

        if (!beans && beansNames != null) {
            xml += ('             xmlns:' + beansNames[1] + '="'
                    + beansNames[0] + '"\n').escapeHTML();
            locations += ('               ' + beansNames[0] + ' '
                    + beansNames[2] + '\n').escapeHTML();
        }

        xml += '             xmlns:p="http://www.springframework.org/schema/p"\n'
                .escapeHTML();

        xml += '             xsi:schemaLocation="\n'.escapeHTML();
        xml += locations;
        xml += '             ">\n'.escapeHTML();
        xml += '\n';
        xml += '</beans:beans>'.escapeHTML();
        xml += "</pre>"

        $('xml-template').innerHTML = xml;
    };

    checkboxes.each( function(e) {
        e.observe('click', updateXmlTemplate);
    })

    updateXmlTemplate(null);
}

function registerDirectoryHandle() {
    var handleClick = function(event) {
        var imgObj = event.target;
        var imgSrc = imgObj.src;
        var subdirs = imgObj.id.gsub(/^handle-/, 'subdirs-');
        var index = imgSrc.lastIndexOf('/') + 1;
        var file = imgSrc.substring(index);
        var prefix = imgSrc.substring(0, index);

        if (file == 'open.gif') {
            $(subdirs).hide();
            imgObj.src = prefix + 'close.gif';
        } else {
            $(subdirs).show();
            imgObj.src = prefix + 'open.gif';
        }
    };

    $$('li.directory>img').each( function(e) {
        e.observe('click', handleClick);
    });
}

document.observe("dom:loaded", function() {
    registerToggleXmlTemplate();
    registerToggleTargetNamespace();
    registerUpdateXmlTemplate();
    registerDirectoryHandle();
});
