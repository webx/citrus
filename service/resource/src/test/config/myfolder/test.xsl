<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output indent="no" method="xml" encoding="GB2312" />
    <xsl:template match="persons">
        <greetings>
            <xsl:apply-templates />
        </greetings>
    </xsl:template>
    <xsl:template match="person">
        <hello>
            <xsl:attribute name="name">
                <xsl:value-of select="@name" />
            </xsl:attribute>
        </hello>
    </xsl:template>
</xsl:stylesheet>
